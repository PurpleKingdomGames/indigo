package com.example.sandbox

import indigo._
import indigoextras.jobs._

import scala.scalajs.js.annotation.JSExportTopLevel
import indigoextras.geometry.Vertex
import indigoextras.datatypes.TimeVaryingValue
import indigo.shared.EqualTo._

@JSExportTopLevel("IndigoGame")
object JobsExample extends IndigoDemo[Unit, StartupData, Model, Unit] {

  val eventFilters: EventFilters = EventFilters.Default

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(
        GameConfig.default
          .withViewport(400, 400)
          .withClearColor(ClearColor.fromRGB(0.0, 0.2, 0.0))
          .withMagnification(2)
      )
      .withAssets(Assets.assets)
      .withFonts(Assets.fontInfo)
      .withSubSystems(JobMarket.subSystem)

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[StartupData] = {
    val treeData = (1 to (dice.roll(3) + 3)).toList.map { i =>
      TreeData(
        i,
        Vertex(
          Math.sin(dice.rollDouble * Radians.TAU.value),
          Math.cos(dice.rollDouble * Radians.TAU.value)
        ),
        dice.rollDouble
      )
    }

    Startup.Success(StartupData(treeData))
  }

  def initialModel(startupData: StartupData): Model =
    Model(
      Bob(
        position = Point(150 - 16, 90),
        jobs = WorkSchedule((BindingKey("bob")))
      ),
      Grove(
        startupData.trees.map {
          case TreeData(i, v, gr) =>
            Tree(
              index = i,
              position = Point((v.x * 25d).toInt, (v.y * 25d).toInt) + Point(50, 150),
              growth = TimeVaryingValue(0, Seconds.zero),
              growthRate = 10 + (10 * gr).toInt,
              ready = false
            )
        }
      )
    )

  def initialViewModel(startupData: StartupData, model: Model): Unit =
    ()

  def updateModel(context: FrameContext[StartupData], model: Model): GlobalEvent => Outcome[Model] = {
    case FrameTick =>
      model.update(context.running)

    case RemoveTree(index) =>
      model.removeTreeWithIndex(index)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[StartupData], model: Model, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[StartupData], model: Model, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        View.drawHut.moveTo(150, 50),
        View.drawBob(model.bob),
        View.drawTrees(model.grove)
      )
}

final case class StartupData(trees: List[TreeData])

final case class TreeData(index: Int, position: Vertex, growthRate: Double)

final case class Model(bob: Bob, grove: Grove) {

  def update(runningTime: Seconds): Outcome[Model] =
    grove.update(runningTime).map { g =>
      this.copy(
        grove = g
      )
    }

  def removeTreeWithIndex(index: Int): Outcome[Model] =
    Outcome(this.copy(grove = grove.removeTreeWithIndex(index)))

}

final case class Grove(trees: List[Tree]) {

  def update(runningTime: Seconds): Outcome[Grove] =
    Outcome
      .sequence(trees.map(_.update(runningTime)))
      .map(ts => this.copy(trees = ts))

  def removeTreeWithIndex(index: Int): Grove =
    this.copy(trees = trees.filterNot(_.index === index))

}

final case class Tree(index: Int, position: Point, growth: TimeVaryingValue[Int], growthRate: Int, ready: Boolean) {

  def update(runningTime: Seconds): Outcome[Tree] =
    if (ready) Outcome(this)
    else {
      val nextGrowth = growth.increaseTo(100, growthRate, runningTime)
      val isReady    = nextGrowth.value === 100

      Outcome(
        this.copy(
          growth = nextGrowth,
          ready = isReady
        )
      ).addGlobalEvents(
        if (isReady) List(JobMarketEvent.Post(ChopDown(index, position))) else Nil
      )
    }

}

final case class ChopDown(index: Int, position: Point) extends Job {
  val isLocal: Boolean = false
  val jobName: JobName = JobName("chop down tree")
}

final case class RemoveTree(index: Int) extends GlobalEvent

