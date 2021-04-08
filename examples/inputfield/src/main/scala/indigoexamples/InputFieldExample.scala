package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object InputFieldExample extends IndigoDemo[Unit, Unit, Unit, MyViewModel] {

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome(
      BootResult
        .noData(defaultGameConfig.withClearColor(RGBA.fromHexString("0xAA3399")))
        .withAssets(AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png")))
        .withFonts(FontStuff.fontInfo)
    )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def initialViewModel(startupData: Unit, model: Unit): Outcome[MyViewModel] =
    Outcome {
      val assets =
        InputFieldAssets(
          Text("placeholder", 0, 0, 0, FontStuff.fontKey, Material.Bitmap(FontStuff.fontName)).alignLeft,
          Graphic(0, 0, 16, 16, 2, Material.Bitmap(FontStuff.fontName))
            .withCrop(188, 78, 14, 23)
            .modifyMaterial {
              case m: Material.ImageEffects => m.withTint(RGBA.Blue)
              case m                        => m
            }
        )

      MyViewModel(
        InputField("Single line", assets).makeSingleLine.moveTo(Point(10, 10)),
        InputField("Multi\nline", assets).makeMultiLine
          .withKey(BindingKey("test input field")) // On change events are only emitted with a key is set
          .moveTo(Point(10, 50))
          .withFocusActions(MyInputFieldEvent("got focus"))
          .withLoseFocusActions(MyInputFieldEvent("lost focus"))
      )
    }

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] = {
    case InputFieldChange(key, updatedText) =>
      println(s"Updated '${key.value}' to: $updatedText")
      Outcome(model)

    case MyInputFieldEvent(message) =>
      println(s"$message")
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[Unit], model: Unit, viewModel: MyViewModel): GlobalEvent => Outcome[MyViewModel] = {
    case FrameTick =>
      for {
        single <- viewModel.singleLine.update(context)
        multi  <- viewModel.multiLine.update(context)
      } yield viewModel.copy(singleLine = single, multiLine = multi)

    case _ =>
      Outcome(viewModel)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def present(context: FrameContext[Unit], model: Unit, viewModel: MyViewModel): Outcome[SceneUpdateFragment] = {

    val single = viewModel.singleLine.draw(
      context.gameTime,
      context.boundaryLocator
    )

    val multi = viewModel.multiLine.draw(
      context.gameTime,
      context.boundaryLocator
    )

    Outcome(SceneUpdateFragment(single ++ multi))

  }

}

final case class MyViewModel(singleLine: InputField, multiLine: InputField)

final case class MyInputFieldEvent(message: String) extends GlobalEvent

object FontStuff {

  val fontKey: FontKey = FontKey("MyFontKey")

  val fontName: AssetName = AssetName("My boxy font")

  val fontInfo: FontInfo =
    FontInfo(fontKey, 320, 230, FontChar("?", 93, 52, 23, 23))
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
