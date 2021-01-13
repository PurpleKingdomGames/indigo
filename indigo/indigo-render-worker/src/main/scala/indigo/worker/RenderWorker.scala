package indigo.worker

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation._

import scala.annotation.nowarn

@JSExportTopLevel("RenderWorker")
object RenderWorker {
  @JSExport
  def main(): Unit = {
    RenderWorkerGlobal.addEventListener("message", onMessage)
    RenderWorkerGlobal.postMessage(s"Started")
  }

  val onMessage: dom.MessageEvent => Unit = msg => {
    RenderWorkerGlobal.postMessage(s"[Worker] onMessage: " + msg.data.toString)
    val message =
      msg.data.asInstanceOf[Command] match {
        case c if c.operation.isDefined && c.operation.get == "echo" =>
          val s = c.data.asInstanceOf[String]
          s"Echo: $s"

        case c if c.operation.isDefined && c.operation.get == "boom" =>
          val s = c.data.asInstanceOf[Boom]
          s"${s.boom} - ${s.fish}"

        case c if c.operation.isDefined && c.operation.get == "foo" =>
          val s = c.data.asInstanceOf[Foo]
          s"${s.bar}"

        case c if c.operation.isDefined =>
          s"Unknown operation: ${c.operation.get}"

        case _ =>
          s"Unexpected message: " + msg.data.toString // Should use JSON.stringify
      }

    RenderWorkerGlobal.postMessage(s"Received: $message")
  }
}

object RenderWorkerGlobal {

  @nowarn
  @js.native
  @JSGlobal("addEventListener")
  def addEventListener(`type`: String, f: js.Function): Unit = js.native

  @nowarn
  @js.native
  @JSGlobal("postMessage")
  def postMessage(data: js.Any): Unit = js.native

}

trait Boom extends js.Object {
  val boom: Boolean
  val fish: js.UndefOr[String]
}

trait Foo extends js.Object {
  val bar: String
}

trait Command extends js.Object {
  val operation: js.UndefOr[String]
  val data: js.Object
}
