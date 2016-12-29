package purple.gameengine

import org.scalajs.dom
import org.scalajs.dom.html
import purple.renderer._

import scala.scalajs.js.JSApp

trait GameEngine[GameModel] extends JSApp {

  def config: GameConfig

  def imageAssets: Set[ImageAsset]

  def initialModel: GameModel

  def updateModel(previousState: GameModel): GameModel

  def updateView(currentState: GameModel): SceneGraphNode

  def main(): Unit = {

    val loadedImageAssets: List[LoadedImageAsset] = imageAssets.toList.map(loadAsset)

    dom.window.requestAnimationFrame(loop(Renderer(RendererConfig(Viewport(config.viewport.width, config.viewport.height)), loadedImageAssets)))

//    val renderer = Renderer(RendererConfig())
//
//    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
//    image.src = "Sprite-0001.png"
//    //    image.src = "f-texture.png"
//    image.onload = (_: dom.Event) => {
//
//      implicit val cnc: ContextAndCanvas = renderer.createCanvas("canvas", viewportWidth, viewportHeight)
//
//      renderer.addRectangle(Rectangle2D(0, 0, 64, 64, image))
//      renderer.addRectangle(Rectangle2D(32, 32, 64, 64, image))
//      renderer.addRectangle(Rectangle2D(viewportWidth - 64, viewportHeight - 64, 64, 64, image))
//
//      renderer.drawScene
//    }
  }

  private def loadAsset(imageAsset: ImageAsset): LoadedImageAsset = {
    var loadedImageAsset: LoadedImageAsset = null

    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    image.src = imageAsset.path
    image.onload = (_: dom.Event) => {
      loadedImageAsset = LoadedImageAsset(imageAsset.name, image)
    }

    loadedImageAsset
  }

  private var state: GameModel = initialModel

  private def loop(renderer: Renderer)(timeDelta: Double): Unit = {
    state = updateModel(state)

    val sceneGraph = updateView(state)

    renderer.drawSceneOnce()

    dom.window.requestAnimationFrame(loop(renderer))
  }

}

case class GameConfig(viewport: GameViewport)

case class GameViewport(width: Int, height: Int)

object SceneGraphNode {
  def empty: SceneGraphNode = SceneGraphNodeBranch(Nil)
}
sealed trait SceneGraphNode
case class SceneGraphNodeBranch(children: List[SceneGraphNode]) extends SceneGraphNode
case class SceneGraphNodeLeaf(x: Int, y: Int, width: Int, height: Int, imageAssetRef: ImageAsset) extends SceneGraphNode

