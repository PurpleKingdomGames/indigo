package indigo.shared.networking

import indigo.shared.events.{NetworkSendEvent, NetworkReceiveEvent}
import indigo.shared.EqualTo

final case class WebSocketId(id: String)
object WebSocketId {
  implicit val eq: EqualTo[WebSocketId] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create((a, b) => eqS.equal(a.id, b.id))
  }
}

final case class WebSocketConfig(id: WebSocketId, address: String)
object WebSocketConfig {

  implicit val eq: EqualTo[WebSocketConfig] = {
    val eqId = implicitly[EqualTo[WebSocketId]]
    val eqS  = implicitly[EqualTo[String]]
    EqualTo.create((a, b) => eqId.equal(a.id, b.id) && eqS.equal(a.address, b.address))
  }

}

sealed trait WebSocketReadyState {
  val value: Int
  val isConnecting: Boolean
  val isOpen: Boolean
  val isClosing: Boolean
  val isClosed: Boolean
}
object WebSocketReadyState {

  case object CONNECTING extends WebSocketReadyState {
    val value: Int            = 0
    val isConnecting: Boolean = true
    val isOpen: Boolean       = false
    val isClosing: Boolean    = false
    val isClosed: Boolean     = false
  }

  case object OPEN extends WebSocketReadyState {
    val value: Int            = 1
    val isConnecting: Boolean = false
    val isOpen: Boolean       = true
    val isClosing: Boolean    = false
    val isClosed: Boolean     = false
  }

  case object CLOSING extends WebSocketReadyState {
    val value: Int            = 2
    val isConnecting: Boolean = false
    val isOpen: Boolean       = false
    val isClosing: Boolean    = true
    val isClosed: Boolean     = false
  }

  case object CLOSED extends WebSocketReadyState {
    val value: Int            = 3
    val isConnecting: Boolean = false
    val isOpen: Boolean       = false
    val isClosing: Boolean    = false
    val isClosed: Boolean     = true
  }

  def fromInt(i: Int): WebSocketReadyState =
    i match {
      case 0 => CONNECTING
      case 1 => OPEN
      case 2 => CLOSING
      case 3 => CLOSED
      case _ => CLOSED
    }

}

sealed trait WebSocketEvent {
  def giveId: WebSocketId =
    this match {
      case WebSocketEvent.ConnectOnly(config) =>
        config.id

      case WebSocketEvent.Open(_, config) =>
        config.id

      case WebSocketEvent.Send(_, config) =>
        config.id

      case WebSocketEvent.Receive(id, _) =>
        id

      case WebSocketEvent.Error(id, _) =>
        id

      case WebSocketEvent.Close(id) =>
        id
    }
}
object WebSocketEvent {
  // Send
  final case class ConnectOnly(webSocketConfig: WebSocketConfig)           extends WebSocketEvent with NetworkSendEvent
  final case class Open(message: String, webSocketConfig: WebSocketConfig) extends WebSocketEvent with NetworkSendEvent
  final case class Send(message: String, webSocketConfig: WebSocketConfig) extends WebSocketEvent with NetworkSendEvent

  // Receive
  final case class Receive(webSocketId: WebSocketId, message: String) extends WebSocketEvent with NetworkReceiveEvent
  final case class Error(webSocketId: WebSocketId, error: String)     extends WebSocketEvent with NetworkReceiveEvent
  final case class Close(webSocketId: WebSocketId)                    extends WebSocketEvent with NetworkReceiveEvent
}
