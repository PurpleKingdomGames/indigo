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

  private val eventQueue: mutable.Queue[GlobalEvent] =
    new mutable.Queue[GlobalEvent]()

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  val pushGlobalEvent: GlobalEvent => Unit = {
    // Networking
    case httpRequest: HttpRequest =>
      Http.processRequest(httpRequest, this)

    case webSocketEvent: WebSocketEvent with NetworkSendEvent =>
      WebSockets.processSendEvent(webSocketEvent, this)

    //Audio
    case PlaySound(assetName, volume) =>
      audioPlayer.playSound(assetName, volume)

    // Storage
    case StorageEvent.Save(key, data) =>
      storage.save(key, data)

    case StorageEvent.Load(key) =>
      storage.load(key).foreach { data =>
        eventQueue.enqueue(StorageEvent.Loaded(key, data))
      }

    case StorageEvent.Delete(key) =>
      storage.delete(key)

    case StorageEvent.DeleteAll =>
      storage.deleteAll()

    case e @ StorageEvent.Loaded(_, _) =>
      eventQueue.enqueue(e)

    // Assets
    case AssetEvent.LoadAssetBatch(batch, key, makeAvailable) =>
      AssetLoader.backgroundLoadAssets(rebuildGameLoop, this, batch, key, makeAvailable)

    case AssetEvent.LoadAsset(asset, key, makeAvailable) =>
      AssetLoader.backgroundLoadAssets(rebuildGameLoop, this, Set(asset), key, makeAvailable)

    // Default
    case e =>
      eventQueue.enqueue(e)
  }

  def collect: List[GlobalEvent] =
    eventQueue.dequeueAll(_ => true).toList
}
