package indigo.worker

import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.platform.SceneProcessor
import indigo.shared.datatypes.FontInfo
import indigo.shared.animation.Animation
import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.platform.AssetMapping
import indigo.shared.datatypes.mutable.CheapMatrix4

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation._

import scala.annotation.nowarn

@JSExportTopLevel("SceneWorker")
object SceneWorker {

  val animationsRegister: AnimationsRegister =
    new AnimationsRegister()
  val fontRegister: FontRegister =
    new FontRegister()
  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(animationsRegister, fontRegister)
  val sceneProcessor: SceneProcessor =
    new SceneProcessor(boundaryLocator, animationsRegister, fontRegister)

  @JSExport
  def main(): Unit = {
    SceneWorkerGlobal.addEventListener("message", onMessage)
    SceneWorkerGlobal.postMessage(s"Scene Worker Started")
  }

  def validate(c: Command, name: String): Boolean =
    c.operation.isDefined && c.operation.get == name

  val onMessage: dom.MessageEvent => Unit =
    msg =>
      msg.data.asInstanceOf[Command] match {
        case c if validate(c, "echo") =>
          SceneWorkerGlobal.postMessage("Echo: " + c.data.asInstanceOf[String])

        case c if validate(c, "addFont") =>
          fontRegister.register(c.data.asInstanceOf[FontInfo])

        case c if validate(c, "addFonts") =>
          fontRegister.registerAll(c.data.asInstanceOf[Array[FontInfo]])

        case c if validate(c, "addAnimation") =>
          animationsRegister.register(c.data.asInstanceOf[Animation])

        case c if validate(c, "addAnimations") =>
          animationsRegister.registerAll(c.data.asInstanceOf[Array[Animation]])

        case c if validate(c, "processScene") =>
          val args = c.data.asInstanceOf[SceneFrameData]

          val res =
            sceneProcessor.processScene(
              gameTime = args.gameTime,
              scene = args.scene,
              assetMapping = args.assetMapping,
              screenWidth = args.screenWidth,
              screenHeight = args.screenHeight,
              orthographicProjectionMatrix = args.orthographicProjectionMatrix
            )

          SceneWorkerGlobal.postMessage(res.asInstanceOf[js.Any])

        case c if c.operation.isDefined =>
          println(s"Scene Worker - Unknown operation: ${c.operation.get}")
          ()

        case _ =>
          // Should use JSON.stringify?
          println(s"Scene Worker - Unexpected message: " + msg.data.toString)
          ()
      }
}

object SceneWorkerGlobal {

  @nowarn
  @js.native
  @JSGlobal("addEventListener")
  def addEventListener(`type`: String, f: js.Function): Unit = js.native

  @nowarn
  @js.native
  @JSGlobal("postMessage")
  def postMessage(data: js.Any): Unit = js.native

}

trait Command extends js.Object {
  val operation: js.UndefOr[String]
  val data: js.Object
}

trait SceneFrameData extends js.Object {
  val gameTime: GameTime
  val scene: SceneUpdateFragment
  val assetMapping: AssetMapping
  val screenWidth: Double
  val screenHeight: Double
  val orthographicProjectionMatrix: CheapMatrix4
}
