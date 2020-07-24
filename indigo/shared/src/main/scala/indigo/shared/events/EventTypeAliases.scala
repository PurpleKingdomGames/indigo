package indigo.shared.events

import indigo.shared.events

trait EventTypeAliases {

  type GlobalEvent    = events.GlobalEvent
  type SubSystemEvent = events.SubSystemEvent
  type ViewEvent      = events.ViewEvent
  type InputEvent     = events.InputEvent

  type EventFilters = events.EventFilters
  val EventFilters: events.EventFilters.type = events.EventFilters

  type RendererDetails = events.RendererDetails
  val RendererDetails: events.RendererDetails.type = events.RendererDetails

  type ViewportResize = events.ViewportResize
  val ViewportResize: events.ViewportResize.type = events.ViewportResize

  type InputState = events.InputState
  val InputState: events.InputState.type = events.InputState

  type InputMapping[A] = events.InputMapping[A]
  val InputMapping: events.InputMapping.type = events.InputMapping

  type Combo = events.Combo
  val Combo: events.Combo.type = events.Combo

  type GamepadInput = events.GamepadInput
  val GamepadInput: events.GamepadInput.type = events.GamepadInput

  type MouseInput = events.MouseInput
  val MouseInput: events.MouseInput.type = events.MouseInput

  type MouseEvent = events.MouseEvent
  val MouseEvent: events.MouseEvent.type = events.MouseEvent

  type KeyboardEvent = events.KeyboardEvent
  val KeyboardEvent: events.KeyboardEvent.type = events.KeyboardEvent

  type FrameTick = events.FrameTick.type
  val FrameTick: events.FrameTick.type = events.FrameTick

  type PlaySound = events.PlaySound
  val PlaySound: events.PlaySound.type = events.PlaySound

  type NetworkSendEvent    = events.NetworkSendEvent
  type NetworkReceiveEvent = events.NetworkReceiveEvent

  type StorageEvent = events.StorageEvent

  type Save = events.StorageEvent.Save
  val Save: events.StorageEvent.Save.type = events.StorageEvent.Save

  type Load = events.StorageEvent.Load
  val Load: events.StorageEvent.Load.type = events.StorageEvent.Load

  type Delete = events.StorageEvent.Delete
  val Delete: events.StorageEvent.Delete.type = events.StorageEvent.Delete

  val DeleteAll: events.StorageEvent.DeleteAll.type = events.StorageEvent.DeleteAll

  type Loaded = events.StorageEvent.Loaded
  val Loaded: events.StorageEvent.Loaded.type = events.StorageEvent.Loaded

  type AssetEvent = events.AssetEvent

  type LoadAsset = events.AssetEvent.LoadAsset
  val LoadAsset: events.AssetEvent.LoadAsset.type = events.AssetEvent.LoadAsset

  type LoadAssetBatch = events.AssetEvent.LoadAssetBatch
  val LoadAssetBatch: events.AssetEvent.LoadAssetBatch.type = events.AssetEvent.LoadAssetBatch

  type AssetBatchLoaded = events.AssetEvent.AssetBatchLoaded
  val AssetBatchLoaded: events.AssetEvent.AssetBatchLoaded.type = events.AssetEvent.AssetBatchLoaded

  type AssetBatchLoadError = events.AssetEvent.AssetBatchLoadError
  val AssetBatchLoadError: events.AssetEvent.AssetBatchLoadError.type = events.AssetEvent.AssetBatchLoadError

}
