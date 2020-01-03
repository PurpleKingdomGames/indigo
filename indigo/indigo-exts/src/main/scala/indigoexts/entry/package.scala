package indigoexts

import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.assets.AssetType
import indigo.shared.config.GameConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object entry {

  implicit val emptyConfigAsync: Future[Option[GameConfig]] = Future(None)

  implicit val emptyAssetsAsync: Future[Set[AssetType]] = Future(Set())

  val defaultGameConfig: GameConfig = indigo.shared.config.GameConfig.default

  val noRender: SceneUpdateFragment = indigo.shared.scenegraph.SceneUpdateFragment.empty

}
