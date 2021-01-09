package indigo.worker

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation._

@JSExportTopLevel("RenderWorker")
object RenderWorker {
  @JSExport
  def main(): Unit = {
    RenderWorkerGlobal.addEventListener("message", onMessage _)
    RenderWorkerGlobal.postMessage(s"Started")
  }

  var count = 0

  def onMessage(msg: dom.MessageEvent) = {
    count += 1
    val s = msg.data.asInstanceOf[Boom]
    RenderWorkerGlobal.postMessage(s"Received: ${s.boom} - ${s.fish}, message count: $count")
  }
}

object RenderWorkerGlobal {

  @js.native
  @JSGlobal("addEventListener")
  def addEventListener(`type`: String, f: js.Function): Unit = js.native

  @js.native
  @JSGlobal("postMessage")
  def postMessage(data: js.Any): Unit                        = js.native
}

trait Boom extends js.Object {
  val boom: Boolean
  val fish: js.UndefOr[String]
}
