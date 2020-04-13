package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import indigo.shared.events.GlobalEvent
import indigo.shared.events.FrameTick
import indigo.shared.events.MouseEvent
import indigo.shared.events.KeyboardEvent
import indigo.shared.events.StorageEvent
import indigo.shared.networking.WebSocketEvent
import indigo.shared.networking.HttpReceiveEvent
import indigo.shared.events.PlaySound
import indigo.shared.networking.HttpRequest
import indigo.shared.networking.WebSocketConfig
import indigo.shared.networking.WebSocketId
import indigo.shared.assets.AssetName

@SuppressWarnings(Array("org.wartremover.warts.Any"))
trait GlobalEventDelegate {
  @JSExport
  val eventType: String
  @JSExport
  val details: js.Object
  def toInternal: GlobalEvent
}

@SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "org.wartremover.warts.Null"))
object GlobalEventDelegate {

  def apply(_eventType: String, _details: js.Object, _internal: GlobalEvent): GlobalEventDelegate =
    new GlobalEventDelegate {
      val eventType: String       = _eventType
      val details: js.Object      = _details
      def toInternal: GlobalEvent = _internal
    }

  final class GlobalEventWrapper(val event: GlobalEventDelegate) extends GlobalEvent

  // This are read only events, a game developer wouldn't make them
  def fromGlobalEvent: GlobalEvent => GlobalEventDelegate = {
    case e @ FrameTick =>
      GlobalEventDelegate("frametick", null, e)

    case e @ MouseEvent.Click(_, _) =>
      GlobalEventDelegate("mouseClick", js.Dynamic.literal(x = e.x, y = e.y), e)

    case e @ MouseEvent.MouseUp(_, _) =>
      GlobalEventDelegate("mouseUp", js.Dynamic.literal(x = e.x, y = e.y), e)

    case e @ MouseEvent.MouseDown(_, _) =>
      GlobalEventDelegate("mouseDown", js.Dynamic.literal(x = e.x, y = e.y), e)

    case e @ MouseEvent.Move(_, _) =>
      GlobalEventDelegate("mouseMove", js.Dynamic.literal(x = e.x, y = e.y), e)

    case e @ KeyboardEvent.KeyUp(_) =>
      GlobalEventDelegate("keyUp", js.Dynamic.literal(key = e.keyCode.key, code = e.keyCode.code), e)

    case e @ KeyboardEvent.KeyDown(_) =>
      GlobalEventDelegate("keyDown", js.Dynamic.literal(key = e.keyCode.key, code = e.keyCode.code), e)

    case e @ StorageEvent.Loaded(data) =>
      GlobalEventDelegate("dataLoaded", js.Dynamic.literal(data = data), e)

    case e @ WebSocketEvent.Receive(socketId, message) =>
      GlobalEventDelegate("socketReceive", js.Dynamic.literal(socketId = socketId.id, message = message), e)

    case e @ WebSocketEvent.Error(socketId, error) =>
      GlobalEventDelegate("socketError", js.Dynamic.literal(socketId = socketId.id, error = error), e)

    case e @ WebSocketEvent.Close(socketId) =>
      GlobalEventDelegate("socketClose", js.Dynamic.literal(socketId = socketId.id), e)

    case e @ HttpReceiveEvent.HttpError =>
      GlobalEventDelegate("httpError", null, e)

    case e @ HttpReceiveEvent.HttpResponse(status, headers, body) =>
      GlobalEventDelegate("httpResponse", js.Dynamic.literal(status = status, headers = headers.toJSDictionary, body = body.getOrElse(null)), e)

    case e =>
      GlobalEventDelegate(null, e.asInstanceOf[js.Object], e)
  }

}

