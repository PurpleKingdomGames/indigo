package indigo.facades.worker

import indigo.shared.platform.ProcessedSceneData
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.display.DisplayClone
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.display.DisplayCloneBatchData
import indigo.shared.display.DisplayEffects
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayObject

import scala.scalajs.js
import scalajs.js.JSConverters._

object ProcessedSceneDataConversion {

  def toJS(res: ProcessedSceneData): js.Any =
    js.Dynamic.literal(
      gameProjection = res.gameProjection.mat.toJSArray,
      lightingProjection = res.lightingProjection.mat.toJSArray,
      uiProjection = res.uiProjection.mat.toJSArray,
      gameLayerDisplayObjects = res.gameLayerDisplayObjects.map(DisplayEntityConversion.toJS).toJSArray,
      lightingLayerDisplayObjects = res.lightingLayerDisplayObjects.map(DisplayEntityConversion.toJS).toJSArray,
      distortionLayerDisplayObjects = res.distortionLayerDisplayObjects.map(DisplayEntityConversion.toJS).toJSArray,
      uiLayerDisplayObjects = res.uiLayerDisplayObjects.map(DisplayEntityConversion.toJS).toJSArray,
      cloneBlankDisplayObjects = res.cloneBlankDisplayObjects.map((k, v) => (k, DisplayObjectConversion.toJS(v))).toJSDictionary,
      lights = res.lights.map(LightConversion.toJS).toJSArray,
      clearColor = RGBAConversion.toJS(res.clearColor),
      gameLayerColorOverlay = RGBAConversion.toJS(res.gameLayerColorOverlay),
      uiLayerColorOverlay = RGBAConversion.toJS(res.uiLayerColorOverlay),
      gameLayerTint = RGBAConversion.toJS(res.gameLayerTint),
      lightingLayerTint = RGBAConversion.toJS(res.lightingLayerTint),
      uiLayerTint = RGBAConversion.toJS(res.uiLayerTint),
      gameLayerSaturation = res.gameLayerSaturation,
      lightingLayerSaturation = res.lightingLayerSaturation,
      uiLayerSaturation = res.uiLayerSaturation
    )

  def fromJS(obj: js.Any): ProcessedSceneData =
    fromProcessedSceneDataJS(obj.asInstanceOf[ProcessedSceneDataJS])

  def fromProcessedSceneDataJS(res: ProcessedSceneDataJS): ProcessedSceneData =
    new ProcessedSceneData(
      gameProjection = CheapMatrix4(res.gameProjection.toArray),
      lightingProjection = CheapMatrix4(res.lightingProjection.toArray),
      uiProjection = CheapMatrix4(res.uiProjection.toArray),
      gameLayerDisplayObjects = res.gameLayerDisplayObjects.map(DisplayEntityConversion.fromDisplayEntityJS).toList,
      lightingLayerDisplayObjects = res.lightingLayerDisplayObjects.map(DisplayEntityConversion.fromDisplayEntityJS).toList,
      distortionLayerDisplayObjects = res.distortionLayerDisplayObjects.map(DisplayEntityConversion.fromDisplayEntityJS).toList,
      uiLayerDisplayObjects = res.uiLayerDisplayObjects.map(DisplayEntityConversion.fromDisplayEntityJS).toList,
      cloneBlankDisplayObjects = res.cloneBlankDisplayObjects.toMap.map((k, v) => (k, DisplayObjectConversion.fromDisplayObjectJS(v))),
      lights = res.lights.map(LightConversion.fromLightJS).toList,
      clearColor = RGBAConversion.fromRGBAJS(res.clearColor),
      gameLayerColorOverlay = RGBAConversion.fromRGBAJS(res.gameLayerColorOverlay),
      uiLayerColorOverlay = RGBAConversion.fromRGBAJS(res.uiLayerColorOverlay),
      gameLayerTint = RGBAConversion.fromRGBAJS(res.gameLayerTint),
      lightingLayerTint = RGBAConversion.fromRGBAJS(res.lightingLayerTint),
      uiLayerTint = RGBAConversion.fromRGBAJS(res.uiLayerTint),
      gameLayerSaturation = res.gameLayerSaturation,
      lightingLayerSaturation = res.lightingLayerSaturation,
      uiLayerSaturation = res.uiLayerSaturation
    )

}

object DisplayEntityConversion {

  def toJS(res: DisplayEntity): js.Any =
    res match {
      case d: DisplayClone      => DisplayCloneConversion.toJS(d)
      case d: DisplayCloneBatch => DisplayCloneBatchConversion.toJS(d)
      case d: DisplayObject     => DisplayObjectConversion.toJS(d)
    }

  def fromJS(obj: js.Any): DisplayEntity =
    fromDisplayEntityJS(obj.asInstanceOf[DisplayEntityJS])

  def fromDisplayEntityJS(res: DisplayEntityJS): DisplayEntity =
    res._type match {
      case "clone"          => DisplayCloneConversion.fromJS(res)
      case "clone batch"    => DisplayCloneBatchConversion.fromJS(res)
      case "display object" => DisplayObjectConversion.fromJS(res)
      case _                => DisplayObjectConversion.fromJS(res)
    }

}

object DisplayCloneConversion {

  def toJS(res: DisplayClone): js.Any =
    js.Dynamic.literal(
      _type = "clone",
      id = res.id,
      transform = res.transform.mat.toJSArray,
      z = res.z,
      alpha = res.alpha
    )

  def fromJS(obj: js.Any): DisplayClone =
    fromDisplayCloneJS(obj.asInstanceOf[DisplayCloneJS])

