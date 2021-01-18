package indigo.facades.worker

import indigo.shared.animation.Animation
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.Cycle
import indigo.shared.animation.Frame
import indigo.shared.datatypes.Material
import indigo.shared.datatypes.Texture
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontSpriteSheet
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.TextAlignment
import indigo.shared.datatypes.Effects
import indigo.shared.datatypes.Overlay
import indigo.shared.platform.SceneFrameData
import indigo.shared.platform.AssetMapping
import indigo.shared.platform.TextureRefAndOffset
import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.scenegraph.SceneAudio
import indigo.shared.scenegraph.SceneAudioSource
import indigo.shared.scenegraph.PlaybackPattern
import indigo.shared.scenegraph.SceneLayer
import indigo.shared.scenegraph.ScreenEffects
import indigo.shared.scenegraph.Light
import indigo.shared.scenegraph.PointLight
import indigo.shared.scenegraph.SpotLight
import indigo.shared.scenegraph.DirectionLight
import indigo.shared.scenegraph.CloneBlank
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.Sprite
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.Transformer
import indigo.shared.scenegraph.Clone
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.scenegraph.CloneTransformData

import scala.scalajs.js
import scalajs.js.JSConverters._

object WorkerConversions {

  // FontInfo

  // Animation

  // SceneFrameData

}

object FontInfoConversion {

  def toJS(fontInfo: FontInfo): js.Any =
    js.Dynamic.literal(
      fontKey = fontInfo.fontKey.key,
      fontSpriteSheet = FontSpriteSheetConversion.toJS(fontInfo.fontSpriteSheet),
      unknownChar = FontCharConversion.toJS(fontInfo.unknownChar),
      fontChars = fontInfo.fontChars.map(FontCharConversion.toJS).toJSArray,
      caseSensitive = fontInfo.caseSensitive
    )

  object FontSpriteSheetConversion {

    def toJS(fontSpriteSheet: FontSpriteSheet): js.Any =
      js.Dynamic.literal(
        material = MaterialConversion.toJS(fontSpriteSheet.material),
        size = PrimitiveConversion.pointToJS(fontSpriteSheet.size)
      )

  }

  object FontCharConversion {

    def toJS(fontChar: FontChar): js.Any =
      js.Dynamic.literal(
        character = fontChar.character,
        bounds = PrimitiveConversion.rectangleToJS(fontChar.bounds)
      )

  }

}

object AnimationConversion {

  def toJS(animation: Animation): js.Any =
    js.Dynamic.literal(
      animationKey = animation.animationKey.value,
      material = MaterialConversion.toJS(animation.material),
      currentCycleLabel = animation.currentCycleLabel.value,
      cycles = animation.cycles.toList.map(CycleConversion.toJS).toJSArray
    )

  object CycleConversion {

    def toJS(cycle: Cycle): js.Any =
      js.Dynamic.literal(
        label = cycle.label.value,
        frames = cycle.frames.toList.map(FrameConversion.toJS).toJSArray,
        playheadPosition = cycle.playheadPosition,
        lastFrameAdvance = cycle.lastFrameAdvance.value.toDouble
      )

  }

  object FrameConversion {

    def toJS(frame: Frame): js.Any =
      js.Dynamic.literal(
        crop = PrimitiveConversion.rectangleToJS(frame.crop),
        duration = frame.duration.value.toDouble,
        frameMaterial = frame.frameMaterial.map(MaterialConversion.toJS).orUndefined
      )

  }

}

object PrimitiveConversion {

  def rectangleToJS(rectangle: Rectangle): js.Any =
    js.Dynamic.literal(
      position = pointToJS(rectangle.position),
      size = pointToJS(rectangle.size)
    )

  def pointToJS(point: Point): js.Any =
    js.Dynamic.literal(
      x = point.x,
      y = point.y
    )

  def vector2ToJS(vector: Vector2): js.Any =
    js.Dynamic.literal(
      x = vector.x,
      y = vector.y
    )

}

object MaterialConversion {

  def toJS(material: Material): js.Any =
    material match {
      case Material.Textured(diffuse, isLit) =>
        js.Dynamic.literal(
          _type = "textured",
          diffuse = diffuse.value,
          isList = isLit
        )

      case Material.Lit(albedo, emissive, normal, specular, isLit) =>
        js.Dynamic.literal(
          _type = "textured",
          albedo = albedo.value,
          emissive = emissive.map(TextureConversion.toJS).orUndefined,
          normal = normal.map(TextureConversion.toJS).orUndefined,
          specular = specular.map(TextureConversion.toJS).orUndefined,
          isList = isLit
        )
    }

