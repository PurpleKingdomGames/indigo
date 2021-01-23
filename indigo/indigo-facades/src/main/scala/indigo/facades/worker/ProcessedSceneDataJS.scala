package indigo.facades.worker

import scala.scalajs.js

trait ProcessedSceneDataJS extends js.Object {
  val gameProjection: js.Array[Double]
  val lightingProjection: js.Array[Double]
  val uiProjection: js.Array[Double]
  val gameLayerDisplayObjects: js.Array[DisplayEntityJS]
  val lightingLayerDisplayObjects: js.Array[DisplayEntityJS]
  val distortionLayerDisplayObjects: js.Array[DisplayEntityJS]
  val uiLayerDisplayObjects: js.Array[DisplayEntityJS]
  val cloneBlankDisplayObjects: js.Dictionary[DisplayObjectJS]
  val lights: js.Array[LightJS]
  val clearColor: RGBAJS
  val gameLayerColorOverlay: RGBAJS
  val uiLayerColorOverlay: RGBAJS
  val gameLayerTint: RGBAJS
  val lightingLayerTint: RGBAJS
  val uiLayerTint: RGBAJS
  val gameLayerSaturation: Double
  val lightingLayerSaturation: Double
  val uiLayerSaturation: Double
}

trait DisplayEntityJS extends js.Object {
  val _type: String
}

trait DisplayObjectJS extends js.Object {
  val transform: js.Array[Double]
  val z: Double
  val width: Float
  val height: Float
  val atlasName: String
  val frameX: Float
  val frameY: Float
  val frameScaleX: Float
  val frameScaleY: Float
  val albedoAmount: Float
  val emissiveOffset: Vector2JS
  val emissiveAmount: Float
  val normalOffset: Vector2JS
  val normalAmount: Float
  val specularOffset: Vector2JS
  val specularAmount: Float
  val isLit: Float
  val effects: DisplayEffectsJS
}

trait DisplayCloneJS extends js.Object {
  val id: String
  val transform: js.Array[Double]
  val z: Double
  val alpha: Float
}

trait DisplayCloneBatchJS extends js.Object {
  val id: String
  val z: Double
  val clones: js.Array[DisplayCloneBatchDataJS]
}

trait DisplayCloneBatchDataJS extends js.Object {
  val transform: js.Array[Double]
  val alpha: Float
}

trait DisplayEffectsJS extends js.Object {
  val tint: js.Array[Float]
  val gradiantOverlayPositions: js.Array[Float]
  val gradiantOverlayFromColor: js.Array[Float]
  val gradiantOverlayToColor: js.Array[Float]
  val borderColor: js.Array[Float]
  val innerBorderAmount: Float
  val outerBorderAmount: Float
  val glowColor: js.Array[Float]
  val innerGlowAmount: Float
  val outerGlowAmount: Float
  val alpha: Float
}
