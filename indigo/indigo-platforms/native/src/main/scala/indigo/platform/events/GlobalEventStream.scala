package indigo.platform.events

import indigo.shared.platform.GlobalEventStream
import indigo.shared.events.{GlobalEvent, NetworkSendEvent, PlaySound}
import indigo.shared.networking.{HttpRequest, WebSocketEvent}
import indigo.shared.platform.AudioPlayer

import indigo.platform.networking.{Http, WebSockets}

import scala.collection.mutable

object GlobalEventStreamImpl {

  def default(audioPlayer: AudioPlayer): GlobalEventStream =
    new GlobalEventStream {
      private val eventQueue: mutable.Queue[GlobalEvent] =
        new mutable.Queue[GlobalEvent]()

      def pushGlobalEvent(e: GlobalEvent): Unit =
        NetworkEventProcessor
          .filter(this)(e)
          .flatMap { AudioEventProcessor.filter(audioPlayer) }
          .foreach(e => eventQueue += e)

      def collect: List[GlobalEvent] =
        eventQueue.dequeueAll(_ => true).toList
    }

  object NetworkEventProcessor {

    def filter(globalEventStream: GlobalEventStream): GlobalEvent => Option[GlobalEvent] = {
      case httpRequest: HttpRequest =>
        Http.processRequest(httpRequest, globalEventStream)
        None

      case webSocketEvent: WebSocketEvent with NetworkSendEvent =>
        WebSockets.processSendEvent(webSocketEvent, globalEventStream)
        None

      case e =>
        Option(e)
    }

  }

  object AudioEventProcessor {

    def filter: AudioPlayer => GlobalEvent => Option[GlobalEvent] = audioPlayer => {
      case PlaySound(assetRef, volume) =>
        audioPlayer.playSound(assetRef, volume)
        None

      case e =>
        Option(e)
    }

  }

}
