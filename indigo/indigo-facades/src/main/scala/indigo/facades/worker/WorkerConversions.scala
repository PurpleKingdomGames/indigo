package indigo.facades.worker

import indigo.shared.animation.Animation
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.Cycle
import indigo.shared.animation.CycleLabel
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
import indigo.shared.datatypes.Border
import indigo.shared.datatypes.Thickness
import indigo.shared.datatypes.Glow
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
        size = PointConversion.toJS(fontSpriteSheet.size)
      )

  }

  object FontCharConversion {

    def toJS(fontChar: FontChar): js.Any =
      js.Dynamic.literal(
        character = fontChar.character,
        bounds = RectangleConversion.toJS(fontChar.bounds)
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
        crop = RectangleConversion.toJS(frame.crop),
        duration = frame.duration.value.toDouble,
        frameMaterial = frame.frameMaterial.map(MaterialConversion.toJS).orUndefined
      )

  }

}

object RectangleConversion {

  def toJS(rectangle: Rectangle): js.Any =
    js.Dynamic.literal(
      position = PointConversion.toJS(rectangle.position),
      size = PointConversion.toJS(rectangle.size)
    )

  def fromJS(obj: js.Any): Rectangle = {
    val res = obj.asInstanceOf[RectangleJS]
    Rectangle(PointConversion.fromJS(res.position), PointConversion.fromJS(res.size))
  }

}

object PointConversion {

  def toJS(point: Point): js.Any =
    js.Dynamic.literal(
      x = point.x,
      y = point.y
    )

  def fromJS(obj: js.Any): Point =
    fromPointJS(obj.asInstanceOf[PointJS])

  def fromPointJS(res: PointJS): Point =
    Point(res.x, res.y)
}

object Vector2Conversion {

  def toJS(vector: Vector2): js.Any =
    js.Dynamic.literal(
      x = vector.x,
      y = vector.y
    )

  def fromJS(obj: js.Any): Vector2 = {
    val res = obj.asInstanceOf[Vector2JS]
    Vector2(res.x, res.y)
  }

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

    def toJS(textureRefAndOffset: TextureRefAndOffset): js.Any =
      js.Dynamic.literal(
        atlasName = textureRefAndOffset.atlasName,
        atlasSize = Vector2Conversion.toJS(textureRefAndOffset.atlasSize),
        offset = PointConversion.toJS(textureRefAndOffset.offset)
      )

  }

}

object SceneUpdateFragmentConversion {

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

