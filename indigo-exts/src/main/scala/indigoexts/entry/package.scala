package indigoexts

import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.shared.{AssetType, GameConfig}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object entry {

  implicit val emptyConfigAsync: Future[Option[GameConfig]] = Future(None)

  implicit val emptyAssetsAsync: Future[Set[AssetType]] = Future(Set())

  val defaultGameConfig: GameConfig = indigo.shared.GameConfig.default

  val noRender: SceneUpdateFragment = indigo.gameengine.scenegraph.SceneUpdateFragment.empty

}
