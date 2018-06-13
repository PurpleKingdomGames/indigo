package com.purplekingdomgames.indigo.networking

import com.purplekingdomgames.indigo.gameengine.events._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.indigo.networking.WebSocketReadyState.{CLOSED, CLOSING}
import com.purplekingdomgames.indigo.runtime.Logger
import org.scalajs.dom

import scala.collection.mutable

object WebSockets {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val connections: mutable.HashMap[WebSocketId, dom.WebSocket] = mutable.HashMap()
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val configs: mutable.HashMap[WebSocketId, WebSocketConfig] = mutable.HashMap()

  def processSendEvent(event: WebSocketEvent with NetworkSendEvent): Unit =
    try event match {
      case WebSocketEvent.ConnectOnly(config) =>
        reEstablishConnection(insertUpdateConfig(config), None)
        ()

      case WebSocketEvent.Open(message, config) =>
        reEstablishConnection(insertUpdateConfig(config), Option(message))
        ()

      case WebSocketEvent.Send(message, config) =>
        reEstablishConnection(insertUpdateConfig(config), None).foreach { socket =>
          socket.send(message)
        }

      case _ =>
        ()
    } catch {
      case e: Throwable =>
        GlobalEventStream.pushGameEvent(WebSocketEvent.Error(event.giveId.getOrElse(WebSocketId("<not found>")), e.getMessage))
    }

  private def insertUpdateConfig(config: WebSocketConfig): WebSocketConfig = {
    val maybeConfig = configs.get(config.id)

    maybeConfig
      .flatMap { c =>
        if (c === config)
          Option(c)
        else {
          configs.remove(config.id)
          configs.put(config.id, config)
        }
      }
      .getOrElse(config)
  }

  private def reEstablishConnection(config: WebSocketConfig, onOpenSendMessage: Option[String]): Option[dom.WebSocket] =
    connections
      .get(config.id)
      .flatMap { conn =>
        WebSocketReadyState.fromInt(conn.readyState) match {
          case CLOSING | CLOSED =>
            newConnection(config, onOpenSendMessage).flatMap { newConn =>
              connections.remove(config.id)
              connections.put(config.id, newConn)
            }

          case _ =>
            Option(conn)
        }
      }
      .orElse {
        newConnection(config, onOpenSendMessage).flatMap { newConn =>
          connections.remove(config.id)
          connections.put(config.id, newConn)
        }
      }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  private def newConnection(config: WebSocketConfig, onOpenSendMessage: Option[String]): Option[dom.WebSocket] =
    try {
      val socket = new dom.WebSocket(config.address)

      socket.onmessage = (e: dom.MessageEvent) =>
        GlobalEventStream.pushGameEvent(WebSocketEvent.Receive(config.id, e.data.toString))

      socket.onopen = (_: dom.Event) => onOpenSendMessage.foreach(msg => socket.send(msg))

      socket.onerror = (e: dom.ErrorEvent) => GlobalEventStream.pushGameEvent(WebSocketEvent.Error(config.id, e.message))

      socket.onclose = (_: dom.CloseEvent) => GlobalEventStream.pushGameEvent(WebSocketEvent.Close(config.id))

      Option(socket)
    } catch {
      case e: Throwable =>
        Logger.info("Error trying to set up a websocket: " + e.getMessage)
        None
    }

}

case class WebSocketId(id: String) {
  def ===(other: WebSocketId): Boolean =
    WebSocketId.equality(this, other)
}
object WebSocketId {
  def generate: WebSocketId =
    WebSocketId(BindingKey.generate.value)

  def equality(a: WebSocketId, b: WebSocketId): Boolean =
    a.id == b.id
}

case class WebSocketConfig(id: WebSocketId, address: String) {
  def ===(other: WebSocketConfig): Boolean =
    WebSocketConfig.equality(this, other)
}
object WebSocketConfig {
  def equality(a: WebSocketConfig, b: WebSocketConfig): Boolean =
    a.id === b.id && a.address == b.address
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
