package indigo.platform.events

import indigo.platform.PlatformFullScreen
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

    case webSocketEvent: (WebSocketEvent & NetworkSendEvent) =>
      WebSockets.processSendEvent(webSocketEvent, this)

    // Audio
    case PlaySound(assetName, volume, switch) =>
      audioPlayer.playSound(assetName, volume, switch)

    // Storage
    case StorageEvent.FetchKeyAt(index) =>
      storage.key(index) match {
        case Left(err)  => eventQueue.enqueue(err)
        case Right(str) => eventQueue.enqueue(StorageEvent.KeyFoundAt(index, str))
      }

    case StorageEvent.FetchKeys(f, t) =>
      val keys = (f to t).toList.map(i => i -> storage.key(i))
      val errors = keys.flatMap {
        _ match {
          case (_, Left(err)) => Some(err)
          case _              => None
        }
      }

      if errors.nonEmpty then errors foreach (e => eventQueue.enqueue(e))
      else
        eventQueue.enqueue(StorageEvent.KeysFound(keys.flatMap {
          _ match {
            case (i, Right(str)) => Some((i, str))
            case _               => None
          }
        }))

    case StorageEvent.Save(key, data) =>
      storage.save(key, data) match {
        case Left(err) => eventQueue.enqueue(err)
        case _         => ()
      }

    case StorageEvent.Load(key) =>
      storage.load(key) match {
        case Left(err)  => eventQueue.enqueue(err)
        case Right(str) => eventQueue.enqueue(StorageEvent.Loaded(key, str))
      }

    case StorageEvent.Delete(key) =>
      storage.delete(key) match {
        case Left(err) => eventQueue.enqueue(err)
        case _         => ()
      }

    case StorageEvent.DeleteAll =>
      storage.deleteAll() match {
        case Left(err) => eventQueue.enqueue(err)
        case _         => ()
      }

    // Assets
    case AssetEvent.LoadAssetBatch(batch, key, makeAvailable) =>
      AssetLoader.backgroundLoadAssets(this, batch, key, makeAvailable)

    case AssetEvent.LoadAsset(asset, key, makeAvailable) =>
      AssetLoader.backgroundLoadAssets(this, Set(asset), key, makeAvailable)

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
