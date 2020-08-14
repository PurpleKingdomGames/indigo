package indigo.platform.events

import indigo.shared.events.{GlobalEvent, NetworkSendEvent, PlaySound}
import indigo.shared.networking.{HttpRequest, WebSocketEvent}

import indigo.platform.networking.{Http, WebSockets}

import scala.collection.mutable
import indigo.shared.events.StorageEvent
import indigo.platform.storage.Storage
import indigo.shared.events.AssetEvent
import indigo.platform.assets.AssetLoader
import indigo.platform.assets.AssetCollection
import indigo.platform.audio.AudioPlayer

final class GlobalEventStream(rebuildGameLoop: AssetCollection => Unit, audioPlayer: AudioPlayer, storage: Storage) {
  private val audioFilter =
    GlobalEventStream.AudioEventProcessor.filter(audioPlayer)

  private val storageFilter =
    GlobalEventStream.StorageEventProcessor.filter(storage)

  private val assetFilter =
    GlobalEventStream.AssetEventProcessor.filter(rebuildGameLoop, this)

  private val eventQueue: mutable.Queue[GlobalEvent] =
    new mutable.Queue[GlobalEvent]()

  def pushGlobalEvent(e: GlobalEvent): Unit =
    GlobalEventStream.NetworkEventProcessor
      .filter(this)(e)
      .flatMap(audioFilter)
      .flatMap(storageFilter)
      .flatMap(assetFilter)
      .foreach(e => eventQueue.enqueue(e))

  def collect: List[GlobalEvent] =
    eventQueue.dequeueAll(_ => true).toList
}

object GlobalEventStream {

  object NetworkEventProcessor {

    def filter(globalEventStream: GlobalEventStream): GlobalEvent => Option[GlobalEvent] = {
      case httpRequest: HttpRequest =>
        Http.processRequest(httpRequest, globalEventStream)
        None

      case webSocketEvent: WebSocketEvent with NetworkSendEvent =>
        WebSockets.processSendEvent(webSocketEvent, globalEventStream)
        None

      case e =>
        Some(e)
    }

  }

  object AudioEventProcessor {

    def filter: AudioPlayer => GlobalEvent => Option[GlobalEvent] =
      audioPlayer => {
        case PlaySound(assetName, volume) =>
          audioPlayer.playSound(assetName, volume)
          None

        case e =>
          Some(e)
      }

  }

  object StorageEventProcessor {

    def filter: Storage => GlobalEvent => Option[GlobalEvent] =
      storage => {
        case StorageEvent.Save(key, data) =>
          storage.save(key, data)
          None

        case StorageEvent.Load(key) =>
          storage.load(key).map(data => StorageEvent.Loaded(key, data))

        case StorageEvent.Delete(key) =>
          storage.delete(key)
          None

        case StorageEvent.DeleteAll =>
          storage.deleteAll()
          None

        case e @ StorageEvent.Loaded(_, _) =>
          Some(e)

        case e =>
          Some(e)
      }

  }

  object AssetEventProcessor {

    def filter(rebuildGameLoop: AssetCollection => Unit, ges: GlobalEventStream): GlobalEvent => Option[GlobalEvent] = {
      case AssetEvent.LoadAssetBatch(batch, key, makeAvailable) =>
        AssetLoader.backgroundLoadAssets(rebuildGameLoop, ges, batch, key, makeAvailable)
        None

      case AssetEvent.LoadAsset(asset, key, makeAvailable) =>
        AssetLoader.backgroundLoadAssets(rebuildGameLoop, ges, Set(asset), key, makeAvailable)
        None

      case e =>
        Some(e)
    }

  }

}
