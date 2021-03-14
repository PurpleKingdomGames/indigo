package indigo.shared.scenegraph

import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Depth
import indigo.shared.materials.BlendMaterial
import scala.annotation.nowarn
import indigo.shared.datatypes.RGBA

/**
  * A layers are used to stack collections screen elements on top of one another.
  *
  * During the scene render, each layer in depth order is _blended_ into the one
  * below it, a bit like doing a foldLeft over a list. You can control how the blend
  * is performed to create effects.
  *
  * Layer fields are all either Lists or options to denote that you _can_ have them
  * but that it isn't necessary. Layers are "monoids" which just means that they
  * can be empty and they can be combined. It is important to note that when they
  * combine they are left bias in the case of all optional fields, which means, that
  * if you do: a.show |+| b.hide, the layer will be visible. This may look odd, and maybe
  * it is (time will tell!), but the idea is that you can set empty placeholder layers
  * early in your scene and then add things to them, confident of the outcome.
  *
  * @param nodes
  * @param key
  * @param magnification
  * @param depth
  * @param visible
  * @param blending
  */
final case class Layer(
    nodes: List[SceneNode],
    backgroundColor: Option[RGBA],
    key: Option[BindingKey],
    magnification: Option[Int],
    depth: Option[Depth],
    visible: Option[Boolean],
    blending: Option[Blending]
) {

  def |+|(other: Layer): Layer =
    this.copy(
      nodes = nodes ++ other.nodes,
      backgroundColor = backgroundColor.orElse(other.backgroundColor),
      key = key.orElse(other.key),
      magnification = magnification.orElse(other.magnification),
      depth = depth.orElse(other.depth),
      visible = visible.orElse(other.visible),
      blending = blending.orElse(other.blending)
    )
  def combine(other: Layer): Layer =
    this |+| other

  def withNodes(newNodes: List[SceneNode]): Layer =
    this.copy(nodes = newNodes)
  def addNodes(moreNodes: List[SceneNode]): Layer =
    withNodes(nodes ++ moreNodes)
  def ++(moreNodes: List[SceneNode]): Layer =
    addNodes(moreNodes)

  def withBackgroundColor(newBackgroundColor: RGBA): Layer =
    this.copy(backgroundColor = Option(newBackgroundColor))

  def withMagnification(level: Int): Layer =
    this.copy(magnification = Option(Math.max(1, Math.min(256, level))))

  def withKey(newKey: BindingKey): Layer =
    this.copy(key = Option(newKey))

  def withDepth(newDepth: Depth): Layer =
    this.copy(depth = Option(newDepth))

  def withVisibility(isVisible: Boolean): Layer =
    this.copy(visible = Option(isVisible))

  def show: Layer =
    withVisibility(true)

  def hide: Layer =
    withVisibility(false)

  def withBlending(newBlending: Blending): Layer =
    this.copy(blending = Option(newBlending))
  def withEntityBlend(newBlend: Blend): Layer =
    this.copy(blending = blending.map(_.withEntityBlend(newBlend)))
  def withLayerBlend(newBlend: Blend): Layer =
    this.copy(blending = blending.map(_.withLayerBlend(newBlend)))
  def withBlendMaterial(newBlendMaterial: BlendMaterial): Layer =
    this.copy(blending = blending.map(_.withBlendMaterial(newBlendMaterial)))
  def modifyBlending(modifier: Blending => Blending): Layer =
    this.copy(blending = blending.map(modifier))
}

object Layer {

  def empty: Layer =
    Layer(Nil, None, None, None, None, None, None)

  def apply(nodes: SceneNode*): Layer =
    Layer(nodes.toList, None, None, None, None, None, None)

  def apply(nodes: List[SceneNode]): Layer =
    Layer(nodes, None, None, None, None, None, None)

  def apply(key: BindingKey, nodes: List[SceneNode]): Layer =
    Layer(nodes, None, Option(key), None, None, None, None)

  def apply(key: BindingKey, magnification: Int, depth: Depth)(nodes: SceneNode*): Layer =
    Layer(nodes.toList, None, Option(key), Option(magnification), Option(depth), None, None)

  def apply(key: BindingKey): Layer =
    Layer(Nil, None, Option(key), None, None, None, None)

  def apply(key: BindingKey, magnification: Int, depth: Depth): Layer =
    Layer(Nil, None, Option(key), Option(magnification), Option(depth), None, None)

}

final case class Blending(entity: Blend, layer: Blend, blendMaterial: BlendMaterial) {

  def withEntityBlend(newBlend: Blend): Blending =
    this.copy(entity = newBlend)

  def withLayerBlend(newBlend: Blend): Blending =
    this.copy(layer = newBlend)

  def withBlendMaterial(newBlendMaterial: BlendMaterial): Blending =
    this.copy(blendMaterial = newBlendMaterial)

}
object Blending {

  def apply(blend: Blend): Blending =
    Blending(blend, blend, BlendMaterial.Normal)

  val Normal: Blending =
    Blending(Blend.Normal, Blend.Normal, BlendMaterial.Normal)
  val Alpha: Blending =
    Blending(Blend.Alpha, Blend.Alpha, BlendMaterial.Normal)

  /**
    * Specifically replicates Indigo's lighting layer behaviour
    */
  val Lighting: Blending =
    Blending(Blend.LightingEntity, Blend.LightingLayer, BlendMaterial.Normal)
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
    Alpha
  val LightingLayer: Blend =
    Min(BlendFactor.SrcColor, BlendFactor.DstColor)
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
