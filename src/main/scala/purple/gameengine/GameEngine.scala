package purple.gameengine

import org.scalajs.dom
import org.scalajs.dom.{Event, html}
import org.scalajs.dom.raw.HTMLImageElement
import purple.renderer._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js.JSApp

trait GameEngine[GameModel] extends JSApp {

  def config: GameConfig

  def imageAssets: Set[ImageAsset]

  def initialModel: GameModel

  def updateModel(timeDelta: Double, previousState: GameModel): GameModel

  def updateView(currentState: GameModel): SceneGraphNode

  def main(): Unit = {

    Future.sequence(imageAssets.toList.map(loadAsset)).foreach { loadedImageAssets =>
      val renderer: Renderer = Renderer(
        RendererConfig(
          viewport = Viewport(config.viewport.width, config.viewport.height),
          clearColor = ClearColor(0, 0, 0.3d, 1)
        ),
        loadedImageAssets
      )

      dom.window.requestAnimationFrame(loop(renderer, 0))
    }

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

    onLoadFuture(image).map(i => LoadedImageAsset(imageAsset.name, i))
  }

  private var state: Option[GameModel] = None

  private def loop(renderer: Renderer, lastUpdateTime: Double)(time: Double): Unit = {
    val timeDelta = time - lastUpdateTime

    if(timeDelta > config.frameRateDeltaMillis) {
      val model = state match {
        case None => initialModel
        case Some(previousState) => updateModel(timeDelta, previousState)
      }

      state = Some(model)

      drawScene(renderer, model)

      dom.window.requestAnimationFrame(loop(renderer, time))
    } else {
      dom.window.requestAnimationFrame(loop(renderer, lastUpdateTime))
    }
  }

  private def drawScene(renderer: Renderer, gameModel: GameModel): Unit = {
    val sceneGraph: SceneGraphNode = updateView(gameModel)

    val displayObjects: List[DisplayObject] =
      sceneGraph.flatten(Nil).map { leaf =>
        DisplayObject(
          x = leaf.x,
          y = leaf.y,
          width = leaf.width,
          height = leaf.height,
          imageRef = leaf.imageAssetRef,
          alpha = leaf.effects.alpha,
          tintR = leaf.effects.tint.r,
          tintG = leaf.effects.tint.g,
          tintB = leaf.effects.tint.b
        )
      }.sortBy(d => d.imageRef)

    renderer.drawScene(displayObjects)
  }

}

case class GameConfig(viewport: GameViewport, frameRate: Int) {
  val frameRateDeltaMillis: Int = 1000 / frameRate
}

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
case class SceneGraphNodeLeaf(x: Int, y: Int, width: Int, height: Int, imageAssetRef: String, effects: SceneGraphNodeLeafEffects) extends SceneGraphNode
case class SceneGraphNodeLeafEffects(alpha: Double, tint: Tint)
case class Tint(r: Double, g: Double, b: Double)
