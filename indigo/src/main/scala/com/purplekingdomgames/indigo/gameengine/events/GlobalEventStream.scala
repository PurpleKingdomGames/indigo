package com.purplekingdomgames.indigo.gameengine.events

import com.purplekingdomgames.indigo.networking.{Http, WebSockets}

import scala.collection.mutable

object GlobalEventStream {

  private val eventQueue: mutable.Queue[GameEvent] =
    new mutable.Queue[GameEvent]()

  def push(e: GameEvent): Unit =
    NetworkEventProcessor.filter(e).foreach(e => eventQueue += e)

  def collect: List[GameEvent] =
    eventQueue.dequeueAll(_ => true).toList

}

object NetworkEventProcessor {

  def filter: GameEvent => Option[GameEvent] = {
    case httpRequest: HttpRequest =>
      Http.processRequest(httpRequest)
      None

    case webSocketEvent: WebSocketEvent with NetworkSendEvent =>
      WebSockets.processSendEvent(webSocketEvent)
      None

    case e =>
      Option(e)
  }


}