    def fromJS(obj: js.Any): RGB = {
      val res = obj.asInstanceOf[RGBJS]
      RGB(res.r, res.g, res.b)
    }

  }

  object RGBAConversion {

    def toJS(rgba: RGBA): js.Any =
      js.Dynamic.literal(
        r = rgba.r,
        g = rgba.g,
        b = rgba.b,
        a = rgba.a
      )

    def fromJS(obj: js.Any): RGBA = {
      val res = obj.asInstanceOf[RGBAJS]
      RGBA(res.r, res.g, res.b, res.a)
    }

    def fromRGBAJS(rgbaJS: RGBAJS): RGBA =
      RGBA(rgbaJS.r, rgbaJS.g, rgbaJS.b, rgbaJS.a)

  }

  object LightConversion {

    def toJS(light: Light): js.Any =
      light match {
        case PointLight(position, height, color, power, attenuation) =>
          js.Dynamic.literal(
            position = PointConversion.toJS(position),
            height = height,
            color = RGBConversion.toJS(color),
            power = power,
            attenuation = attenuation
          )

        case SpotLight(position, height, color, power, attenuation, angle, rotation, near, far) =>
          js.Dynamic.literal(
            position = PointConversion.toJS(position),
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
        _type = "clone",
        id = node.id.value,
        depth = node.depth.zIndex,
        transform = CloneTransformDataConversion.toJS(node.transform)
      )

    def fromJS(obj: js.Any): Clone =
      fromCloneJS(obj.asInstanceOf[CloneJS])

    def fromCloneJS(res: CloneJS): Clone =
      ???

  }

  object CloneBatchConversion {

    def toJS(node: CloneBatch): js.Any =
      js.Dynamic.literal(
        _type = "clone batch",
        id = node.id.value,
        depth = node.depth.zIndex,
        transform = CloneTransformDataConversion.toJS(node.transform),
        clones = node.clones.map(CloneTransformDataConversion.toJS).toJSArray,
        staticBatchKey = node.staticBatchKey.map(_.value).orUndefined
      )

    def fromJS(obj: js.Any): CloneBatch =
      fromCloneBatchJS(obj.asInstanceOf[CloneBatchJS])

    def fromCloneBatchJS(res: CloneBatchJS): CloneBatch =
      ???

  }

  object CloneTransformDataConversion {

    def toJS(data: CloneTransformData): js.Any =
      js.Dynamic.literal(
        position = PointConversion.toJS(data.position),
        rotation = data.rotation.value,
        scale = Vector2Conversion.toJS(data.scale),
        alpha = data.alpha,
        flipHorizontal = data.flipHorizontal,
        flipVertical = data.flipVertical
      )

  }

  object SpriteConversion {

    def toJS(node: Sprite): js.Any =
      js.Dynamic.literal(
        _type = "sprite",
        bindingKey = node.bindingKey.value,
        animationKey = node.animationKey.value,
        animationActions = node.animationActions.map(AnimationActionConversion.toJS).toJSArray,
        eventHandler = null,
        effects = EffectsConversion.toJS(node.effects),
        position = PointConversion.toJS(node.position),
        rotation = node.rotation.value,
        scale = Vector2Conversion.toJS(node.scale),
        depth = node.depth.zIndex,
        ref = PointConversion.toJS(node.ref),
        flip = FlipConversion.toJS(node.flip)
      )

    def fromJS(obj: js.Any): Sprite =
      fromSpriteJS(obj.asInstanceOf[SpriteJS])

    def fromSpriteJS(res: SpriteJS): Sprite =
      ???

  }

  object GraphicConversion {

    def toJS(node: Graphic): js.Any =
      js.Dynamic.literal(
        _type = "graphic",
        material = MaterialConversion.toJS(node.material),
        crop = RectangleConversion.toJS(node.crop),
        effects = EffectsConversion.toJS(node.effects),
        position = PointConversion.toJS(node.position),
        rotation = node.rotation.value,
        scale = Vector2Conversion.toJS(node.scale),
        depth = node.depth.zIndex,
        ref = PointConversion.toJS(node.ref),
        flip = FlipConversion.toJS(node.flip)
      )

    def fromJS(obj: js.Any): Graphic =
      fromGraphicJS(obj.asInstanceOf[GraphicJS])

    def fromGraphicJS(res: GraphicJS): Graphic =
      ???

  }

  object TextConversion {

    def toJS(node: Text): js.Any =
      js.Dynamic.literal(
        _type = "text",
        text = node.text,
        alignment = node.alignment match {
          case TextAlignment.Left   => "left"
          case TextAlignment.Right  => "right"
          case TextAlignment.Center => "center"
        },
        fontKey = node.fontKey.key,
        effects = EffectsConversion.toJS(node.effects),
        eventHandler = null,
        position = PointConversion.toJS(node.position),
        rotation = node.rotation.value,
        scale = Vector2Conversion.toJS(node.scale),
        depth = node.depth.zIndex,
        ref = PointConversion.toJS(node.ref),
        flip = FlipConversion.toJS(node.flip)
      )

    def fromJS(obj: js.Any): Text =
      fromTextJS(obj.asInstanceOf[TextJS])

    def fromTextJS(res: TextJS): Text =
      ???

  }

  object GroupConversion {

    def toJS(node: Group): js.Any =
      js.Dynamic.literal(
        _type = "group",
        children = node.children.map {
          case g: Group   => toJS(g)
          case s: Sprite  => SpriteConversion.toJS(s)
          case g: Graphic => GraphicConversion.toJS(g)
          case t: Text    => TextConversion.toJS(t)
        },
        position = PointConversion.toJS(node.position),
        rotation = node.rotation.value,
        scale = Vector2Conversion.toJS(node.scale),
        depth = node.depth.zIndex,
        ref = PointConversion.toJS(node.ref),
        flip = FlipConversion.toJS(node.flip)
      )

    trait GroupJS extends js.Object {
      val children: js.Array[SceneGraphNodeJS]
      val position: PointJS
      val rotation: Double
      val scale: Vector2JS
      val depth: Int
      val ref: PointJS
      val flip: FlipJS
    }

    def fromJS(obj: js.Any): Group =
      fromGroupJS(obj.asInstanceOf[GroupJS])

    def fromGroupJS(res: GroupJS): Group =
      ???

  }

  object FlipConversion {

    def toJS(flip: Flip): js.Any =
      js.Dynamic.literal(
        horizontal = flip.horizontal,
        vertical = flip.vertical
      )

    def fromJS(obj: js.Any): Flip = {
      val res = obj.asInstanceOf[FlipJS]
      Flip(res.horizontal, res.vertical)
    }

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

    def fromJS(obj: js.Any): AnimationAction = {
      val res = obj.asInstanceOf[AnimationActionJS]
      res._action match {
        case "play"   => AnimationAction.Play
        case "change" => AnimationAction.ChangeCycle(CycleLabel(res.label.get))
        case "first"  => AnimationAction.JumpToFirstFrame
        case "last"   => AnimationAction.JumpToLastFrame
        case "jump"   => AnimationAction.JumpToFrame(res.to.get)
        case _        => AnimationAction.Play
      }
    }

  }

  object EffectsConversion {

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

    def toJS(effects: Effects): js.Any =
      js.Dynamic.literal(
        tint = RGBAConversion.toJS(effects.tint),
        overlay = effects.overlay match {
          case Overlay.Color(rgba) =>
            js.Dynamic.literal(_type = "color", color = RGBAConversion.toJS(rgba))

          case Overlay.LinearGradiant(fromPoint, fromColor, toPoint, toColor) =>
            js.Dynamic.literal(
              _type = "linear gradiant",
              fromPoint = PointConversion.toJS(fromPoint),
              fromColor = RGBAConversion.toJS(fromColor),
              toPoint = PointConversion.toJS(toPoint),
              toColor = RGBAConversion.toJS(toColor)
            )
        },
        border = js.Dynamic.literal(
          color = RGBAConversion.toJS(effects.border.color),
          innerThickness = effects.border.innerThickness.toInt,
          outerThickness = effects.border.outerThickness.toInt
        ),
        glow = js.Dynamic.literal(
          color = RGBAConversion.toJS(effects.glow.color),
          innerGlowAmount = effects.glow.innerGlowAmount,
          outerGlowAmount = effects.glow.outerGlowAmount
        ),
        alpha = effects.alpha
      )

    def fromJS(obj: js.Any): Effects = {
      val res = obj.asInstanceOf[EffectsJS]

      val overlay =
        res.overlay._type match {
          case "color" =>
            Overlay.Color(RGBAConversion.fromRGBAJS(res.overlay.color.get))

          case "linear gradiant" =>
            Overlay.LinearGradiant(
              fromPoint = PointConversion.fromPointJS(res.overlay.fromPoint.get),
              fromColor = RGBAConversion.fromRGBAJS(res.overlay.fromColor.get),
              toPoint = PointConversion.fromPointJS(res.overlay.toPoint.get),
              toColor = RGBAConversion.fromRGBAJS(res.overlay.toColor.get)
            )

          case _ =>
            Overlay.Color.default

        }

      val border =
        Border(
          RGBAConversion.fromRGBAJS(res.glow.color),
          Thickness.fromInt(res.border.innerThickness),
          Thickness.fromInt(res.border.outerThickness)
        )

      val glow =
        Glow(
          RGBAConversion.fromRGBAJS(res.glow.color),
          res.glow.innerGlowAmount,
          res.glow.outerGlowAmount
        )

      Effects(
        tint = RGBAConversion.fromRGBAJS(res.tint),
        overlay = overlay,
        border = border,
        glow = glow,
        alpha = res.alpha
      )
    }

  }

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
