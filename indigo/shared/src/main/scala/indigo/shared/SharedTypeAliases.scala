package indigo.shared

import indigo.shared

trait SharedTypeAliases {

  type AsString[A] = shared.AsString[A]
  val AsString: shared.AsString.type = shared.AsString

  type EqualTo[A] = shared.EqualTo[A]
  val EqualTo: shared.EqualTo.type = shared.EqualTo

  type AssetType = shared.assets.AssetType
  val AssetType: shared.assets.AssetType.type = shared.assets.AssetType

  type ClearColor = shared.ClearColor
  val ClearColor: shared.ClearColor.type = shared.ClearColor

  type GameConfig = shared.config.GameConfig
  val GameConfig: shared.config.GameConfig.type = shared.config.GameConfig

  type GameViewport = shared.config.GameViewport
  val GameViewport: shared.config.GameViewport.type = shared.config.GameViewport

  type AdvancedGameConfig = shared.config.AdvancedGameConfig
  val AdvancedGameConfig: shared.config.AdvancedGameConfig.type = shared.config.AdvancedGameConfig

  val IndigoLogger: shared.IndigoLogger.type = shared.IndigoLogger

  type Aseprite = shared.formats.Aseprite
  val Aseprite: shared.formats.Aseprite.type = shared.formats.Aseprite

  type TiledMap = shared.formats.TiledMap
  val TiledMap: shared.formats.TiledMap.type = shared.formats.TiledMap

  type Gamepad = shared.input.Gamepad
  val Gamepad: shared.input.Gamepad.type = shared.input.Gamepad

  type GamepadDPad = shared.input.GamepadDPad
  val GamepadDPad: shared.input.GamepadDPad.type = shared.input.GamepadDPad

  type GamepadAnalogControls = shared.input.GamepadAnalogControls
  val GamepadAnalogControls: shared.input.GamepadAnalogControls.type = shared.input.GamepadAnalogControls

  type AnalogAxis = shared.input.AnalogAxis
  val AnalogAxis: shared.input.AnalogAxis.type = shared.input.AnalogAxis

  type GamepadButtons = shared.input.GamepadButtons
  val GamepadButtons: shared.input.GamepadButtons.type = shared.input.GamepadButtons

  type BoundaryLocator = shared.BoundaryLocator

  type FrameContext = shared.FrameContext

}
