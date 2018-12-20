package ingidoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._
import indigoexts.automaton._

import scala.util.Random

object AutomataExample extends IndigoGameBasic[Unit, MyGameModel, Unit] {

  import FontStuff._

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(
    AssetType.Image("graphics", "assets/graphics.png"),
    AssetType.Image(fontName, "assets/boxy_font.png")
  )

  val fonts: Set[FontInfo] = Set(fontInfo)

  val animations: Set[Animations] = Set()

  val subSystems: Set[SubSystem] = Set(
    AutomataFarm.empty.add(
      TextAutomaton(
        AutomataPoolKey("points"),
        Text("0", 0, 0, 1, fontKey).alignCenter,
        AutomataLifeSpan(1000),
        List(
          AutomataModifier.MoveTo((_, seed, _) => {
            val diff = 30 * (seed.timeAliveDelta / seed.lifeSpan)
            seed.spawnedAt + Point(0, -diff.toInt)
          }),
          AutomataModifier.ChangeAlpha { (_, seed, originalAlpha) =>
            // Note: There is a shader bug that makes the White part of text not respect alpha correctly.
            originalAlpha * (seed.timeAliveDelta / seed.lifeSpan)
          },
          AutomataModifier.ChangeTint { (_, seed, _) =>
            Tint(1 * (seed.timeAliveDelta / seed.lifeSpan), 0, 0)
          }
        )
      )
    )
  )

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up).withUpAction { () =>
        Option(
          AutomataEvent.ModifyAndSpawn(
            AutomataPoolKey("points"),
            generateLocation(),
            { case t: TextAutomaton => t.changeTextTo(generatePoints()) }
          )
        )
      },
      count = 0
    )

  def generateLocation(): Point =
    Point(Random.nextInt(config.viewport.width - 50) + 25, Random.nextInt(config.viewport.height - 50) + 25)

  def generatePoints(): String =
    (Random.nextInt(10) * 100).toString + "!!"

  def update(gameTime: GameTime, model: MyGameModel): GlobalEvent => UpdatedModel[MyGameModel] = {
    case e: ButtonEvent =>
      UpdatedModel(
        model.copy(
          button = model.button.update(e)
        )
      )

    case FrameTick =>
      UpdatedModel(model).addGlobalEvents(AutomataEvent.Cull)

    case _ =>
      UpdatedModel(model)
  }

  def initialViewModel(startupData: Unit): MyGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents): UpdatedViewModel[Unit] =
    UpdatedViewModel(())

  def present(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    model.button
      .draw(
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2),
        frameInputEvents = frameInputEvents,
        buttonAssets = ButtonAssets(
          up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
          over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
          down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
        )
      )
      .toSceneUpdateFragment
      .addGameLayerNodes(Text("click to win!", 30, 10, 1, fontKey))

}

final case class MyGameModel(button: Button, count: Int)

object FontStuff {

  val fontKey: FontKey = FontKey("MyFontKey")
  val fontName: String = "My boxy font"

  val fontInfo: FontInfo =
    FontInfo(fontKey, fontName, 320, 230, FontChar("?", 93, 52, 23, 23))
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
