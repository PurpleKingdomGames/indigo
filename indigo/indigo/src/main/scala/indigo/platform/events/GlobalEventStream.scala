package indigo.platform.events

import indigo.platform.PlatformFullScreen
import indigo.platform.assets.AssetCollection
import indigo.platform.assets.AssetLoader
import indigo.platform.audio.AudioPlayer
import indigo.platform.networking.Http
import indigo.platform.networking.WebSockets
import indigo.platform.storage.Storage
import indigo.shared.collections.Batch
import indigo.shared.events.AssetEvent
import indigo.shared.events.EnterFullScreen
import indigo.shared.events.ExitFullScreen
import indigo.shared.events.GlobalEvent
import indigo.shared.events.NetworkSendEvent
import indigo.shared.events.PlaySound
import indigo.shared.events.StorageEvent
import indigo.shared.events.ToggleFullScreen
import indigo.shared.networking.HttpRequest
import indigo.shared.networking.WebSocketEvent

import scala.collection.mutable

final class GlobalEventStream(
    rebuildGameLoop: AssetCollection => Unit,
    audioPlayer: AudioPlayer,
    storage: Storage,
    platform: => PlatformFullScreen
) {

  private val eventQueue: mutable.Queue[GlobalEvent] =
    new mutable.Queue[GlobalEvent]()

  def kill(): Unit =
    eventQueue.clear()
    ()

  val pushGlobalEvent: GlobalEvent => Unit = {
    // Networking
    case httpRequest: HttpRequest =>
      Http.processRequest(httpRequest, this)

    case webSocketEvent: WebSocketEvent with NetworkSendEvent =>
      WebSockets.processSendEvent(webSocketEvent, this)

    // Audio
    case PlaySound(assetName, volume) =>
      audioPlayer.playSound(assetName, volume)

    // Storage
    case StorageEvent.FetchKeyAt(index) =>
      eventQueue.enqueue(StorageEvent.KeyFoundAt(index, storage.key(index)))

    case StorageEvent.FetchKeys(f, t) =>
      val keys = (f to t).toList.map(i => i -> storage.key(i))
      eventQueue.enqueue(StorageEvent.KeysFound(keys))

    case StorageEvent.Save(key, data) =>
      storage.save(key, data)

    case StorageEvent.Load(key) =>
      eventQueue.enqueue(StorageEvent.Loaded(key, storage.load(key)))

    case StorageEvent.Delete(key) =>
      storage.delete(key)

    case StorageEvent.DeleteAll =>
      storage.deleteAll()

    // Assets
    case AssetEvent.LoadAssetBatch(batch, key, makeAvailable) =>
      AssetLoader.backgroundLoadAssets(rebuildGameLoop, this, batch, key, makeAvailable)

    case AssetEvent.LoadAsset(asset, key, makeAvailable) =>
      AssetLoader.backgroundLoadAssets(rebuildGameLoop, this, Set(asset), key, makeAvailable)

    // Fullscreen
    case ToggleFullScreen =>
      platform.toggleFullScreen()

    case EnterFullScreen =>
      platform.enterFullScreen()

    case ExitFullScreen =>
      platform.exitFullScreen()

    // Default
    case e =>
      eventQueue.enqueue(e)
  }

  def collect: Batch[GlobalEvent] =
    Batch.fromSeq(eventQueue.dequeueAll(_ => true))
}
