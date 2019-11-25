package indigoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._

object AutomataExample extends IndigoGameBasic[Unit, MyGameModel, Unit] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(
    AssetType.Image("graphics", "assets/graphics.png"),
    AssetType.Image(FontStuff.fontName, "assets/boxy_font.png")
  )

  val fonts: Set[FontInfo] = Set(FontStuff.fontInfo)

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] =
    Set(Score.automataSubSystem(FontStuff.fontKey))

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up)
    )

  def update(gameTime: GameTime, model: MyGameModel, dice: Dice): GlobalEvent => Outcome[MyGameModel] = {
    case e: ButtonEvent =>
      Outcome(
        model.copy(
          button = model.button.withUpAction { () =>
            List(Score.spawnEvent(Score.generateLocation(config, dice), dice))
          }.update(e)
        )
      )

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): MyGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[Unit] =
    Outcome(())

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
      .addGameLayerNodes(Text("click to win!", 30, 10, 1, FontStuff.fontKey))

}

final case class MyGameModel(button: Button)
