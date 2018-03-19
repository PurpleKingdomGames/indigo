package com.purplekingdomgames.indigo.networking

import com.purplekingdomgames.indigo.gameengine.events._
import org.scalajs.dom
import org.scalajs.dom.WebSocket

import scala.collection.mutable

object WebSocket {

  private val connections: mutable.HashMap[String, WebSocketConnection] = mutable.HashMap()

  def create(key: String, webSocketConnection: WebSocketConnection): Unit =
    if(connections.get(key).isEmpty) {
      connections.put(key, webSocketConnection)
    }

  def close(key: String): Unit =
    connections.get(key).foreach(_.close())

}

case class WebSocketConnection(address: String, onOpen: () => WebSocketSend, onMessage: dom.MessageEvent => WebSocketReceive, onError: () => WebSocketError, onClose: () => WebSocketClose) {
  private var maybeWebSocket: Option[WebSocket] = None

  def readyState: WebSocketReadyState =
    maybeWebSocket.map(s => WebSocketReadyState.fromInt(s.readyState)).getOrElse(WebSocketReadyState.CLOSED)

  def open(): Unit = {
    val socket = new dom.WebSocket(address)

    socket.onmessage =
      (e: dom.MessageEvent) =>
        GlobalEventStream.push(onMessage(e))

    socket.onopen =
      (_: dom.Event) =>
        GlobalEventStream.push(onOpen())

    socket.onerror =
      (e: dom.ErrorEvent) =>
        GlobalEventStream.push(onError())

    socket.onclose =
      (e: dom.CloseEvent) =>
        GlobalEventStream.push(onClose())

    maybeWebSocket = Some(socket)
  }

  def close(): Unit =
    maybeWebSocket.foreach(_.close())

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
    val value: Int = 0
    val isConnecting: Boolean = true
    val isOpen: Boolean = false
    val isClosing: Boolean = false
    val isClosed: Boolean = false
  }

  case object OPEN extends WebSocketReadyState {
    val value: Int = 1
    val isConnecting: Boolean = false
    val isOpen: Boolean = true
    val isClosing: Boolean = false
    val isClosed: Boolean = false
  }

  case object CLOSING extends WebSocketReadyState {
    val value: Int = 2
    val isConnecting: Boolean = false
    val isOpen: Boolean = false
    val isClosing: Boolean = true
    val isClosed: Boolean = false
  }

  case object CLOSED extends WebSocketReadyState {
    val value: Int = 3
    val isConnecting: Boolean = false
    val isOpen: Boolean = false
    val isClosing: Boolean = false
    val isClosed: Boolean = true
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