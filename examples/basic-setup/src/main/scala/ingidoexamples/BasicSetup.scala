package ingidoexamples

import com.purplekingdomgames.indigo._

object BasicSetup extends IndigoGameBasic[MyStartUpData, MyGameModel] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, MyStartUpData] =
    Right(MyStartUpData())

  def initialModel(startupData: MyStartUpData): MyGameModel =
    MyGameModel()

  def update(gameTime: GameTime, model: MyGameModel): GameEvent => MyGameModel = _ =>
    model

  def render(gameTime: GameTime, model: MyGameModel, frameInputEvents: FrameInputEvents): SceneGraphUpdate =
    noRender
}

// What does your game need to start? E.g. Parsing a level description file
case class MyStartUpData()

// Your game model is anything you like!
case class MyGameModel()