  def fromDisplayCloneJS(res: DisplayCloneJS): DisplayClone =
    new DisplayClone(
      id = res.id,
      transform = CheapMatrix4(res.transform.toArray),
      z = res.z,
      alpha = res.alpha
    )

}

object DisplayCloneBatchConversion {

  def toJS(res: DisplayCloneBatch): js.Any =
    js.Dynamic.literal(
      _type = "clone batch",
      id = res.id,
      z = res.z,
      clones = res.clones.map(DisplayCloneBatchDataConversion.toJS).toJSArray
    )

  def fromJS(obj: js.Any): DisplayCloneBatch =
    fromDisplayCloneBatchJS(obj.asInstanceOf[DisplayCloneBatchJS])

  def fromDisplayCloneBatchJS(res: DisplayCloneBatchJS): DisplayCloneBatch =
    new DisplayCloneBatch(
      id = res.id,
      z = res.z,
      clones = res.clones.map(DisplayCloneBatchDataConversion.fromDisplayCloneBatchDataJS).toList
    )

}

object DisplayCloneBatchDataConversion {

  def toJS(res: DisplayCloneBatchData): js.Any =
    js.Dynamic.literal(
      transform = res.transform.mat.toJSArray,
      alpha = res.alpha
    )

  def fromJS(obj: js.Any): DisplayCloneBatchData =
    fromDisplayCloneBatchDataJS(obj.asInstanceOf[DisplayCloneBatchDataJS])

  def fromDisplayCloneBatchDataJS(res: DisplayCloneBatchDataJS): DisplayCloneBatchData =
    new DisplayCloneBatchData(
      transform = CheapMatrix4(res.transform.toArray),
      alpha = res.alpha
    )

}

object DisplayEffectsConversion {

  def toJS(res: DisplayEffects): js.Any =
    js.Dynamic.literal(
      tint = res.tint.toJSArray,
      gradiantOverlayPositions = res.gradiantOverlayPositions.toJSArray,
      gradiantOverlayFromColor = res.gradiantOverlayFromColor.toJSArray,
      gradiantOverlayToColor = res.gradiantOverlayToColor.toJSArray,
      borderColor = res.borderColor.toJSArray,
      innerBorderAmount = res.innerBorderAmount,
      outerBorderAmount = res.outerBorderAmount,
      glowColor = res.glowColor.toJSArray,
      innerGlowAmount = res.innerGlowAmount,
      outerGlowAmount = res.outerGlowAmount,
      alpha = res.alpha
    )

  def fromJS(obj: js.Any): DisplayEffects =
    fromDisplayEffectsJS(obj.asInstanceOf[DisplayEffectsJS])

  def fromDisplayEffectsJS(res: DisplayEffectsJS): DisplayEffects =
    new DisplayEffects(
      tint = res.tint.toArray,
      gradiantOverlayPositions = res.gradiantOverlayPositions.toArray,
      gradiantOverlayFromColor = res.gradiantOverlayFromColor.toArray,
      gradiantOverlayToColor = res.gradiantOverlayToColor.toArray,
      borderColor = res.borderColor.toArray,
      innerBorderAmount = res.innerBorderAmount,
      outerBorderAmount = res.outerBorderAmount,
      glowColor = res.glowColor.toArray,
      innerGlowAmount = res.innerGlowAmount,
      outerGlowAmount = res.outerGlowAmount,
      alpha = res.alpha
    )

}

object DisplayObjectConversion {

  def toJS(res: DisplayObject): js.Any =
    js.Dynamic.literal(
      _type = "display object",
      transform = res.transform.mat.toJSArray,
      z = res.z,
      width = res.width,
      height = res.height,
      atlasName = res.atlasName,
      frameX = res.frameX,
      frameY = res.frameY,
      frameScaleX = res.frameScaleX,
      frameScaleY = res.frameScaleY,
      albedoAmount = res.albedoAmount,
      emissiveOffset = Vector2Conversion.toJS(res.emissiveOffset),
      emissiveAmount = res.emissiveAmount,
      normalOffset = Vector2Conversion.toJS(res.normalOffset),
      normalAmount = res.normalAmount,
      specularOffset = Vector2Conversion.toJS(res.specularOffset),
      specularAmount = res.specularAmount,
      isLit = res.isLit,
      effects = DisplayEffectsConversion.toJS(res.effects)
    )

  def fromJS(obj: js.Any): DisplayObject =
    fromDisplayObjectJS(obj.asInstanceOf[DisplayObjectJS])

  def fromDisplayObjectJS(res: DisplayObjectJS): DisplayObject =
    new DisplayObject(
      transform = CheapMatrix4(res.transform.toArray),
      z = res.z,
      width = res.width,
      height = res.height,
      atlasName = res.atlasName,
      frameX = res.frameX,
      frameY = res.frameY,
      frameScaleX = res.frameScaleX,
      frameScaleY = res.frameScaleY,
      albedoAmount = res.albedoAmount,
      emissiveOffset = Vector2Conversion.fromVector2JS(res.emissiveOffset),
      emissiveAmount = res.emissiveAmount,
      normalOffset = Vector2Conversion.fromVector2JS(res.normalOffset),
      normalAmount = res.normalAmount,
      specularOffset = Vector2Conversion.fromVector2JS(res.specularOffset),
      specularAmount = res.specularAmount,
      isLit = res.isLit,
      effects = DisplayEffectsConversion.fromDisplayEffectsJS(res.effects)
    )

}
