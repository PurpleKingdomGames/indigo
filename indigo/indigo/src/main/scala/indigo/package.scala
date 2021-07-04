package indigo

val logger: indigo.shared.IndigoLogger.type = indigo.shared.IndigoLogger

type Startup[SuccessType] = shared.Startup[SuccessType]
val Startup: shared.Startup.type = shared.Startup

type GameTime = shared.time.GameTime
val GameTime: shared.time.GameTime.type = shared.time.GameTime

type Millis = shared.time.Millis
val Millis: shared.time.Millis.type = shared.time.Millis

type Seconds = shared.time.Seconds
val Seconds: shared.time.Seconds.type = shared.time.Seconds

type Dice = shared.dice.Dice
val Dice: shared.dice.Dice.type = shared.dice.Dice

type AssetCollection = platform.assets.AssetCollection

type AssetName = shared.assets.AssetName
val AssetName: shared.assets.AssetName.type = shared.assets.AssetName

type AssetPath = shared.assets.AssetPath
val AssetPath: shared.assets.AssetPath.type = shared.assets.AssetPath

type AssetTag = shared.assets.AssetTag
val AssetTag: shared.assets.AssetTag.type = shared.assets.AssetTag

type Material = shared.materials.Material
val Material: shared.materials.Material.type = shared.materials.Material

type LightingModel = shared.materials.LightingModel
val LightingModel: shared.materials.LightingModel.type = shared.materials.LightingModel

type Texture = shared.materials.Texture
val Texture: shared.materials.Texture.type = shared.materials.Texture

type BlendMaterial = shared.materials.BlendMaterial
val BlendMaterial: shared.materials.BlendMaterial.type = shared.materials.BlendMaterial

type ShaderData = shared.materials.ShaderData
val ShaderData: shared.materials.ShaderData.type = shared.materials.ShaderData

type BlendShaderData = shared.materials.BlendShaderData
val BlendShaderData: shared.materials.BlendShaderData.type = shared.materials.BlendShaderData

type Shader = shared.shader.Shader

type BlendShader = shared.shader.BlendShader
val BlendShader: shared.shader.BlendShader.type = shared.shader.BlendShader

val ShaderLibrary: indigo.shaders.ShaderLibrary.type = indigo.shaders.ShaderLibrary

type EntityShader = shared.shader.EntityShader
val EntityShader: shared.shader.EntityShader.type = shared.shader.EntityShader

type ShaderId = shared.shader.ShaderId
val ShaderId: shared.shader.ShaderId.type = shared.shader.ShaderId

type Uniform = shared.shader.Uniform
val Uniform: shared.shader.Uniform.type = shared.shader.Uniform

type UniformBlock = shared.shader.UniformBlock
val UniformBlock: shared.shader.UniformBlock.type = shared.shader.UniformBlock

type ShaderPrimitive = shared.shader.ShaderPrimitive
val ShaderPrimitive: shared.shader.ShaderPrimitive.type = shared.shader.ShaderPrimitive

val StandardShaders: shared.shader.StandardShaders.type = shared.shader.StandardShaders

type Outcome[T] = shared.Outcome[T]
val Outcome: shared.Outcome.type = shared.Outcome

type Key = shared.constants.Key
val Key: shared.constants.Key.type = shared.constants.Key

type NonEmptyList[A] = shared.collections.NonEmptyList[A]
val NonEmptyList: shared.collections.NonEmptyList.type = shared.collections.NonEmptyList

type Signal[A] = shared.temporal.Signal[A]
val Signal: shared.temporal.Signal.type = shared.temporal.Signal

type SignalReader[R, A] = shared.temporal.SignalReader[R, A]
val SignalReader: shared.temporal.SignalReader.type = shared.temporal.SignalReader

type SignalState[S, A] = shared.temporal.SignalState[S, A]
val SignalState: shared.temporal.SignalState.type = shared.temporal.SignalState

type SignalFunction[A, B] = shared.temporal.SignalFunction[A, B]
val SignalFunction: shared.temporal.SignalFunction.type = shared.temporal.SignalFunction

type SubSystem = shared.subsystems.SubSystem
val SubSystem: shared.subsystems.SubSystem.type = shared.subsystems.SubSystem

/** defaultGameConfig Provides a useful default config set up:
  *   - Game Viewport = 550 x 400
  *   - FPS = 30
  *   - Clear color = Black
  *   - Magnification = 1
  *   - No advanced settings enabled
  * @return
  *   A GameConfig instance
  */
val defaultGameConfig: indigo.shared.config.GameConfig =
  indigo.shared.config.GameConfig.default