// Bob is our NPC 'Actor'
final case class Bob(
    position: Point,
    jobs: WorkSchedule[Bob, Unit]
) {

  def update(gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[Bob] =
    e => {
      val nextJobs =
        jobs.update(gameTime, dice, this, ())(Bob.bobWorker)(e)

      nextJobs.map {
        case WorkScheduleUpdate(js, b) =>
          b.copy(jobs = js)
      }
    }

}
object Bob {

  val loiterPositionA: Point = Point(150 - 16, 90)
  val loiterPositionB: Point = Point(150 + 16, 90)

  implicit val bobWorker: Worker[Bob, Unit] =
    new Worker[Bob, Unit] {

      def isJobComplete(bob: Bob): Job => Boolean = {
        case ChopDown(_, position) if bob.position === position =>
          true

        case ChopDown(_, _) =>
          false

        case Pace(to) if bob.position === to =>
          true

        case Pace(_) =>
          false

        case Idle(percentDone) if percentDone.value === 100 =>
          true

        case Idle(_) =>
          false

        case _ =>
          true
      }

      def onJobComplete(bob: Bob, context: Unit): Job => Outcome[List[Job]] = {
        case ChopDown(index, _) =>
          Outcome(Nil).addGlobalEvents(RemoveTree(index))

        case Pace(_) =>
          Outcome(Nil)

        case Idle(_) =>
          Outcome(Nil)

        case _ =>
          Outcome(Nil)
      }

      def workOnJob(gameTime: GameTime, bob: Bob, context: Unit): Job => (Job, Bob) = {
        case job @ ChopDown(_, position) =>
          val nextPosition: Point =
            Point(
              x =
                if (position.x === bob.position.x) position.x
                else if (position.x < bob.position.x) bob.position.x - 1
                else bob.position.x + 1,
              y =
                if (position.y === bob.position.y) position.y
                else if (position.y < bob.position.y) bob.position.y - 1
                else bob.position.y + 1
            )

          (job, bob.copy(position = nextPosition))

        case job @ Pace(to) =>
          val nextPosition: Point =
            Point(
              x =
                if (to.x === bob.position.x) to.x
                else if (to.x < bob.position.x) bob.position.x - 1
                else bob.position.x + 1,
              y =
                if (to.y === bob.position.y) to.y
                else if (to.y < bob.position.y) bob.position.y - 1
                else bob.position.y + 1
            )

          (job, bob.copy(position = nextPosition))

        case Idle(percentDone) =>
          (Idle(percentDone.increaseTo(100, 20, gameTime.running)), bob)

        case job =>
          (job, bob)
      }

      def generateJobs(gameTime: GameTime, dice: Dice): List[Job] =
        List(
          Pace(loiterPositionB),
          Pace(loiterPositionA),
          Idle(TimeVaryingValue(0, gameTime.running))
        )

      def canTakeJob(bob: Bob): Job => Boolean = {
        case ChopDown(_, _) =>
          true

        case _ =>
          false
      }

    }

}

final case class Pace(to: Point) extends Job {
  val isLocal: Boolean = true
  val jobName: JobName = JobName("bob is pacing")
}

final case class Idle(percentDone: TimeVaryingValue[Int]) extends Job {
  val isLocal: Boolean = true
  val jobName: JobName = JobName("bob is pacing")
}

object Assets {

  val dots: AssetName = AssetName("dots")
  val font: AssetName = AssetName("boxy font")

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(dots, AssetPath("assets/dots.png")),
      AssetType.Image(font, AssetPath("assets/boxy_font.png"))
    )

  val redDot: Graphic    = Graphic(Rectangle(0, 0, 16, 16), 1, Material.Textured(dots)).withRef(8, 8)
  val greenDot: Graphic  = Graphic(Rectangle(16, 0, 16, 16), 1, Material.Textured(dots)).withRef(8, 8)
  val blueDot: Graphic   = Graphic(Rectangle(0, 16, 16, 16), 1, Material.Textured(dots)).withRef(8, 8)
  val yellowDot: Graphic = Graphic(Rectangle(16, 16, 16, 16), 1, Material.Textured(dots)).withRef(8, 8)

  val fontKey: FontKey = FontKey("Game font")

  val fontInfo: FontInfo =
    FontInfo(fontKey, Material.Textured(font), 320, 230, FontChar(" ", 145, 52, 23, 23)).isCaseInSensitive
      .addChar(FontChar("0", 3, 26, 23, 23))
      .addChar(FontChar("1", 26, 26, 15, 23))
      .addChar(FontChar("2", 41, 26, 23, 23))
      .addChar(FontChar("3", 64, 26, 23, 23))
      .addChar(FontChar("4", 87, 26, 23, 23))
      .addChar(FontChar("5", 110, 26, 23, 23))
      .addChar(FontChar("6", 133, 26, 23, 23))
      .addChar(FontChar("7", 156, 26, 23, 23))
      .addChar(FontChar("8", 179, 26, 23, 23))
      .addChar(FontChar("9", 202, 26, 23, 23))
}

object View {

  def drawHut: Group =
    Group(
      // Roof
      Assets.blueDot.moveTo(-8, -16),
      Assets.blueDot.moveTo(8, -16),
      // Left wall
      Assets.blueDot.moveTo(-16, 0),
      Assets.blueDot.moveTo(-16, 16),
      // Right wall
      Assets.blueDot.moveTo(16, 0),
      Assets.blueDot.moveTo(16, 16)
    )

  def drawBob(bob: Bob): Graphic =
    Assets.redDot.moveTo(bob.position)

  def drawTrees(grove: Grove): Group =
    Group(
      grove.trees
        .map { tree =>
          val scale: Double = tree.growth.value.toDouble / 100.toDouble

          Assets.greenDot
            .moveTo(tree.position)
            .scaleBy(scale, scale)
        }
    )

}
