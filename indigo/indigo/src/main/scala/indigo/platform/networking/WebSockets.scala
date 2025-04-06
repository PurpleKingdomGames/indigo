package indigo.platform.networking

import indigo.platform.events.GlobalEventStream
import indigo.shared.IndigoLogger
import indigo.shared.events.*
import indigo.shared.networking.WebSocketConfig
import indigo.shared.networking.WebSocketEvent
import indigo.shared.networking.WebSocketReadyState
import indigo.shared.networking.WebSocketReadyState.CLOSED
import indigo.shared.networking.WebSocketReadyState.CLOSING
import org.scalajs.dom

import scala.annotation.nowarn

object WebSockets:

  private val connections: scalajs.js.Dictionary[dom.WebSocket] = scalajs.js.Dictionary.empty
  private val configs: scalajs.js.Dictionary[WebSocketConfig]   = scalajs.js.Dictionary.empty

  @nowarn("msg=unused")
  def processSendEvent(event: WebSocketEvent & NetworkSendEvent, globalEventStream: GlobalEventStream): Unit =
    try
      event match {
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
      }
    catch {
      case e: Throwable =>
        globalEventStream.pushGlobalEvent(WebSocketEvent.Error(event.giveId, e.getMessage))
    }

  private def insertUpdateConfig(config: WebSocketConfig): WebSocketConfig = {
    val maybeConfig = configs.get(config.id.id)

    maybeConfig
      .flatMap { c =>
        if (c == config) Option(c)
        else {
          configs.put(config.id.id, config)
        }
      }
      .getOrElse(config)
  }

  private def reEstablishConnection(
      config: WebSocketConfig,
      onOpenSendMessage: Option[String],
      globalEventStream: GlobalEventStream
  ): Option[dom.WebSocket] =
    connections
      .get(config.id.id)
      .flatMap { conn =>
        WebSocketReadyState.fromInt(conn.readyState) match {
          case CLOSING | CLOSED =>
            newConnection(config, onOpenSendMessage, globalEventStream).flatMap { newConn =>
              connections.put(config.id.id, newConn)
            }

          case _ =>
            Option(conn)
        }
      }
      .orElse {
        newConnection(config, onOpenSendMessage, globalEventStream).flatMap { newConn =>
          connections.put(config.id.id, newConn)
        }
      }

  private def newConnection(
      config: WebSocketConfig,
      onOpenSendMessage: Option[String],
      globalEventStream: GlobalEventStream
  ): Option[dom.WebSocket] =
    try {
      val socket = new dom.WebSocket(config.address)

      socket.onmessage = (e: dom.MessageEvent) =>
        globalEventStream.pushGlobalEvent(WebSocketEvent.Receive(config.id, e.data.toString))

      socket.onopen = (_: dom.Event) => onOpenSendMessage.foreach(msg => socket.send(msg))

      socket.onerror = (_: dom.Event) =>
        globalEventStream.pushGlobalEvent(WebSocketEvent.Error(config.id, "Web socket connection error"))

      socket.onclose = (_: dom.CloseEvent) => globalEventStream.pushGlobalEvent(WebSocketEvent.Close(config.id))

      Option(socket)
    } catch {
      case e: Throwable =>
        IndigoLogger.info("Error trying to set up a websocket: " + e.getMessage)
        None
    }
