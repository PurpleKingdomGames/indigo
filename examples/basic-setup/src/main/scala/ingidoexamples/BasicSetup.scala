package ingidoexamples

import com.purplekingdomgames.indigo._
import com.purplekingdomgames.indigo.gameengine.events
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneUpdateFragment

object BasicSetup extends IndigoGameBasic[MyStartUpData, MyGameModel] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, MyStartUpData] =
    Right(MyStartUpData())

  def initialModel(startupData: MyStartUpData): MyGameModel =
    MyGameModel()

  def update(gameTime: GameTime, model: MyGameModel): events.GameEvent => MyGameModel = _ =>
    model

  def present(gameTime: GameTime, model: MyGameModel, frameInputEvents: events.FrameInputEvents): SceneUpdateFragment =
    noRender
}

// What does your game need to start? E.g. Parsing a level description file
case class MyStartUpData()

// Your game model is anything you like!
case class MyGameModel()