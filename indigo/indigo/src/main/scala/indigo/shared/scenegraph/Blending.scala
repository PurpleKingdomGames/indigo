package indigo.shared.scenegraph

import indigo.shared.materials.BlendMaterial
import indigo.shared.datatypes.RGBA
import scala.annotation.nowarn

final case class Blending(entity: Blend, layer: Blend, blendMaterial: BlendMaterial, clearColor: Option[RGBA]) {

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

  /**
    * Replicates Indigo's original lighting layer behaviour
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

sealed trait BlendFactor
object BlendFactor {

  case object Zero extends BlendFactor
  case object One extends BlendFactor {
    // No warns because arg is never used.
    // I've done this as overloads as these are the only allowed conversions
    // in WebGL, or I can think of an unsurprising result
    // i.e. One - Src is actually invalid
    @nowarn def -(factor: Zero.type): One.type                  = One
    @nowarn def -(factor: One.type): Zero.type                  = Zero
    @nowarn def -(factor: SrcColor.type): OneMinusSrcColor.type = OneMinusSrcColor
    @nowarn def -(factor: DstColor.type): OneMinusDstColor.type = OneMinusDstColor
    @nowarn def -(factor: SrcAlpha.type): OneMinusSrcAlpha.type = OneMinusSrcAlpha
    @nowarn def -(factor: DstAlpha.type): OneMinusDstAlpha.type = OneMinusDstAlpha
    @nowarn def -(factor: OneMinusSrcColor.type): SrcColor.type = SrcColor
    @nowarn def -(factor: OneMinusDstColor.type): DstColor.type = DstColor
    @nowarn def -(factor: OneMinusSrcAlpha.type): SrcAlpha.type = SrcAlpha
    @nowarn def -(factor: OneMinusDstAlpha.type): DstAlpha.type = DstAlpha
  }
  case object Src {
    def color: SrcColor.type = SrcColor
    def rgb: SrcColor.type   = SrcColor
    def alpha: SrcAlpha.type = SrcAlpha
    def a: SrcAlpha.type     = SrcAlpha
  }
  case object Dst {
    def color: DstColor.type = DstColor
    def rgb: DstColor.type   = DstColor
    def alpha: DstAlpha.type = DstAlpha
    def a: DstAlpha.type     = DstAlpha
  }
  case object SrcColor extends BlendFactor
  case object DstColor extends BlendFactor
  case object SrcAlpha extends BlendFactor {
    def saturate: SrcAlphaSaturate.type = SrcAlphaSaturate
  }
  case object DstAlpha         extends BlendFactor
  case object OneMinusSrcColor extends BlendFactor
  case object OneMinusDstColor extends BlendFactor
  case object OneMinusSrcAlpha extends BlendFactor
  case object OneMinusDstAlpha extends BlendFactor
  case object SrcAlphaSaturate extends BlendFactor

}
