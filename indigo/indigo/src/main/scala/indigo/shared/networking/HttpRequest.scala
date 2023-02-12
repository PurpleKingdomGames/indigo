package indigo.shared.networking

import indigo.shared.events.NetworkReceiveEvent
import indigo.shared.events.NetworkSendEvent

object HttpMethod {
  val GET: String    = "GET"
  val POST: String   = "POST"
  val PUT: String    = "PUT"
  val DELETE: String = "DELETE"
}

sealed trait HttpRequest extends NetworkSendEvent derives CanEqual {
  val params: Map[String, String]
  val url: String
  val headers: Map[String, String]
  val body: Option[String]
  val method: String

  val fullUrl: String = if (params.isEmpty) url else url + "?" + params.toList.map(p => p._1 + "=" + p._2).mkString("&")
}
object HttpRequest {
  final case class GET(url: String, params: Map[String, String], headers: Map[String, String]) extends HttpRequest {
    val body: Option[String] = None
    val method: String       = HttpMethod.GET
  }
  final case class POST(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String])
      extends HttpRequest {
    val method: String = HttpMethod.POST
  }
  final case class PUT(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String])
      extends HttpRequest {
    val method: String = HttpMethod.PUT
  }
  final case class DELETE(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String])
      extends HttpRequest {
    val method: String = HttpMethod.DELETE
  }

  object GET {
    def apply(url: String): GET =
      GET(url, Map.empty[String, String], Map.empty[String, String])
  }

  object POST {
    def apply(url: String, body: String): POST =
      POST(url, Map.empty[String, String], Map.empty[String, String], Option(body))
  }

  object PUT {
    def apply(url: String, body: String): PUT =
      PUT(url, Map.empty[String, String], Map.empty[String, String], Option(body))
  }

  object DELETE {
    def apply(url: String, body: Option[String]): DELETE =
      DELETE(url, Map.empty[String, String], Map.empty[String, String], body)
  }
}

sealed trait HttpReceiveEvent extends NetworkReceiveEvent
object HttpReceiveEvent {
  case object HttpError extends HttpReceiveEvent
  final case class HttpResponse(status: Int, headers: Map[String, String], body: Option[String])
      extends HttpReceiveEvent
}
