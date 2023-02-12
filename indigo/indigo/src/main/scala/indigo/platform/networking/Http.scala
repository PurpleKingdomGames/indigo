package indigo.platform.networking

import indigo.platform.events.GlobalEventStream
import indigo.shared.networking.HttpReceiveEvent.HttpError
import indigo.shared.networking.HttpReceiveEvent.HttpResponse
import indigo.shared.networking.HttpRequest
import org.scalajs.dom
import org.scalajs.dom.XMLHttpRequest

object Http {

  private given CanEqual[Option[String], Option[String]] = CanEqual.derived

  def processRequest(request: HttpRequest, globalEventStream: GlobalEventStream): Unit =
    try {

      val xhr = new XMLHttpRequest

      xhr.open(request.method, request.fullUrl)

      request.headers.foreach { h =>
        xhr.setRequestHeader(h._1, h._2)
      }

      xhr.onload = (_: dom.Event) => {

        val parsedHeaders: Map[String, String] =
          xhr
            .getAllResponseHeaders()
            .split("\\r\\n")
            .map(p =>
              p.split(':').map(_.trim).toList match {
                case Nil =>
                  None
                case x :: y :: Nil =>
                  Option((x, y))

                case x :: Nil =>
                  Option((x, ""))

                case _ =>
                  None

              }
            )
            .collect { case Some(t) => t }
            .foldLeft(Map.empty[String, String])(_ + _)

        val body = ((str: String) => if (str.isEmpty) None else Option(str))(xhr.responseText)

        globalEventStream.pushGlobalEvent(
          HttpResponse(
            status = xhr.status,
            headers = parsedHeaders,
            body = body
          )
        )
      }

      xhr.onerror = (_: dom.Event) => globalEventStream.pushGlobalEvent(HttpError)

      request.body match {
        case Some(b) =>
          xhr.send(b)

        case None =>
          xhr.send()
      }

    } catch {
      case _: Throwable =>
        globalEventStream.pushGlobalEvent(HttpError)
    }

}
