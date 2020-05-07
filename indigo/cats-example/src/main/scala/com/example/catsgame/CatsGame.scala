package com.example.catsgame

import indigo._
import indigo.json.Json
import indigoexts.formats._
import indigogame._

object CatsGame extends IndigoGameBasic[CustomStartupData, DudeModel, Unit] {

  val targetFPS: Int = 60

  private val magnificationLevel: Int = 2
  private val viewportWidth: Int      = 228 * magnificationLevel
  private val viewportHeight: Int     = 128 * magnificationLevel

  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(viewportWidth, viewportHeight),
      frameRate = targetFPS,
      clearColor = ClearColor(0.4, 0.2, 0.5, 1),
      magnification = magnificationLevel
    )

  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] =
    Set()

  val animations: Set[Animation] =
    Set()

  val subSystems: Set[SubSystem] =
    Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, CustomStartupData] = {
    def makeStartupData(aseprite: Aseprite, spriteAndAnimations: SpriteAndAnimations): Startup.Success[CustomStartupData] =
      Startup
        .Success(
          CustomStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withRef(16, 16)                                                                         // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
            )
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[CustomStartupData]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(Assets.dudeName.value + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(aseprite, Depth(3), Assets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    res.getOrElse(Startup.Failure(StartupErrors("Failed to load the dude")))
  }

  def initialModel(startupData: CustomStartupData): DudeModel =
    DudeModel(startupData.dude, DudeIdle)

  def update(gameTime: GameTime, model: DudeModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[DudeModel] = {
    case KeyboardEvent.KeyDown(Keys.LEFT_ARROW) =>
      Outcome(model.walkLeft)

    case KeyboardEvent.KeyDown(Keys.RIGHT_ARROW) =>
      Outcome(model.walkRight)

    case KeyboardEvent.KeyDown(Keys.UP_ARROW) =>
      Outcome(model.walkUp)

    case KeyboardEvent.KeyDown(Keys.DOWN_ARROW) =>
      Outcome(model.walkDown)

    case KeyboardEvent.KeyUp(_) =>
      Outcome(model.idle)

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: CustomStartupData): DudeModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: DudeModel, viewModel: Unit, inputState: InputState, dice: Dice): Outcome[Unit] =
    Outcome(viewModel)

  def present(gameTime: GameTime, model: DudeModel, viewModel: Unit, inputState: InputState): SceneUpdateFragment =
    SceneUpdateFragment(
      model.walkDirection match {
        case d @ DudeLeft =>
          model.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeRight =>
          model.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeUp =>
          model.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeDown =>
          model.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeIdle =>
          model.dude.sprite
            .changeCycle(d.cycleName)
            .play()
      }
    )
}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class CustomStartupData(dude: Dude)

object Assets {

  val dudeName: AssetName = AssetName("base_charactor")

  val dudeNameMaterial: Material.Textured = Material.Textured(dudeName)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(AssetName(dudeName.value + "-json"), AssetPath("assets/" + dudeName.value + ".json")),
      AssetType.Image(dudeName, AssetPath("assets/" + dudeName.value + ".png"))
    )

}

final case class DudeModel(dude: Dude, walkDirection: DudeDirection) {
  def idle: DudeModel      = this.copy(walkDirection = DudeIdle)
  def walkLeft: DudeModel  = this.copy(walkDirection = DudeLeft)
  def walkRight: DudeModel = this.copy(walkDirection = DudeRight)
  def walkUp: DudeModel    = this.copy(walkDirection = DudeUp)
  def walkDown: DudeModel  = this.copy(walkDirection = DudeDown)
}

sealed trait DudeDirection {
  val cycleName: CycleLabel
}
case object DudeIdle  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("blink")      }
case object DudeLeft  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk left")  }
case object DudeRight extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk right") }
case object DudeUp    extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk up")    }
case object DudeDown  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk down")  }
