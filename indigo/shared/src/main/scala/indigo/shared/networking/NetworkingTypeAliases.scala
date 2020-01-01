package indigo.shared.networking

import indigo.shared.networking

trait NetworkingTypeAliases {

  //WebSockets

  type WebSocketEvent = networking.WebSocketEvent
  val WebSocketEvent: networking.WebSocketEvent.type = networking.WebSocketEvent

  type WebSocketConfig = networking.WebSocketConfig
  val WebSocketConfig: networking.WebSocketConfig.type = networking.WebSocketConfig

  type WebSocketId = networking.WebSocketId
  val WebSocketId: networking.WebSocketId.type = networking.WebSocketId

  type WebSocketReadyState = networking.WebSocketReadyState
  val WebSocketReadyState: networking.WebSocketReadyState.type = networking.WebSocketReadyState

  // Http

  val HttpMethod: networking.HttpMethod.type = networking.HttpMethod

  type HttpRequest = networking.HttpRequest
  val HttpRequest: networking.HttpRequest.type = networking.HttpRequest

  type HttpReceiveEvent = networking.HttpReceiveEvent
  val HttpReceiveEvent: networking.HttpReceiveEvent.type = networking.HttpReceiveEvent

  val HttpError: networking.HttpReceiveEvent.HttpError.type = networking.HttpReceiveEvent.HttpError

  type HttpResponse = networking.HttpReceiveEvent.HttpResponse
  val HttpResponse: networking.HttpReceiveEvent.HttpResponse.type = networking.HttpReceiveEvent.HttpResponse

}
