package purple.gameengine

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.HTMLImageElement
import org.scalajs.dom.Event
import purple.renderer._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.scalajs.js.JSApp

trait GameEngine[GameModel] extends JSApp {

  def config: GameConfig

  def imageAssets: Set[ImageAsset]

  def initialModel: GameModel

  def updateModel(previousState: GameModel): GameModel

  def updateView(currentState: GameModel): SceneGraphNode

  def main(): Unit = {

//    val loadedImageAssets: List[LoadedImageAsset] =
    Future.sequence(imageAssets.toList.map(loadAsset)).foreach { loadedImageAssets =>
      val renderer: Renderer = Renderer(
        RendererConfig(
          viewport = Viewport(config.viewport.width, config.viewport.height),
          clearColor = ClearColor(0, 0, 0.3d, 1)
        ),
        loadedImageAssets
      )

      dom.window.requestAnimationFrame(loop(renderer))
    }


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

  private def onLoadFuture(image: HTMLImageElement): Future[HTMLImageElement] = {
    if (image.complete) {
      Future.successful(image)
    } else {
      val p = Promise[HTMLImageElement]()
      image.onload = { (_: Event) =>
        p.success(image)
      }
      p.future
    }
  }

  private def loadAsset(imageAsset: ImageAsset): Future[LoadedImageAsset] = {

    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    image.src = imageAsset.path

//    val loaded = Await.result(onLoadFuture(image), 5.second)

    onLoadFuture(image).map(i => LoadedImageAsset(imageAsset.name, i))
  }

  private var state: GameModel = initialModel

  private def loop(renderer: Renderer)(timeDelta: Double): Unit = {
    state = updateModel(state)

    val sceneGraph: SceneGraphNode = updateView(state)

    renderer.drawSceneOnce(
      sceneGraph.flatten(Nil).map { leaf =>
        DisplayObject(
          x = leaf.x,
          y = leaf.y,
          width = leaf.width,
          height = leaf.height,
          imageRef = leaf.imageAssetRef
        )
      }.sortBy(d => d.imageRef)
    )

    dom.window.requestAnimationFrame(loop(renderer))
  }

}

case class GameConfig(viewport: GameViewport)

case class GameViewport(width: Int, height: Int)

object SceneGraphNode {
  def empty: SceneGraphNode = SceneGraphNodeBranch(Nil)
}
sealed trait SceneGraphNode {

  def flatten(acc: List[SceneGraphNodeLeaf]): List[SceneGraphNodeLeaf] = {
    this match {
      case l: SceneGraphNodeLeaf => l :: acc
      case b: SceneGraphNodeBranch =>
        b.children.flatMap(n => n.flatten(Nil)) ++ acc
    }
  }

}
case class SceneGraphNodeBranch(children: List[SceneGraphNode]) extends SceneGraphNode
case class SceneGraphNodeLeaf(x: Int, y: Int, width: Int, height: Int, imageAssetRef: String) extends SceneGraphNode

