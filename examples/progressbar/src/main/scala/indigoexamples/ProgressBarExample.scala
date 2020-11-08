package indigoexamples

import indigo._
import indigo.shared.EqualTo._
import indigoextras.interleaved.{MonitoredStep, LongCompute}
import indigoextras.ui.ProgressBar

import scala.annotation.tailrec
import scala.scalajs.js.annotation._
import indigoextras.subsystems.FPSCounter

/**
  * Presents an example of a progress bar being updated by an interleaved computation (a computation
  * whose steps are performed between frames).
  */
@JSExportTopLevel("IndigoGame")
object ProgressBarExample extends IndigoDemo[Unit, Unit, MyGameModel, MyViewModel] {
  val eventFilters: EventFilters = EventFilters.Default

  // val numbersToFactorise: List[Long] = (7000001L to 7001000L).toList // Slow ~10fps
  val numbersToFactorise: List[Long] = (700001L to 701000L).toList // Acceptable? ~40fps
  // val numbersToFactorise: List[Long] = (70001L to 71000L).toList // Fast ~60fps

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(defaultGameConfig)
      .withAssets(Assets.assets)
      .withFonts(FontDetails.fontInfo)
      .withSubSystems(FPSCounter(FontDetails.fontKey, Point(10, 60), 60))

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      LongCompute[List[Long]](
        initialValue = Nil,
        steps = numbersToFactorise.map(new PrimeFactorise(_))
      )
    )

  def initialViewModel(startupData: Unit, model: MyGameModel): MyViewModel =
    MyViewModel(
      ProgressBar(
        Assets.basePosition,
        Assets.barPosition,
        Assets.base,
        Assets.bar,
        progress = 0.0
      )
    )

  def updateModel(context: FrameContext[Unit], model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = {
    // If the computations are not yet complete, perform more
    case FrameTick if !model.computation.isComplete =>
      Outcome(model.copy(computation = model.computation.update(context.gameTime)))

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
      .addUiLayerNodes(
        Text("Units to attempt: " + model.computation.unitsToAttempt.toString(), 10, 100, 1, FontDetails.fontKey),
        Text("Remaining: " + model.computation.sizeRemaining.toString(), 10, 125, 1, FontDetails.fontKey),
        Text("Completed: " + model.computation.sizeCompleted.toString(), 10, 150, 1, FontDetails.fontKey)
      )
}

/** The game model contains the interleaved computation's current state. */
final case class MyGameModel(computation: LongCompute[Unit, List[Long]])

/** The view model contains the progress bar current state. */
final case class MyViewModel(progressBar: ProgressBar)

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

object Assets {

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

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName(FontDetails.fontName), AssetPath("assets/boxy_font.png")),
      AssetType.Image(progressBase, AssetPath("assets/progress-base.png")),
      AssetType.Image(progressBar, AssetPath("assets/progress-bar.png"))
    )

}

object FontDetails {

  val fontName: String = "My boxy font"

  def fontKey: FontKey = FontKey("My Font")

  def fontInfo: FontInfo =
    FontInfo(fontKey, Material.Textured(AssetName(fontName)), 320, 230, FontChar("?", 93, 52, 23, 23))
      .addChar(FontChar("A", 3, 78, 23, 23))
      .addChar(FontChar("B", 26, 78, 23, 23))
      .addChar(FontChar("C", 50, 78, 23, 23))
      .addChar(FontChar("D", 73, 78, 23, 23))
      .addChar(FontChar("E", 96, 78, 23, 23))
      .addChar(FontChar("F", 119, 78, 23, 23))
      .addChar(FontChar("G", 142, 78, 23, 23))
      .addChar(FontChar("H", 165, 78, 23, 23))
      .addChar(FontChar("I", 188, 78, 15, 23))
      .addChar(FontChar("J", 202, 78, 23, 23))
      .addChar(FontChar("K", 225, 78, 23, 23))
      .addChar(FontChar("L", 248, 78, 23, 23))
      .addChar(FontChar("M", 271, 78, 23, 23))
      .addChar(FontChar("N", 3, 104, 23, 23))
      .addChar(FontChar("O", 29, 104, 23, 23))
      .addChar(FontChar("P", 54, 104, 23, 23))
      .addChar(FontChar("Q", 75, 104, 23, 23))
      .addChar(FontChar("R", 101, 104, 23, 23))
      .addChar(FontChar("S", 124, 104, 23, 23))
      .addChar(FontChar("T", 148, 104, 23, 23))
      .addChar(FontChar("U", 173, 104, 23, 23))
      .addChar(FontChar("V", 197, 104, 23, 23))
      .addChar(FontChar("W", 220, 104, 23, 23))
      .addChar(FontChar("X", 248, 104, 23, 23))
      .addChar(FontChar("Y", 271, 104, 23, 23))
      .addChar(FontChar("Z", 297, 104, 23, 23))
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
      .addChar(FontChar("?", 93, 52, 23, 23))
      .addChar(FontChar("!", 3, 0, 15, 23))
      .addChar(FontChar(".", 286, 0, 15, 23))
      .addChar(FontChar(",", 248, 0, 15, 23))
      .addChar(FontChar(" ", 145, 52, 23, 23))
      .addChar(FontChar(":", 3, 52, 12, 23))
}
