package indigoexamples

import indigo._
import indigo.shared.EqualTo._
import indigoextras.ui._

import scala.annotation.tailrec
import scala.scalajs.js.annotation._

/**
  * Presents an example of a progress bar being updated by an interleaved computation (a computation
  * whose steps are performed between frames).
  */
@JSExportTopLevel("IndigoGame")
object ProgressBarExample extends IndigoDemo[Unit, Unit, MyGameModel, MyViewModel] {
  val eventFilters: EventFilters = EventFilters.Default

  /** The progress base is a static image on top of which the progressing bar is drawn */
  val progressBase: AssetName = AssetName("progress-base")
  val base: Graphic =
    Graphic(0, 0, 480, 32, 3, Material.Textured(AssetName("progress-base")))
  val basePosition: Point = Point(20, 20)

  /** The progress bar image, shown partially according to the progress so far */
  val progressBar: AssetName = AssetName("progress-bar")
  val bar: Graphic =
    Graphic(0, 0, 476, 28, 3, Material.Textured(AssetName("progress-bar")))
  val barPosition: Point = Point(22, 22)

  /** For this example, our computation steps are to factorise a large number to primes */
  class PrimeFactorise(toFactorise: Long) extends MonitoredStep[Unit, List[Long]] {
    val size: Int = 1

    def perform(reference: Unit, current: List[Long]): List[Long] = {
      @tailrec
      def factorFrom(value: Long, factor: Long, found: List[Long]): List[Long] =
        if (value === 1L) found
        else if (value % factor === 0) factorFrom(value / factor, 2, factor :: found)
        else factorFrom(value, factor + 1, found)
      factorFrom(toFactorise, 2, Nil)
    }
  }
  val numbersToFactorise: List[Long] = (7000001L to 7001000L).toList

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(defaultGameConfig)
      .withAssets(
        AssetType.Image(progressBase, AssetPath("assets/progress-base.png")),
        AssetType.Image(progressBar, AssetPath("assets/progress-bar.png"))
      )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel = {
    val steps = numbersToFactorise.map(new PrimeFactorise(_))
    MyGameModel(InterleavedComputation[Unit, List[Long]](Nil, (), steps))
  }

  def initialViewModel(startupData: Unit, model: MyGameModel): MyViewModel =
    MyViewModel(
      ProgressBar(basePosition, barPosition, base, bar, progress = 0.0)
    )

  def updateModel(context: FrameContext[Unit], model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = {
    // If the computations are not yet complete, perform more
    case FrameTick if !model.computation.isCompleted =>
      Outcome(model.copy(computation = model.computation.performMore(context.gameTime)))

    case _ =>
      Outcome(model)
  }

  // Set the progress bar progress to match the interleaved computation progress
  def updateViewModel(
      context: FrameContext[Unit],
      model: MyGameModel,
      viewModel: MyViewModel
  ): GlobalEvent => Outcome[MyViewModel] =
    _ => Outcome(viewModel.copy(progressBar = viewModel.progressBar.update(model.computation.portionCompleted)))

  def present(context: FrameContext[Unit], model: MyGameModel, viewModel: MyViewModel): SceneUpdateFragment =
    SceneUpdateFragment(viewModel.progressBar.draw)
}

/** The game model contains the interleaved computation's current state. */
final case class MyGameModel(computation: InterleavedComputation[Unit, List[Long]])

/** The view model contains the progress bar current state. */
final case class MyViewModel(progressBar: ProgressBar)