/** noRender Convenience value, alias for SceneUpdateFragment.empty
  * @return
  *   An Empty SceneUpdateFragment
  */
val noRender: indigo.shared.scenegraph.SceneUpdateFragment =
  indigo.shared.scenegraph.SceneUpdateFragment.empty

// events

type GlobalEvent    = shared.events.GlobalEvent
type SubSystemEvent = shared.events.SubSystemEvent
type ViewEvent      = shared.events.ViewEvent
type InputEvent     = shared.events.InputEvent
type EventHandler   = shared.scenegraph.EventHandler

type EventFilters = shared.events.EventFilters
val EventFilters: shared.events.EventFilters.type = shared.events.EventFilters

type AccessControl = shared.events.AccessControl
val AccessControl: shared.events.AccessControl.type = shared.events.AccessControl

type RendererDetails = shared.events.RendererDetails
val RendererDetails: shared.events.RendererDetails.type = shared.events.RendererDetails

type ViewportResize = shared.events.ViewportResize
val ViewportResize: shared.events.ViewportResize.type = shared.events.ViewportResize

val ToggleFullScreen: shared.events.ToggleFullScreen.type         = shared.events.ToggleFullScreen
val EnterFullScreen: shared.events.EnterFullScreen.type           = shared.events.EnterFullScreen
val ExitFullScreen: shared.events.ExitFullScreen.type             = shared.events.ExitFullScreen
val FullScreenEntered: shared.events.FullScreenEntered.type       = shared.events.FullScreenEntered
val FullScreenEnterError: shared.events.FullScreenEnterError.type = shared.events.FullScreenEnterError
val FullScreenExited: shared.events.FullScreenExited.type         = shared.events.FullScreenExited
val FullScreenExitError: shared.events.FullScreenExitError.type   = shared.events.FullScreenExitError

type InputState = shared.events.InputState
val InputState: shared.events.InputState.type = shared.events.InputState

type InputMapping[A] = shared.events.InputMapping[A]
val InputMapping: shared.events.InputMapping.type = shared.events.InputMapping

type Combo = shared.events.Combo
val Combo: shared.events.Combo.type = shared.events.Combo

type GamepadInput = shared.events.GamepadInput
val GamepadInput: shared.events.GamepadInput.type = shared.events.GamepadInput

type Mouse = shared.input.Mouse
val Mouse: shared.input.Mouse.type = shared.input.Mouse

type MouseInput = shared.events.MouseInput
val MouseInput: shared.events.MouseInput.type = shared.events.MouseInput

type MouseEvent = shared.events.MouseEvent
val MouseEvent: shared.events.MouseEvent.type = shared.events.MouseEvent

type Keyboard = shared.input.Keyboard
val Keyboard: shared.input.Keyboard.type = shared.input.Keyboard

type KeyboardEvent = shared.events.KeyboardEvent
val KeyboardEvent: shared.events.KeyboardEvent.type = shared.events.KeyboardEvent

type FrameTick = shared.events.FrameTick.type
val FrameTick: shared.events.FrameTick.type = shared.events.FrameTick

type PlaySound = shared.events.PlaySound
val PlaySound: shared.events.PlaySound.type = shared.events.PlaySound

type NetworkSendEvent    = shared.events.NetworkSendEvent
type NetworkReceiveEvent = shared.events.NetworkReceiveEvent

type StorageEvent = shared.events.StorageEvent
val StorageEvent: shared.events.StorageEvent.type = shared.events.StorageEvent

type Save = shared.events.StorageEvent.Save
val Save: shared.events.StorageEvent.Save.type = shared.events.StorageEvent.Save

type Load = shared.events.StorageEvent.Load
val Load: shared.events.StorageEvent.Load.type = shared.events.StorageEvent.Load

type Delete = shared.events.StorageEvent.Delete
val Delete: shared.events.StorageEvent.Delete.type = shared.events.StorageEvent.Delete

val DeleteAll: shared.events.StorageEvent.DeleteAll.type = shared.events.StorageEvent.DeleteAll

type Loaded = shared.events.StorageEvent.Loaded
val Loaded: shared.events.StorageEvent.Loaded.type = shared.events.StorageEvent.Loaded

type AssetEvent = shared.events.AssetEvent
val AssetEvent: shared.events.AssetEvent.type = shared.events.AssetEvent

type LoadAsset = shared.events.AssetEvent.LoadAsset
val LoadAsset: shared.events.AssetEvent.LoadAsset.type = shared.events.AssetEvent.LoadAsset

type LoadAssetBatch = shared.events.AssetEvent.LoadAssetBatch
val LoadAssetBatch: shared.events.AssetEvent.LoadAssetBatch.type = shared.events.AssetEvent.LoadAssetBatch

