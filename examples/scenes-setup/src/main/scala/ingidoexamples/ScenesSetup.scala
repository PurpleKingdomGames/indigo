package ingidoexamples

import indigo._
import indigoexts._
import indigoexts.scenes._

object ScenesSetup extends IndigoGameWithScenes[MyStartUpData, MyGameModel, MyViewModel] {

  val scenes: ScenesList[MyGameModel, MyViewModel, _, _] =
    SceneA :: SceneB :: ScenesNil[MyGameModel, MyViewModel]()

  val initialScene: Option[SceneName] = SceneA.name

  val config: GameConfig = defaultGameConfig.withClearColor(ClearColor.fromHexString("0xAA3399"))

  val assets: Set[AssetType] = Set(AssetType.Image(FontStuff.fontName, "assets/boxy_font.png"))

  val fonts: Set[FontInfo] = Set(FontStuff.fontInfo)

  val animations: Set[Animations] = Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, MyStartUpData] =
    Right(MyStartUpData("Scene A!", "Scene B?"))

  def initialModel(startupData: MyStartUpData): MyGameModel =
    MyGameModel(
      sceneA = MessageA(startupData.messageA),
      sceneB = MessageB(startupData.messageB)
    )

  def initialViewModel(startupData: MyStartUpData): MyGameModel => MyViewModel =
    _ => MyViewModel()
}

// There is no relevant entry in the ViewModel for either scene, so we've just left it as Unit.
object SceneA extends Scene[MyGameModel, MyViewModel, MessageA, Unit] {
  val name: SceneName = SceneName("A")

  val sceneModelLens: Lens[MyGameModel, MessageA] =
    Lens(
      model => model.sceneA,
      (model, newMessage) => model.copy(sceneA = newMessage)
    )

  // Nothing to do
  val sceneViewModelLens: Lens[MyViewModel, Unit] =
    Lens.identity(())

  // Nothing to do
  def updateSceneModel(gameTime: GameTime, sceneModel: MessageA): GameEvent => MessageA =
    _ => sceneModel

  // Nothing to do
  def updateSceneViewModel(gameTime: GameTime, sceneModel: MessageA, sceneViewModel: Unit, frameInputEvents: FrameInputEvents): Unit = ()

  // Show some text
  // When the user clicks anywhere in the screen, trigger an event to jump to the other scene.
  def updateSceneView(gameTime: GameTime, sceneModel: MessageA, sceneViewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val events: List[ViewEvent] =
      if (frameInputEvents.wasMouseClickedWithin(Rectangle(0, 0, 550, 400))) List(JumpToScene(SceneB.name))
      else Nil

    val text: Text = Text(sceneModel.value, 20, 20, 1, FontStuff.fontKey)

    SceneUpdateFragment.empty
      .addGameLayerNodes(text)
      .addViewEvents(events)
  }

}

// There is no relevant entry in the ViewModel for either scene, so we've just left it as Unit.
object SceneB extends Scene[MyGameModel, MyViewModel, MessageB, Unit] {

  val name: SceneName = SceneName("B")

  val sceneModelLens: Lens[MyGameModel, MessageB] =
    Lens(
      model => model.sceneB,
      (model, newMessage) => model.copy(sceneB = newMessage)
    )

  // Nothing to do
  val sceneViewModelLens: Lens[MyViewModel, Unit] =
    Lens.identity(())

  // Nothing to do
  def updateSceneModel(gameTime: GameTime, sceneModel: MessageB): GameEvent => MessageB =
    _ => sceneModel

  // Nothing to do
  def updateSceneViewModel(gameTime: GameTime, sceneModel: MessageB, sceneViewModel: Unit, frameInputEvents: FrameInputEvents): Unit = ()

  // Show some text
  // When the user clicks anywhere in the screen, trigger an event to jump to the other scene.
  def updateSceneView(gameTime: GameTime, sceneModel: MessageB, sceneViewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val events: List[ViewEvent] =
      if (frameInputEvents.wasMouseClickedWithin(Rectangle(0, 0, 550, 400))) List(JumpToScene(SceneA.name))
      else Nil

    val text: Text = Text(sceneModel.value, 20, 20, 1, FontStuff.fontKey)

    SceneUpdateFragment.empty
      .addGameLayerNodes(text)
      .addViewEvents(events)
  }
}

case class MyStartUpData(messageA: String, messageB: String)
case class MyGameModel(sceneA: MessageA, sceneB: MessageB)
case class MyViewModel()

case class MessageA(value: String)
case class MessageB(value: String)

object FontStuff {

  val fontName: String = "My boxy font"

  def fontKey: FontKey = FontKey("My Font")

  def fontInfo: FontInfo =
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
