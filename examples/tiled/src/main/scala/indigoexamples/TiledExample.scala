package indigoexamples

import indigo._
import indigo.json.Json

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object TiledExample extends IndigoSandbox[Group, Unit] {

  val config: GameConfig =
    defaultGameConfig
      .withViewport(19 * 32, 11 * 32)
      .withClearColor(RGBA.Blue.mix(RGBA.White.withAlpha(0.75)))

  val terrianData: AssetName  = AssetName("terrain-data")
  val terrianImage: AssetName = AssetName("terrain-image")

  val assets: Set[AssetType] =
    Set(
      AssetType.Text(terrianData, AssetPath("assets/terrain.json")),
      AssetType.Image(terrianImage, AssetPath("assets/terrain.png"))
    )

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val shaders: Set[Shader] = Set()

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Group]] =
    Outcome {
      val maybeTiledMap = for {
        j <- assetCollection.findTextDataByName(terrianData)
        t <- Json.tiledMapFromJson(j)
        g <- t.toGroup(terrianImage)
      } yield g

      maybeTiledMap match {
        case None =>
          Startup.Failure("Could not generate TiledMap from data.")

        case Some(tiledMap) =>
          Startup.Success(tiledMap)
      }
    }

  def initialModel(startupData: Group): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Group], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def present(context: FrameContext[Group], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(context.startUpData)
    )
}