type AssetBatchLoaded = shared.events.AssetEvent.AssetBatchLoaded
val AssetBatchLoaded: shared.events.AssetEvent.AssetBatchLoaded.type = shared.events.AssetEvent.AssetBatchLoaded

type AssetBatchLoadError = shared.events.AssetEvent.AssetBatchLoadError
val AssetBatchLoadError: shared.events.AssetEvent.AssetBatchLoadError.type =
  shared.events.AssetEvent.AssetBatchLoadError

// Data

type FontChar = shared.datatypes.FontChar
val FontChar: shared.datatypes.FontChar.type = shared.datatypes.FontChar

type FontInfo = shared.datatypes.FontInfo
val FontInfo: shared.datatypes.FontInfo.type = shared.datatypes.FontInfo

type FontKey = shared.datatypes.FontKey
val FontKey: shared.datatypes.FontKey.type = shared.datatypes.FontKey

type TextAlignment = shared.datatypes.TextAlignment
val TextAlignment: shared.datatypes.TextAlignment.type = shared.datatypes.TextAlignment

type Rectangle = shared.datatypes.Rectangle
val Rectangle: shared.datatypes.Rectangle.type = shared.datatypes.Rectangle

type Point = shared.datatypes.Point
val Point: shared.datatypes.Point.type = shared.datatypes.Point

type Size = shared.datatypes.Size
val Size: shared.datatypes.Size.type = shared.datatypes.Size

type Vector2 = shared.datatypes.Vector2
val Vector2: shared.datatypes.Vector2.type = shared.datatypes.Vector2

type Vector3 = shared.datatypes.Vector3
val Vector3: shared.datatypes.Vector3.type = shared.datatypes.Vector3

type Vector4 = shared.datatypes.Vector4
val Vector4: shared.datatypes.Vector4.type = shared.datatypes.Vector4

type Matrix3 = shared.datatypes.Matrix3
val Matrix3: shared.datatypes.Matrix3.type = shared.datatypes.Matrix3

type Matrix4 = shared.datatypes.Matrix4
val Matrix4: shared.datatypes.Matrix4.type = shared.datatypes.Matrix4

type Depth = shared.datatypes.Depth
val Depth: shared.datatypes.Depth.type = shared.datatypes.Depth

type Radians = shared.datatypes.Radians
val Radians: shared.datatypes.Radians.type = shared.datatypes.Radians

type BindingKey = shared.datatypes.BindingKey
val BindingKey: shared.datatypes.BindingKey.type = shared.datatypes.BindingKey

type Fill = shared.datatypes.Fill
val Fill: shared.datatypes.Fill.type = shared.datatypes.Fill

type Stroke = shared.datatypes.Stroke
val Stroke: shared.datatypes.Stroke.type = shared.datatypes.Stroke

type RGB = shared.datatypes.RGB
val RGB: shared.datatypes.RGB.type = shared.datatypes.RGB

type RGBA = shared.datatypes.RGBA
val RGBA: shared.datatypes.RGBA.type = shared.datatypes.RGBA

type Flip = shared.datatypes.Flip
val Flip: shared.datatypes.Flip.type = shared.datatypes.Flip

// shared

type AssetType = shared.assets.AssetType
val AssetType: shared.assets.AssetType.type = shared.assets.AssetType

type GameConfig = shared.config.GameConfig
val GameConfig: shared.config.GameConfig.type = shared.config.GameConfig

type GameViewport = shared.config.GameViewport
val GameViewport: shared.config.GameViewport.type = shared.config.GameViewport

type AdvancedGameConfig = shared.config.AdvancedGameConfig
val AdvancedGameConfig: shared.config.AdvancedGameConfig.type = shared.config.AdvancedGameConfig

type RenderingTechnology = shared.config.RenderingTechnology
val RenderingTechnology: shared.config.RenderingTechnology.type = shared.config.RenderingTechnology

val IndigoLogger: shared.IndigoLogger.type = shared.IndigoLogger

type Aseprite = shared.formats.Aseprite
val Aseprite: shared.formats.Aseprite.type = shared.formats.Aseprite

type SpriteAndAnimations = shared.formats.SpriteAndAnimations
val SpriteAndAnimations: shared.formats.SpriteAndAnimations.type = shared.formats.SpriteAndAnimations

type TiledMap = shared.formats.TiledMap
val TiledMap: shared.formats.TiledMap.type = shared.formats.TiledMap

type TiledGridMap[A] = shared.formats.TiledGridMap[A]
val TiledGridMap: shared.formats.TiledGridMap.type = shared.formats.TiledGridMap

