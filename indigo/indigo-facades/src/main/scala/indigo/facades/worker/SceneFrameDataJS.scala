package indigo.facades.worker

import scala.scalajs.js

trait SceneFrameDataJS extends js.Object {
  val gameTime: GameTimeJS
  val scene: SceneUpdateFragmentJS
  val assetMapping: js.Dictionary[TextureRefAndOffsetJS]
  val screenWidth: Double
  val screenHeight: Double
  val orthographicProjectionMatrix: js.Array[Double]
}

trait Vector2JS extends js.Object {
  val x: Double
  val y: Double
}

trait PointJS extends js.Object {
  val x: Int
  val y: Int
}

trait RectangleJS extends js.Object {
  val position: PointJS
  val size: PointJS
}

trait RGBAJS extends js.Object {
  val r: Double
  val g: Double
  val b: Double
  val a: Double
}

trait RGBJS extends js.Object {
  val r: Double
  val g: Double
  val b: Double
}

trait FlipJS extends js.Object {
  val horizontal: Boolean
  val vertical: Boolean
}

trait AnimationActionJS extends js.Object {
  val _action: String
  val label: js.UndefOr[String]
  val to: js.UndefOr[Int]
}

trait SceneGraphNodeJS extends js.Object {
  val _type: String
}

trait CloneTransformDataJS extends js.Object {
  val position: PointJS
  val rotation: Double
  val scale: Vector2JS
  val alpha: Double
  val flipHorizontal: Boolean
  val flipVertical: Boolean
}

trait CloneJS extends js.Object {
  val _type: String
  val id: String
  val depth: Int
  val transform: CloneTransformDataJS
}

trait CloneBatchJS extends js.Object {
  val _type: String
  val id: String
  val depth: Int
  val transform: CloneTransformDataJS
  val clones: js.Array[CloneTransformDataJS]
  val staticBatchKey: js.UndefOr[String]
}

trait GroupJS extends js.Object {
  val children: js.Array[SceneGraphNodeJS]
  val position: PointJS
  val rotation: Double
  val scale: Vector2JS
  val depth: Int
  val ref: PointJS
  val flip: FlipJS
}

trait OverlayJS extends js.Object {
  val _type: String

  // Color
  val color: js.UndefOr[RGBAJS]

  // Linear Gradiant
  val fromPoint: js.UndefOr[PointJS]
  val fromColor: js.UndefOr[RGBAJS]
  val toPoint: js.UndefOr[PointJS]
  val toColor: js.UndefOr[RGBAJS]
}

trait BorderJS extends js.Object {
  val color: RGBAJS
  val innerThickness: Int
  val outerThickness: Int
}

trait GlowJS extends js.Object {
  val color: RGBAJS
  val innerGlowAmount: Double
  val outerGlowAmount: Double
}

trait EffectsJS extends js.Object {
  val tint: RGBAJS
  val overlay: OverlayJS
  val border: BorderJS
  val glow: GlowJS
  val alpha: Double
}

trait TextureJS extends js.Object {
  val assetName: String
  val amount: Double
}

trait MaterialJS extends js.Object {
  val _type: String
  val isLit: Boolean

  // Textured
  val diffuse: String

  // Lit
  val albedo: String
  val emissive: js.UndefOr[TextureJS]
  val normal: js.UndefOr[TextureJS]
  val specular: js.UndefOr[TextureJS]
}

trait TextJS extends js.Object {
  val text: String
  val alignment: String
  val fontKey: String
  val effects: EffectsJS
  val position: PointJS
  val rotation: Double
  val scale: Vector2JS
  val depth: Int
  val ref: PointJS
  val flip: FlipJS
}

trait SpriteJS extends js.Object {
  val bindingKey: String
  val animationKey: String
  val animationActions: js.Array[AnimationActionJS]
  val effects: EffectsJS
  val position: PointJS
  val rotation: Double
  val scale: Vector2JS
  val depth: Int
  val ref: PointJS
  val flip: FlipJS
}

trait GraphicJS extends js.Object {
  val material: MaterialJS
  val crop: RectangleJS
  val effects: EffectsJS
  val position: PointJS
  val rotation: Double
  val scale: Vector2JS
  val depth: Int
  val ref: PointJS
  val flip: FlipJS
}

trait FrameJS extends js.Object {
  val crop: RectangleJS
  val duration: Double
  val frameMaterial: js.UndefOr[MaterialJS]
}

trait CycleJS extends js.Object {
  val label: String
  val frames: js.Array[FrameJS]
  val playheadPosition: Int
  val lastFrameAdvance: Double
}

trait AnimationJS extends js.Object {
  val animationKey: String
  val material: MaterialJS
  val currentCycleLabel: String
  val cycles: js.Array[CycleJS]
}

trait FontCharJS extends js.Object {
  val character: String
  val bounds: RectangleJS
}

trait FontSpriteSheetJS extends js.Object {
  val material: MaterialJS
  val size: PointJS
}

trait FontInfoJS extends js.Object {
  val fontKey: String
  val fontSpriteSheet: FontSpriteSheetJS
  val unknownChar: FontCharJS
  val fontChars: js.Array[FontCharJS]
  val caseSensitive: Boolean
}

trait LightJS extends js.Object {
  val _type: String
  val position: PointJS
  val height: Int
  val color: RGBJS
  val power: Double
  val attenuation: Int
  val angle: Double
  val rotation: Double
  val near: Int
  val far: Int
}

trait CloneBlankJS extends js.Object {
  val id: String
  val cloneable: SceneGraphNodeJS
}

trait ScreenEffectsJS extends js.Object {
  val gameColorOverlay: RGBAJS
  val uiColorOverlay: RGBAJS
}

trait SceneAudioSourceJS extends js.Object {
  val _type: String
  val bindingKey: String
  val masterVolume: Double
  val assetName: String
  val volume: Double
}

trait SceneAudioJS extends js.Object {
  val sourceA: SceneAudioSourceJS
  val sourceB: SceneAudioSourceJS
  val sourceC: SceneAudioSourceJS
}

trait SceneLayerJS extends js.Object {
  val nodes: js.Array[SceneGraphNodeJS]
  val tint: RGBAJS
  val saturation: Double
  val magnification: js.UndefOr[Int]
}

trait SceneUpdateFragmentJS extends js.Object {
  val gameLayer: SceneLayerJS
  val lightingLayer: SceneLayerJS
  val distortionLayer: SceneLayerJS
  val uiLayer: SceneLayerJS
  val ambientLight: RGBAJS
  val lights: js.Array[LightJS]
  val audio: SceneAudioJS
  val screenEffects: ScreenEffectsJS
  val cloneBlanks: js.Array[CloneBlankJS]
}

trait GameTimeJS extends js.Object {
  val running: Double
  val delta: Double
  val targetFPS: Int
}

trait TextureRefAndOffsetJS extends js.Object {
  val atlasName: String
  val atlasSize: Vector2JS
  val offset: PointJS
}