// Web Sockets
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("ConnectOnly")
final class ConnectOnlyDelegate(_id: String, _address: String) extends GlobalEventDelegate {
  @JSExport
  val id = _id
  @JSExport
  val address = _address
  @JSExport
  val eventType: String = "socketConnectOnly"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(id = id, address = address)
  def toInternal: GlobalEvent = WebSocketEvent.ConnectOnly(WebSocketConfig(WebSocketId(id), address))
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Open")
final class OpenDelegate(_id: String, _address: String, _message: String) extends GlobalEventDelegate {
  @JSExport
  val id = _id
  @JSExport
  val address = _address
  @JSExport
  val message = _message
  @JSExport
  val eventType: String = "socketOpen"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(id = id, address = address, message = message)
  def toInternal: GlobalEvent = WebSocketEvent.Open(message, WebSocketConfig(WebSocketId(id), address))
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Send")
final class SendDelegate(_id: String, _address: String, _message: String) extends GlobalEventDelegate {
  @JSExport
  val id = _id
  @JSExport
  val address = _address
  @JSExport
  val message = _message
  @JSExport
  val eventType: String = "socketSend"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(id = id, address = address, message = message)
  def toInternal: GlobalEvent = WebSocketEvent.Send(message, WebSocketConfig(WebSocketId(id), address))
}

// HTTP
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GET")
final class HTTPGETDelegate(_url: String, _params: js.Dictionary[String], _headers: js.Dictionary[String]) extends GlobalEventDelegate {
  @JSExport
  val url = _url
  @JSExport
  val params = _params
  @JSExport
  val headers = _headers
  @JSExport
  val eventType: String = "httpGet"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(url = url, params = params, headers = headers)
  def toInternal: GlobalEvent = HttpRequest.GET(url, params.toMap, headers.toMap)
}

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Null"))
@JSExportTopLevel("POST")
final class HTTPPOSTDelegate(_url: String, _params: js.Dictionary[String], _headers: js.Dictionary[String], _body: Option[String]) extends GlobalEventDelegate {
  @JSExport
  val url = _url
  @JSExport
  val params = _params
  @JSExport
  val headers = _headers
  @JSExport
  val body = _body
  @JSExport
  val eventType: String = "httpPost"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(url = url, params = params, headers = headers, body = body.getOrElse(null))
  def toInternal: GlobalEvent = HttpRequest.POST(url, params.toMap, headers.toMap, body)
}

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Null"))
@JSExportTopLevel("PUT")
final class HTTPPUTDelegate(_url: String, _params: js.Dictionary[String], _headers: js.Dictionary[String], _body: Option[String]) extends GlobalEventDelegate {
  @JSExport
  val url = _url
  @JSExport
  val params = _params
  @JSExport
  val headers = _headers
  @JSExport
  val body = _body
  @JSExport
  val eventType: String = "httpPut"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(url = url, params = params, headers = headers, body = body.getOrElse(null))
  def toInternal: GlobalEvent = HttpRequest.PUT(url, params.toMap, headers.toMap, body)
}

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Null"))
@JSExportTopLevel("DELETE")
final class HTTPDELETEDelegate(_url: String, _params: js.Dictionary[String], _headers: js.Dictionary[String], _body: Option[String]) extends GlobalEventDelegate {
  @JSExport
  val url = _url
  @JSExport
  val params = _params
  @JSExport
  val headers = _headers
  @JSExport
  val body = _body
  @JSExport
  val eventType: String = "httpDelete"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(url = url, params = params, headers = headers, body = body.getOrElse(null))
  def toInternal: GlobalEvent = HttpRequest.DELETE(url, params.toMap, headers.toMap, body)
}

// Sounds
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("PlaySound")
final class PlaySoundDelegate(_assetName: String, _volume: VolumeDelegate) extends GlobalEventDelegate {
  @JSExport
  val assetName = _assetName
  @JSExport
  val volume = _volume
  @JSExport
  val eventType: String = "playSound"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(assetRef = assetName, volume = volume.asInstanceOf[js.Object])
  def toInternal: GlobalEvent = PlaySound(AssetName(assetName), volume.toInternal)
}

// Storage
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Save")
final class SaveDelegate(_key: String, _data: String) extends GlobalEventDelegate {
  @JSExport
  val key: String = _key
  @JSExport
  val data = _data
  @JSExport
  val eventType: String = "save"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(key = key, data = data)
  def toInternal: GlobalEvent = StorageEvent.Save(key, data)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Load")
final class LoadDelegate(_key: String) extends GlobalEventDelegate {
  @JSExport
  val key: String = _key
  @JSExport
  val eventType: String = "load"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(key = key)
  def toInternal: GlobalEvent = StorageEvent.Load(key)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Delete")
final class DeleteDelegate(_key: String) extends GlobalEventDelegate {
  @JSExport
  val key: String = _key
  @JSExport
  val eventType: String = "delete"
  @JSExport
  val details: js.Object      = js.Dynamic.literal(key = key)
  def toInternal: GlobalEvent = StorageEvent.Delete(key)
}

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Null"))
@JSExportTopLevel("DeleteAll")
final class DeleteAllDelegate extends GlobalEventDelegate {
  @JSExport
  val eventType: String = "deleteAll"
  @JSExport
  val details: js.Object      = null
  def toInternal: GlobalEvent = StorageEvent.DeleteAll
}