type TiledGridLayer[A] = shared.formats.TiledGridLayer[A]
val TiledGridLayer: shared.formats.TiledGridLayer.type = shared.formats.TiledGridLayer

type TiledGridCell[A] = shared.formats.TiledGridCell[A]
val TiledGridCell: shared.formats.TiledGridCell.type = shared.formats.TiledGridCell

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

type FrameContext[StartUpData] = shared.FrameContext[StartUpData]
type SubSystemFrameContext     = shared.subsystems.SubSystemFrameContext

//WebSockets

type WebSocketEvent = shared.networking.WebSocketEvent
val WebSocketEvent: shared.networking.WebSocketEvent.type = shared.networking.WebSocketEvent

type WebSocketConfig = shared.networking.WebSocketConfig
val WebSocketConfig: shared.networking.WebSocketConfig.type = shared.networking.WebSocketConfig

type WebSocketId = shared.networking.WebSocketId
val WebSocketId: shared.networking.WebSocketId.type = shared.networking.WebSocketId

type WebSocketReadyState = shared.networking.WebSocketReadyState
val WebSocketReadyState: shared.networking.WebSocketReadyState.type = shared.networking.WebSocketReadyState

// Http

val HttpMethod: shared.networking.HttpMethod.type = shared.networking.HttpMethod

type HttpRequest = shared.networking.HttpRequest
val HttpRequest: shared.networking.HttpRequest.type = shared.networking.HttpRequest

type HttpReceiveEvent = shared.networking.HttpReceiveEvent
val HttpReceiveEvent: shared.networking.HttpReceiveEvent.type = shared.networking.HttpReceiveEvent

val HttpError: shared.networking.HttpReceiveEvent.HttpError.type = shared.networking.HttpReceiveEvent.HttpError

type HttpResponse = shared.networking.HttpReceiveEvent.HttpResponse
val HttpResponse: shared.networking.HttpReceiveEvent.HttpResponse.type = shared.networking.HttpReceiveEvent.HttpResponse

// Scene graph

type SceneUpdateFragment = shared.scenegraph.SceneUpdateFragment
val SceneUpdateFragment: shared.scenegraph.SceneUpdateFragment.type = shared.scenegraph.SceneUpdateFragment

type Camera = shared.scenegraph.Camera
val Camera: shared.scenegraph.Camera.type = shared.scenegraph.Camera

type Zoom = shared.scenegraph.Zoom
val Zoom: shared.scenegraph.Zoom.type = shared.scenegraph.Zoom

type Layer = shared.scenegraph.Layer
val Layer: shared.scenegraph.Layer.type = shared.scenegraph.Layer

type Blending = shared.scenegraph.Blending
val Blending: shared.scenegraph.Blending.type = shared.scenegraph.Blending

type Blend = shared.scenegraph.Blend
val Blend: shared.scenegraph.Blend.type = shared.scenegraph.Blend

type BlendFactor = shared.scenegraph.BlendFactor
val BlendFactor: shared.scenegraph.BlendFactor.type = shared.scenegraph.BlendFactor

type SceneNode = shared.scenegraph.SceneNode
val SceneNode: shared.scenegraph.SceneNode.type = shared.scenegraph.SceneNode

type EntityNode    = shared.scenegraph.EntityNode
type DependentNode = shared.scenegraph.DependentNode
type RenderNode    = shared.scenegraph.RenderNode

// Audio
type SceneAudio = shared.scenegraph.SceneAudio
val SceneAudio: shared.scenegraph.SceneAudio.type = shared.scenegraph.SceneAudio

type Volume = indigo.shared.audio.Volume
val Volume: indigo.shared.audio.Volume.type = indigo.shared.audio.Volume

type Track = indigo.shared.audio.Track
val Track: indigo.shared.audio.Track.type = indigo.shared.audio.Track

type PlaybackPattern = shared.scenegraph.PlaybackPattern
val PlaybackPattern: shared.scenegraph.PlaybackPattern.type = shared.scenegraph.PlaybackPattern

type SceneAudioSource = shared.scenegraph.SceneAudioSource
val SceneAudioSource: shared.scenegraph.SceneAudioSource.type = shared.scenegraph.SceneAudioSource

// Animation
type Animation = indigo.shared.animation.Animation
val Animation: indigo.shared.animation.Animation.type = indigo.shared.animation.Animation

type Cycle = indigo.shared.animation.Cycle
val Cycle: indigo.shared.animation.Cycle.type = indigo.shared.animation.Cycle

type CycleLabel = indigo.shared.animation.CycleLabel
val CycleLabel: indigo.shared.animation.CycleLabel.type = indigo.shared.animation.CycleLabel

