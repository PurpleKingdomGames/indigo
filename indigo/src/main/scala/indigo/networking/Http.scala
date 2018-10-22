package indigo.networking

import indigo.gameengine.events.GlobalEventStream
import indigo.networking.HttpReceiveEvent.{HttpError, HttpResponse}
import org.scalajs.dom
import org.scalajs.dom.XMLHttpRequest

object Http {

  def processRequest(request: HttpRequest)(implicit globalEventStream: GlobalEventStream): Unit =
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
            .map(
              p =>
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

        globalEventStream.pushGameEvent(
          HttpResponse(
            status = xhr.status,
            headers = parsedHeaders,
            body = body
          )
        )
      }

      xhr.onerror = (_: dom.Event) => globalEventStream.pushGameEvent(HttpError)

      request.body match {
        case Some(b) =>
          xhr.send(b)

        case None =>
          xhr.send()
      }

    } catch {
      case _: Throwable =>
        globalEventStream.pushGameEvent(HttpError)
    }

}

object HttpMethod {
  val GET: String    = "GET"
  val POST: String   = "POST"
  val PUT: String    = "PUT"
  val DELETE: String = "DELETE"
}

sealed trait HttpRequest extends NetworkSendEvent {
  val params: Map[String, String]
  val url: String
  val headers: Map[String, String]
  val body: Option[String]
  val method: String

  val fullUrl: String = if (params.isEmpty) url else url + "?" + params.toList.map(p => p._1 + "=" + p._2).mkString("&")
}
object HttpRequest {
  case class GET(url: String, params: Map[String, String], headers: Map[String, String]) extends HttpRequest {
    val body: Option[String] = None
    val method: String       = HttpMethod.GET
  }
  case class POST(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String]) extends HttpRequest {
    val method: String = HttpMethod.POST
  }
  case class PUT(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String]) extends HttpRequest {
    val method: String = HttpMethod.PUT
  }
  case class DELETE(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String]) extends HttpRequest {
    val method: String = HttpMethod.DELETE
  }

  object GET {
    def apply(url: String): GET =
      GET(url, Map(), Map())
  }

  object POST {
    def apply(url: String, body: String): POST =
      POST(url, Map(), Map(), Option(body))
  }

  object PUT {
    def apply(url: String, body: String): PUT =
      PUT(url, Map(), Map(), Option(body))
  }

  object DELETE {
    def apply(url: String, body: Option[String]): DELETE =
      DELETE(url, Map(), Map(), body)
  }
}

sealed trait HttpReceiveEvent extends NetworkReceiveEvent
object HttpReceiveEvent {
  case object HttpError                                                                    extends HttpReceiveEvent
  case class HttpResponse(status: Int, headers: Map[String, String], body: Option[String]) extends HttpReceiveEvent
}
