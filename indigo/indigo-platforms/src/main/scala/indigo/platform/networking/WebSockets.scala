package indigo.platform.networking

import indigo.shared.events._
import indigo.shared.networking.WebSocketReadyState
import indigo.shared.networking.WebSocketReadyState.{CLOSED, CLOSING}
import indigo.shared.networking.{WebSocketId, WebSocketConfig, WebSocketEvent}
import indigo.shared.IndigoLogger
import indigo.platform.events.GlobalEventStream

import org.scalajs.dom

import indigo.shared.EqualTo._

import scala.collection.mutable

object WebSockets {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val connections: mutable.HashMap[WebSocketId, dom.WebSocket] = mutable.HashMap()
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val configs: mutable.HashMap[WebSocketId, WebSocketConfig] = mutable.HashMap()

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def processSendEvent(event: WebSocketEvent with NetworkSendEvent, globalEventStream: GlobalEventStream): Unit =
    try event match {
      case WebSocketEvent.ConnectOnly(config) =>
        reEstablishConnection(insertUpdateConfig(config), None, globalEventStream)
        ()

      case WebSocketEvent.Open(message, config) =>
        reEstablishConnection(insertUpdateConfig(config), Option(message), globalEventStream)
        ()

      case WebSocketEvent.Send(message, config) =>
        reEstablishConnection(insertUpdateConfig(config), None, globalEventStream).foreach { socket =>
          socket.send(message)
        }
    } catch {
      case e: Throwable =>
        globalEventStream.pushGlobalEvent(WebSocketEvent.Error(event.giveId, e.getMessage))
    }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
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

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private def reEstablishConnection(config: WebSocketConfig, onOpenSendMessage: Option[String], globalEventStream: GlobalEventStream): Option[dom.WebSocket] =
    connections
      .get(config.id)
      .flatMap { conn =>
        WebSocketReadyState.fromInt(conn.readyState) match {
          case CLOSING | CLOSED =>
            newConnection(config, onOpenSendMessage, globalEventStream).flatMap { newConn =>
              connections.remove(config.id)
              connections.put(config.id, newConn)
            }

          case _ =>
            Option(conn)
        }
      }
      .orElse {
        newConnection(config, onOpenSendMessage, globalEventStream).flatMap { newConn =>
          connections.remove(config.id)
          connections.put(config.id, newConn)
        }
      }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  private def newConnection(config: WebSocketConfig, onOpenSendMessage: Option[String], globalEventStream: GlobalEventStream): Option[dom.WebSocket] =
    try {
      val socket = new dom.WebSocket(config.address)

      socket.onmessage = (e: dom.MessageEvent) => globalEventStream.pushGlobalEvent(WebSocketEvent.Receive(config.id, e.data.toString))

      socket.onopen = (_: dom.Event) => onOpenSendMessage.foreach(msg => socket.send(msg))

      socket.onerror = (_: dom.Event) => globalEventStream.pushGlobalEvent(WebSocketEvent.Error(config.id, "Web socket connection error"))

      socket.onclose = (_: dom.CloseEvent) => globalEventStream.pushGlobalEvent(WebSocketEvent.Close(config.id))

      Option(socket)
    } catch {
      case e: Throwable =>
        IndigoLogger.info("Error trying to set up a websocket: " + e.getMessage)
        None
    }

}