  object TextureConversion {

    def toJS(texture: Texture): js.Any =
      js.Dynamic.literal(
        assetName = texture.assetName.value,
        amount = texture.amount
      )

  }

}

object SceneFrameDataConversion {
  /*
  val scene: SceneUpdateFragment
  val assetMapping: AssetMapping
  val orthographicProjectionMatrix: CheapMatrix4
   */
  def toJS(sceneFrameData: SceneFrameData): js.Any =
    js.Dynamic.literal(
      gameTime = GameTimeConversion.toJS(sceneFrameData.gameTime),
      scene = SceneUpdateFragmentConversion.toJS(sceneFrameData.scene),
      assetMapping = sceneFrameData.assetMapping.mappings.map((k, v) => (k, TextureRefAndOffsetConversion.toJS(v))).toJSDictionary,
      screenWidth = sceneFrameData.screenWidth,
      screenHeight = sceneFrameData.screenHeight,
      orthographicProjectionMatrix = sceneFrameData.orthographicProjectionMatrix.mat.toJSArray
    )

  object GameTimeConversion {

    def toJS(gameTime: GameTime): js.Any =
      js.Dynamic.literal(
        running = gameTime.running.value,
        delta = gameTime.delta.value,
        targetFPS = gameTime.targetFPS.value
      )

  }

  object TextureRefAndOffsetConversion {

    //val atlasName: String, val atlasSize: Vector2, val offset: Point
    def toJS(textureRefAndOffset: TextureRefAndOffset): js.Any =
      js.Dynamic.literal(
        atlasName = textureRefAndOffset.atlasName,
        atlasSize = PrimitiveConversion.vector2ToJS(textureRefAndOffset.atlasSize),
        offset = PrimitiveConversion.pointToJS(textureRefAndOffset.offset)
      )

  }

}

object SceneUpdateFragmentConversion {
  /*
    gameLayer: SceneLayer,
    lightingLayer: SceneLayer,
    distortionLayer: SceneLayer,
    uiLayer: SceneLayer,
    ambientLight: RGBA,
    lights: List[Light],
    audio: SceneAudio,
    screenEffects: ScreenEffects,
    cloneBlanks: List[CloneBlank]
   */
  def toJS(suf: SceneUpdateFragment): js.Any =
    js.Dynamic.literal(
      gameLayer = SceneLayerConversion.toJS(suf.gameLayer),
      lightingLayer = SceneLayerConversion.toJS(suf.lightingLayer),
      distortionLayer = SceneLayerConversion.toJS(suf.distortionLayer),
      uiLayer = SceneLayerConversion.toJS(suf.uiLayer),
      ambientLight = RGBAConversion.toJS(suf.ambientLight),
      lights = suf.lights.map(LightConversion.toJS).toJSArray,
      audio = SceneAudioConversion.toJS(suf.audio),
      screenEffects = ScreenEffectsConversion.toJS(suf.screenEffects),
      cloneBlanks = suf.cloneBlanks.map(CloneBlankConversion.toJS).toJSArray
    )

  object SceneLayerConversion {

    def toJS(sceneLayer: SceneLayer): js.Any =
      js.Dynamic.literal(
        nodes = sceneLayer.nodes.map {
          case g: Graphic     => GraphicConversion.toJS(g)
          case g: Group       => GroupConversion.toJS(g)
          case s: Sprite      => SpriteConversion.toJS(s)
          case t: Text        => TextConversion.toJS(t)
          case c: Clone       => CloneConversion.toJS(c)
          case c: CloneBatch  => CloneBatchConversion.toJS(c)
          case t: Transformer => null
        }.toJSArray,
        tint = RGBAConversion.toJS(sceneLayer.tint),
        saturation = sceneLayer.saturation,
        magnification = sceneLayer.magnification.orUndefined
      )

  }

  object RGBConversion {

    def toJS(rgb: RGB): js.Any =
      js.Dynamic.literal(
        r = rgb.r,
        g = rgb.g,
        b = rgb.b
      )

  }

  object RGBAConversion {

