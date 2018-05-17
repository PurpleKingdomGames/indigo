package ingidoexamples

import com.purplekingdomgames.indigo._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.FontInfo
import com.purplekingdomgames.indigo.gameengine.{GameTime, StartupErrors, events}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Animations, SceneUpdateFragment}
import com.purplekingdomgames.shared.{AssetType, GameConfig}

object BasicSetup extends IndigoGameBasic[MyStartUpData, MyGameModel] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set()

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animations] = Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, MyStartUpData] =
    Right(MyStartUpData())

  def initialModel(startupData: MyStartUpData): MyGameModel =
    MyGameModel()

  def update(gameTime: GameTime, model: MyGameModel): events.GameEvent => MyGameModel = _ => model

  def present(gameTime: GameTime, model: MyGameModel, frameInputEvents: events.FrameInputEvents): SceneUpdateFragment =
    noRender
}

// What does your game need to start? E.g. Parsing a level description file
case class MyStartUpData()

// Your game model is anything you like!
case class MyGameModel()
