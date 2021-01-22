package indigo.worker

import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.facades.worker.SceneProcessor
import indigo.shared.datatypes.FontInfo
import indigo.shared.animation.Animation
import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.platform.AssetMapping
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.platform.SceneFrameData
import indigo.facades.worker.WorkerConversions._

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
    SceneWorkerGlobal.postMessage(
      new WorkerMessage {
        val _type = "message"
        val data  = "Scene Worker Started"
      }
    )
  }

  def validate(c: Command, name: String): Boolean =
    c.operation.isDefined && c.operation.get == name

  val onMessage: dom.MessageEvent => Unit =
    msg =>
      msg.data.asInstanceOf[Command] match {
        case c if validate(c, "echo") =>
          SceneWorkerGlobal.postMessage(
            new WorkerMessage {
              val _type = "echo"
              val data  = "Echo: " + c.data.asInstanceOf[String]
            }
          )

        case c if validate(c, "purge") =>
          sceneProcessor.purgeTextureAtlasCaches()

        case c if validate(c, "addFont") =>
          fontRegister.register(
            readFontInfo(c.data)
          )

        case c if validate(c, "addFonts") =>
          fontRegister.registerAll(
            c.data.asInstanceOf[js.Array[js.Any]].map(readFontInfo).toArray
          )

        case c if validate(c, "addAnimation") =>
          animationsRegister.register(
            readAnimation(c.data)
          )

        case c if validate(c, "addAnimations") =>
          animationsRegister.registerAll(
            c.data.asInstanceOf[js.Array[js.Any]].map(readAnimation).toArray
          )

        case c if validate(c, "processScene") =>
          val args = readSceneFrameData(c.data)

          val res =
            sceneProcessor.processScene(
              gameTime = args.gameTime,
              scene = args.scene,
              assetMapping = args.assetMapping,
              screenWidth = args.screenWidth,
              screenHeight = args.screenHeight,
              orthographicProjectionMatrix = args.orthographicProjectionMatrix
            )

          SceneWorkerGlobal.postMessage(
            new WorkerMessage {
              val _type = "processed scene"
              val data  = res
            }
          )

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

trait WorkerMessage extends js.Object {
  val _type: js.UndefOr[String]
  val data: js.Any
}