    def toJS(rgba: RGBA): js.Any =
      js.Dynamic.literal(
        r = rgba.r,
        g = rgba.g,
        b = rgba.b,
        a = rgba.a
      )

  }

  object LightConversion {

    def toJS(light: Light): js.Any =
      light match {
        case PointLight(position, height, color, power, attenuation) =>
          js.Dynamic.literal(
            position = PrimitiveConversion.pointToJS(position),
            height = height,
            color = RGBConversion.toJS(color),
            power = power,
            attenuation = attenuation
          )

        case SpotLight(position, height, color, power, attenuation, angle, rotation, near, far) =>
          js.Dynamic.literal(
            position = PrimitiveConversion.pointToJS(position),
            height = height,
            color = RGBConversion.toJS(color),
            power = power,
            attenuation = attenuation,
            angle = angle.value,
            rotation = rotation.value,
            near = near,
            far = far
          )

        case DirectionLight(height, color, power, rotation) =>
          js.Dynamic.literal(
            height = height,
            power = power,
            color = RGBConversion.toJS(color),
            rotation = rotation.value
          )
      }

  }

  object SceneAudioConversion {

    def toJS(sceneAudio: SceneAudio): js.Any =
      js.Dynamic.literal(
        sourceA = SceneAudioSourceConversion.toJS(sceneAudio.sourceA),
        sourceB = SceneAudioSourceConversion.toJS(sceneAudio.sourceB),
        sourceC = SceneAudioSourceConversion.toJS(sceneAudio.sourceC)
      )

    object SceneAudioSourceConversion {

      def toJS(source: SceneAudioSource): js.Any =
        js.Dynamic.literal(
          bindingKey = source.bindingKey.value,
          playbackPattern = source.playbackPattern match {
            case PlaybackPattern.Silent =>
              js.Dynamic.literal(_type = "silent")

            case PlaybackPattern.SingleTrackLoop(track) =>
              js.Dynamic.literal(
                _type = "single",
                assetName = track.assetName.value,
                volume = track.volume.amount
              )
          },
          masterVolume = source.masterVolume.amount
        )

    }

  }

  object ScreenEffectsConversion {

    def toJS(screenEffects: ScreenEffects): js.Any =
      js.Dynamic.literal(
        gameColorOverlay = RGBAConversion.toJS(screenEffects.gameColorOverlay),
        uiColorOverlay = RGBAConversion.toJS(screenEffects.uiColorOverlay)
      )

  }

  object CloneBlankConversion {

    def toJS(cloneBlank: CloneBlank): js.Any =
      js.Dynamic.literal(
        id = cloneBlank.id.value,
        cloneable = cloneBlank.cloneable match {
          case s: Sprite  => SpriteConversion.toJS(s)
          case g: Graphic => GraphicConversion.toJS(g)
        }
      )

  }

  object CloneConversion {

    def toJS(node: Clone): js.Any =
      js.Dynamic.literal(
        id = node.id.value,
        depth = node.depth.zIndex,
        transform = CloneTransformDataConversion.toJS(node.transform)
      )

  }

  object CloneBatchConversion {

    def toJS(node: CloneBatch): js.Any =
      js.Dynamic.literal(
        id = node.id.value,
        depth = node.depth.zIndex,
        transform = CloneTransformDataConversion.toJS(node.transform),
        clones = node.clones.map(CloneTransformDataConversion.toJS).toJSArray,
        staticBatchKey = node.staticBatchKey.map(_.value).orUndefined
      )

  }

  object CloneTransformDataConversion {

    def toJS(data: CloneTransformData): js.Any =
      js.Dynamic.literal(
        position = PrimitiveConversion.pointToJS(data.position),
        rotation = data.rotation.value,
        scale = PrimitiveConversion.vector2ToJS(data.scale),
        alpha = data.alpha,
        flipHorizontal = data.flipHorizontal,
        flipVertical = data.flipVertical
      )

  }

  object SpriteConversion {

    def toJS(node: Sprite): js.Any =
      js.Dynamic.literal(
        bindingKey = node.bindingKey.value,
        animationKey = node.animationKey.value,
        animationActions = node.animationActions.map(AnimationActionConversion.toJS).toJSArray,
        eventHandler = null,
        effects = EffectsConversion.toJS(node.effects),
        position = PrimitiveConversion.pointToJS(node.position),
        rotation = node.rotation.value,
        scale = PrimitiveConversion.vector2ToJS(node.scale),
        depth = node.depth.zIndex,
        ref = PrimitiveConversion.pointToJS(node.ref),
        flip = FlipConversion.toJS(node.flip)
      )

  }

