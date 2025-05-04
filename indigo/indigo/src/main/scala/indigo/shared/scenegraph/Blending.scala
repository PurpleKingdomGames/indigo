package indigo.shared.scenegraph

import indigo.shared.datatypes.RGBA
import indigo.shared.materials.BlendMaterial

/** Blending instances tell Indigo how to blend the entities onto a layer, and then how to blend the layer onto the
  * layers below it.
  *
  * @param entity
  *   The blending mode to use when blending the entities in this layer onto the layer below.
  * @param layer
  *   The blending mode to use when blending this layer onto the layers below it.
  * @param blendMaterial
  *   The blending material to use when blending the entities in this layer onto the layer below.
  * @param clearColor
  *   The color to use when clearing the layer before blending. If None, a transparent value will be used. The purpose
  *   of clear colour is to allow effect that require the layer to be a uniform colour before merge, for example, in the
  *   image based lighting blend mode.
  */
final case class Blending(entity: Blend, layer: Blend, blendMaterial: BlendMaterial, clearColor: Option[RGBA])
    derives CanEqual {

  def withClearColor(clearColor: RGBA): Blending =
    this.copy(clearColor = Option(clearColor))

  def withEntityBlend(newBlend: Blend): Blending =
    this.copy(entity = newBlend)

  def withLayerBlend(newBlend: Blend): Blending =
    this.copy(layer = newBlend)

  def withBlendMaterial(newBlendMaterial: BlendMaterial): Blending =
    this.copy(blendMaterial = newBlendMaterial)

}
object Blending {

  def apply(blend: Blend): Blending =
    Blending(blend, blend, BlendMaterial.Normal, None)

  val Normal: Blending =
    Blending(Blend.Normal, Blend.Normal, BlendMaterial.Normal, None)
  val Alpha: Blending =
    Blending(Blend.Alpha, Blend.Alpha, BlendMaterial.Normal, None)

  /** Replicates Indigo's original lighting layer behaviour
    */
  def Lighting(ambientLightColor: RGBA): Blending =
    Blending(Blend.LightingEntity, Blend.Normal, BlendMaterial.Lighting(ambientLightColor), Option(RGBA.Black))

}

sealed trait Blend {
  def op: String
  def src: BlendFactor
  def dst: BlendFactor
}
object Blend {
  given CanEqual[Blend, Blend] = CanEqual.derived

  final case class Add(src: BlendFactor, dst: BlendFactor) extends Blend {
    val op: String = "add"
  }
  final case class Subtract(src: BlendFactor, dst: BlendFactor) extends Blend {
    val op: String = "subtract"
  }
  final case class ReverseSubtract(src: BlendFactor, dst: BlendFactor) extends Blend {
    val op: String = "reverse subtract"
  }
  final case class Min(src: BlendFactor, dst: BlendFactor) extends Blend {
    val op: String = "min"
  }
  final case class Max(src: BlendFactor, dst: BlendFactor) extends Blend {
    val op: String = "max"
  }
  final case class Lighten(src: BlendFactor, dst: BlendFactor) extends Blend {
    val op: String = "lighten"
  }
  final case class Darken(src: BlendFactor, dst: BlendFactor) extends Blend {
    val op: String = "darken"
  }

  val Normal: Blend =
    Add(BlendFactor.One, BlendFactor.OneMinusSrcAlpha)
  val Alpha: Blend =
    Add(BlendFactor.SrcAlpha, BlendFactor.DstAlpha)
  val LightingEntity: Blend =
    Lighten(BlendFactor.SrcAlpha, BlendFactor.DstAlpha)
}

enum BlendFactor derives CanEqual:
  case Zero, One, SrcColor, DstColor, SrcAlpha, DstAlpha, OneMinusSrcColor, OneMinusDstColor, OneMinusSrcAlpha,
    OneMinusDstAlpha, SrcAlphaSaturate
