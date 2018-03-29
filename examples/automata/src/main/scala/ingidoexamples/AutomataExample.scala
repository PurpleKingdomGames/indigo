package ingidoexamples

import com.purplekingdomgames.indigo._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.{GameTime, StartupErrors, events}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, SceneGraphUpdate, SceneUpdateFragment, Text}
import com.purplekingdomgames.indigoexts.automata._
import com.purplekingdomgames.indigoexts.ui._
import com.purplekingdomgames.shared.{AssetType, GameConfig, ImageAsset}

object AutomataExample extends IndigoGameBasic[Unit, MyGameModel] {

  val fontName: String = "My boxy font"

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(
    ImageAsset("graphics", "assets/graphics.png"),
    ImageAsset(fontName, "assets/boxy_font.png")
  )

  def setup(assetCollection: AssetCollection): Either[StartupErrors, Unit] = {

    AutomataFarm.register(
      TextAutomaton(
        AutomataPoolKey("points"),
        Text("1000", 0, 0, 1, fontInfo).alignCenter,
        AutomataLifeSpan(1000),
        List(
          AutomataModifier.MoveTo((_, seed, _) => {
            val start = Point.tuple2ToPoint(config.viewport.center)
            val diff = 30 * (seed.timeAliveDelta / seed.lifeSpan)

            start + Point(0, -diff.toInt)
          }),
          AutomataModifier.ChangeAlpha((_, seed, originalAlpha) =>
            originalAlpha * (seed.timeAliveDelta / seed.lifeSpan)
          )
        )
      )
    )

    Right(())
  }

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up).withUpAction { () =>
        Option(AutomataEvent.Spawn(AutomataPoolKey("points"), Point(0, 0)))
      },
      count = 0
    )

  def update(gameTime: GameTime, model: MyGameModel): events.GameEvent => MyGameModel = {
    case e: ButtonEvent =>
      model.copy(
        button = model.button.update(e)
      )

    case e: AutomataEvent =>
      AutomataFarm.update(gameTime, e)
      model

    case _ =>
      model
  }

  def present(gameTime: GameTime, model: MyGameModel, frameInputEvents: events.FrameInputEvents): SceneUpdateFragment = {
    val button: ButtonViewUpdate = model.button.draw(
      bounds = Rectangle(10, 10, 16, 16),
      depth = Depth(2),
      frameInputEvents = frameInputEvents,
      buttonAssets = ButtonAssets(
        up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
      )
    )

    SceneGraphUpdate(
      List(button.buttonGraphic, Text("click to win!", 30, 10, 1, fontInfo))
        ++ AutomataFarm.render(gameTime),
      button.buttonEvents
    )

    button
      .toSceneUpdateFragment
      .addGameLayerNodes(AutomataFarm.render(gameTime))
  }

  val fontInfo: FontInfo =
    FontInfo(fontName, 320, 230, FontChar("?", 93, 52, 23, 23))
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

case class MyGameModel(button: Button, count: Int)