type Frame = indigo.shared.animation.Frame
val Frame: indigo.shared.animation.Frame.type = indigo.shared.animation.Frame

type AnimationKey = indigo.shared.animation.AnimationKey
val AnimationKey: indigo.shared.animation.AnimationKey.type = indigo.shared.animation.AnimationKey

type AnimationAction = indigo.shared.animation.AnimationAction
val AnimationAction: indigo.shared.animation.AnimationAction.type = indigo.shared.animation.AnimationAction

// Primitives
type Shape = shared.scenegraph.Shape
val Shape: shared.scenegraph.Shape.type = shared.scenegraph.Shape

type Sprite[M <: Material] = shared.scenegraph.Sprite[M]
val Sprite: shared.scenegraph.Sprite.type = shared.scenegraph.Sprite

type Text[M <: Material] = shared.scenegraph.Text[M]
val Text: shared.scenegraph.Text.type = shared.scenegraph.Text

type Graphic[M <: Material] = shared.scenegraph.Graphic[M]
val Graphic: shared.scenegraph.Graphic.type = shared.scenegraph.Graphic

type Group = shared.scenegraph.Group
val Group: shared.scenegraph.Group.type = shared.scenegraph.Group

type TextBox = shared.scenegraph.TextBox
val TextBox: shared.scenegraph.TextBox.type = shared.scenegraph.TextBox

// TextStyle

type TextStyle = shared.datatypes.TextStyle
val TextStyle: shared.datatypes.TextStyle.type = shared.datatypes.TextStyle

type Font = shared.datatypes.Font
val Font: shared.datatypes.Font.type = shared.datatypes.Font

type FontFamily = shared.datatypes.FontFamily
val FontFamily: shared.datatypes.FontFamily.type = shared.datatypes.FontFamily

type FontVariant = shared.datatypes.FontVariant
val FontVariant: shared.datatypes.FontVariant.type = shared.datatypes.FontVariant

type FontStyle = shared.datatypes.FontStyle
val FontStyle: shared.datatypes.FontStyle.type = shared.datatypes.FontStyle

type FontWeight = shared.datatypes.FontWeight
val FontWeight: shared.datatypes.FontWeight.type = shared.datatypes.FontWeight

type TextStroke = shared.datatypes.TextStroke
val TextStroke: shared.datatypes.TextStroke.type = shared.datatypes.TextStroke

type Pixels = shared.datatypes.Pixels
val Pixels: shared.datatypes.Pixels.type = shared.datatypes.Pixels

type TextAlign = shared.datatypes.TextAlign
val TextAlign: shared.datatypes.TextAlign.type = shared.datatypes.TextAlign

type TextBaseLine = shared.datatypes.TextBaseLine
val TextBaseLine: shared.datatypes.TextBaseLine.type = shared.datatypes.TextBaseLine

type TextDirection = shared.datatypes.TextDirection
val TextDirection: shared.datatypes.TextDirection.type = shared.datatypes.TextDirection

// Clones
type Cloneable = shared.scenegraph.Cloneable

type CloneBlank = shared.scenegraph.CloneBlank
val CloneBlank: shared.scenegraph.CloneBlank.type = shared.scenegraph.CloneBlank

type CloneId = shared.scenegraph.CloneId
val CloneId: shared.scenegraph.CloneId.type = shared.scenegraph.CloneId

type Clone = shared.scenegraph.Clone
val Clone: shared.scenegraph.Clone.type = shared.scenegraph.Clone

type CloneBatch = shared.scenegraph.CloneBatch
val CloneBatch: shared.scenegraph.CloneBatch.type = shared.scenegraph.CloneBatch

type CloneTransformData = shared.scenegraph.CloneTransformData
val CloneTransformData: shared.scenegraph.CloneTransformData.type = shared.scenegraph.CloneTransformData

// Lights
type PointLight = shared.scenegraph.PointLight
val PointLight: shared.scenegraph.PointLight.type = shared.scenegraph.PointLight

type SpotLight = shared.scenegraph.SpotLight
val SpotLight: shared.scenegraph.SpotLight.type = shared.scenegraph.SpotLight

type DirectionLight = shared.scenegraph.DirectionLight
val DirectionLight: shared.scenegraph.DirectionLight.type = shared.scenegraph.DirectionLight

type AmbientLight = shared.scenegraph.AmbientLight
val AmbientLight: shared.scenegraph.AmbientLight.type = shared.scenegraph.AmbientLight

type Falloff = shared.scenegraph.Falloff
val Falloff: shared.scenegraph.Falloff.type = shared.scenegraph.Falloff
