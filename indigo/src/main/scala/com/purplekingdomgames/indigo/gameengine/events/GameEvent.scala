package com.purplekingdomgames.indigo.gameengine.events

import com.purplekingdomgames.indigo.gameengine.constants.KeyCode
import com.purplekingdomgames.indigo.gameengine.scenegraph.Volume
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point
import com.purplekingdomgames.indigo.networking.{HttpMethod, WebSocketConfig, WebSocketId}

sealed trait GameEvent

case object FrameTick extends GameEvent

sealed trait MouseEvent extends GameEvent {
  val x: Int
  val y: Int
  def position: Point = Point(x, y)
}
object MouseEvent {
  case class Click(x: Int, y: Int)     extends MouseEvent
  case class MouseUp(x: Int, y: Int)   extends MouseEvent
  case class MouseDown(x: Int, y: Int) extends MouseEvent
  case class Move(x: Int, y: Int)      extends MouseEvent
}

sealed trait KeyboardEvent extends GameEvent {
  val keyCode: KeyCode
}
object KeyboardEvent {
  case class KeyUp(keyCode: KeyCode)    extends KeyboardEvent
  case class KeyDown(keyCode: KeyCode)  extends KeyboardEvent
  case class KeyPress(keyCode: KeyCode) extends KeyboardEvent
}

trait ViewEvent extends GameEvent

case class PlaySound(assetRef: String, volume: Volume) extends ViewEvent

sealed trait NetworkSendEvent    extends ViewEvent
sealed trait NetworkReceiveEvent extends GameEvent

// WebSockets

sealed trait WebSocketEvent {
  def giveId: Option[WebSocketId] =
    this match {
      case WebSocketEvent.ConnectOnly(config) =>
        Option(config.id)

      case WebSocketEvent.Open(_, config) =>
        Option(config.id)

      case WebSocketEvent.Send(_, config) =>
        Option(config.id)

      case WebSocketEvent.Receive(id, _) =>
        Option(id)

      case WebSocketEvent.Error(id, _) =>
        Option(id)

      case WebSocketEvent.Close(id) =>
        Option(id)

      case _ =>
        None
    }
}
object WebSocketEvent {
  // Send
  case class ConnectOnly(webSocketConfig: WebSocketConfig)           extends WebSocketEvent with NetworkSendEvent
  case class Open(message: String, webSocketConfig: WebSocketConfig) extends WebSocketEvent with NetworkSendEvent
  case class Send(message: String, webSocketConfig: WebSocketConfig) extends WebSocketEvent with NetworkSendEvent

  // Receive
  case class Receive(webSocketId: WebSocketId, message: String) extends WebSocketEvent with NetworkReceiveEvent
  case class Error(webSocketId: WebSocketId, error: String)     extends WebSocketEvent with NetworkReceiveEvent
  case class Close(webSocketId: WebSocketId)                    extends WebSocketEvent with NetworkReceiveEvent
}

// Http

sealed trait HttpReceiveEvent extends NetworkReceiveEvent
object HttpReceiveEvent {
  case object HttpError                                                                    extends HttpReceiveEvent
  case class HttpResponse(status: Int, headers: Map[String, String], body: Option[String]) extends HttpReceiveEvent
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
  case class POST(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String])
      extends HttpRequest {
    val method: String = HttpMethod.POST
  }
  case class PUT(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String])
      extends HttpRequest {
    val method: String = HttpMethod.PUT
  }
  case class DELETE(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String])
      extends HttpRequest {
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
