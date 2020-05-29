package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object InputFieldExample extends IndigoDemo[Unit, Unit, MyViewModel] {

  val config: GameConfig = defaultGameConfig.withClearColor(ClearColor.fromHexString("0xAA3399"))

  val assets: Set[AssetType] = Set(AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png")))

  val fonts: Set[FontInfo] = Set(FontStuff.fontInfo)

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def initialViewModel(startupData: Unit, model: Unit): MyViewModel = {
    val assets =
      InputFieldAssets(
        Text("placeholder", 0, 0, 0, FontStuff.fontKey).alignLeft,
        Graphic(0, 0, 16, 16, 2, Material.Textured(FontStuff.fontName)).withCrop(188, 78, 14, 23).withTint(0, 0, 1)
      )

    MyViewModel(
      InputField("Single line", assets).makeSingleLine.moveTo(Point(10, 10)),
      InputField("Multi\nline", assets).makeMultiLine.moveTo(Point(10, 50))
    )
  }

  def updateModel(context: FrameContext, model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext, model: Unit, viewModel: MyViewModel): Outcome[MyViewModel] =
    Outcome(
      viewModel.copy(
        singleLine = viewModel.singleLine.update(context),
        multiLine = viewModel.multiLine.update(context)
      )
    )

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def present(context: FrameContext, model: Unit, viewModel: MyViewModel): SceneUpdateFragment = {

    val single = viewModel.singleLine.draw(
      context.gameTime,
      context.boundaryLocator
    )

    val multi = viewModel.multiLine.draw(
      context.gameTime,
      context.boundaryLocator
    )

    single |+| multi

  }

}

final case class MyViewModel(singleLine: InputField, multiLine: InputField)

object FontStuff {

  val fontKey: FontKey = FontKey("MyFontKey")

  val fontName: AssetName = AssetName("My boxy font")

  val fontInfo: FontInfo =
    FontInfo(fontKey, Material.Textured(fontName), 320, 230, FontChar("?", 93, 52, 23, 23))
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
}