  object GraphicConversion {

    def toJS(node: Graphic): js.Any =
      js.Dynamic.literal(
        material = MaterialConversion.toJS(node.material),
        crop = PrimitiveConversion.rectangleToJS(node.crop),
        effects = EffectsConversion.toJS(node.effects),
        position = PrimitiveConversion.pointToJS(node.position),
        rotation = node.rotation.value,
        scale = PrimitiveConversion.vector2ToJS(node.scale),
        depth = node.depth.zIndex,
        ref = PrimitiveConversion.pointToJS(node.ref),
        flip = FlipConversion.toJS(node.flip)
      )

  }

  object TextConversion {

    def toJS(node: Text): js.Any =
      js.Dynamic.literal(
        text = node.text,
        alignment = node.alignment match {
          case TextAlignment.Left   => "left"
          case TextAlignment.Right  => "right"
          case TextAlignment.Center => "center"
        },
        fontKey = node.fontKey.key,
        effects = EffectsConversion.toJS(node.effects),
        eventHandler = null,
        position = PrimitiveConversion.pointToJS(node.position),
        rotation = node.rotation.value,
        scale = PrimitiveConversion.vector2ToJS(node.scale),
        depth = node.depth.zIndex,
        ref = PrimitiveConversion.pointToJS(node.ref),
        flip = FlipConversion.toJS(node.flip)
      )

  }

  object GroupConversion {

    def toJS(node: Group): js.Any =
      js.Dynamic.literal(
        children = node.children.map {
          case g: Group   => toJS(g)
          case s: Sprite  => SpriteConversion.toJS(s)
          case g: Graphic => GraphicConversion.toJS(g)
          case t: Text    => TextConversion.toJS(t)
        },
        position = PrimitiveConversion.pointToJS(node.position),
        rotation = node.rotation.value,
        scale = PrimitiveConversion.vector2ToJS(node.scale),
        depth = node.depth.zIndex,
        ref = PrimitiveConversion.pointToJS(node.ref),
        flip = FlipConversion.toJS(node.flip)
      )

  }

  object FlipConversion {

    def toJS(flip: Flip): js.Any =
      js.Dynamic.literal(
        horizontal = flip.horizontal,
        vertical = flip.vertical
      )

  }

  object AnimationActionConversion {

    def toJS(action: AnimationAction): js.Any =
      action match {
        case AnimationAction.Play             => js.Dynamic.literal(_action = "play")
        case AnimationAction.ChangeCycle(l)   => js.Dynamic.literal(_action = "change", label = l.value)
        case AnimationAction.JumpToFirstFrame => js.Dynamic.literal(_action = "first")
        case AnimationAction.JumpToLastFrame  => js.Dynamic.literal(_action = "last")
        case AnimationAction.JumpToFrame(num) => js.Dynamic.literal(_action = "jump", to = num)

      }

  }

  object EffectsConversion {

    def toJS(effects: Effects): js.Any =
      js.Dynamic.literal(
        tint = RGBAConversion.toJS(effects.tint),
        overlay = effects.overlay match {
          case Overlay.Color(rgba) =>
            js.Dynamic.literal(_type = "color", color = RGBAConversion.toJS(rgba))

          case Overlay.LinearGradiant(fromPoint, fromColor, toPoint, toColor) =>
            js.Dynamic.literal(
              _type = "linear gradiant",
              fromPoint = PrimitiveConversion.pointToJS(fromPoint),
              fromColor = RGBAConversion.toJS(fromColor),
              toPoint = PrimitiveConversion.pointToJS(toPoint),
              toColor = RGBAConversion.toJS(toColor)
            )
        },
        border = js.Dynamic.literal(
          color = RGBAConversion.toJS(effects.border.color),
          innerThickness = effects.border.innerThickness.hash,
          outerThickness = effects.border.outerThickness.hash
        ),
        glow = js.Dynamic.literal(
          color = RGBAConversion.toJS(effects.glow.color),
          innerGlowAmount = effects.glow.innerGlowAmount,
          outerGlowAmount = effects.glow.outerGlowAmount
        ),
        alpha = effects.alpha
      )

  }

}
