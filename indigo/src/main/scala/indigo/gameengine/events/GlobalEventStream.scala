package indigo.gameengine.events

import indigo.gameengine.audio.IAudioPlayer
import indigo.gameengine.scenegraph.PlaySound
import indigo.networking._

import scala.collection.mutable

trait GlobalEventStream {
  def pushGameEvent(e: GameEvent): Unit
  def pushViewEvent(audioPlayer: IAudioPlayer, e: FrameEvent): Unit
  def collect: List[GameEvent]
}

object GlobalEventStream {

  val default: GlobalEventStream =
    new GlobalEventStream {
      private val eventQueue: mutable.Queue[GameEvent] =
        new mutable.Queue[GameEvent]()

      def pushGameEvent(e: GameEvent): Unit =
        NetworkEventProcessor
          .filter(this)(e)
          .foreach(e => eventQueue += e)

      def pushViewEvent(audioPlayer: IAudioPlayer, e: FrameEvent): Unit =
        NetworkEventProcessor
          .filter(this)(e)
          .flatMap { AudioEventProcessor.filter(audioPlayer) }
          .foreach(e => eventQueue += e)

      def collect: List[GameEvent] =
        eventQueue.dequeueAll(_ => true).toList
    }

  object NetworkEventProcessor {

    def filter(implicit globalEventStream: GlobalEventStream): GameEvent => Option[GameEvent] = {
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

  object AudioEventProcessor {

    def filter: IAudioPlayer => GameEvent => Option[GameEvent] = audioPlayer => {
      case PlaySound(assetRef, volume) =>
        audioPlayer.playSound(assetRef, volume)
        None

      case e =>
        Option(e)
    }

  }

}
