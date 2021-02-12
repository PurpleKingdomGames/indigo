package indigo.shared.scenegraph

import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Depth
import scala.annotation.nowarn

final case class Layer(
    nodes: List[SceneGraphNode],
    key: Option[BindingKey],
    magnification: Option[Int],
    depth: Option[Depth],
    visible: Boolean,
    blending: Blending
) {

  def |+|(other: Layer): Layer =
    this.copy(
      nodes = nodes ++ other.nodes,
      key = (key, other.key) match {
        case (Some(k), Some(_)) => Some(k)
        case (Some(k), None)    => Some(k)
        case (None, Some(k))    => Some(k)
        case _                  => None
      },
      magnification = (magnification, other.magnification) match {
        case (Some(m), Some(_)) => Some(m)
        case (Some(m), None)    => Some(m)
        case (None, Some(m))    => Some(m)
        case _                  => None
      },
      depth = (depth, other.depth) match {
        case (Some(d), Some(_)) => Some(d)
        case (Some(d), None)    => Some(d)
        case (None, Some(d))    => Some(d)
        case _                  => None
      }
    )

  def withNodes(newNodes: List[SceneGraphNode]): Layer =
    this.copy(nodes = newNodes)
  def addNodes(moreNodes: List[SceneGraphNode]): Layer =
    withNodes(nodes ++ moreNodes)
  def ++(moreNodes: List[SceneGraphNode]): Layer =
    addNodes(moreNodes)

  def withMagnification(level: Int): Layer =
    this.copy(magnification = Option(Math.max(1, Math.min(256, level))))

  def withKey(newKey: BindingKey): Layer =
    this.copy(key = Option(newKey))

  def withDepth(newDepth: Depth): Layer =
    this.copy(depth = Option(newDepth))

  def withVisibility(isVisible: Boolean): Layer =
    this.copy(visible = isVisible)

  def show: Layer =
    withVisibility(true)

  def hide: Layer =
    withVisibility(false)

  def withBlending(newBlending: Blending): Layer =
    this.copy(blending = newBlending)
  def withEntityBlend(newBlend: Blend): Layer =
    this.copy(blending = blending.withEntityBlend(newBlend))
  def withLayerBlend(newBlend: Blend): Layer =
    this.copy(blending = blending.withLayerBlend(newBlend))
}

object Layer {

  def empty: Layer =
    Layer(Nil, None, None, None, true, Blending.Normal)

  def apply(nodes: SceneGraphNode*): Layer =
    Layer(nodes.toList, None, None, None, true, Blending.Normal)

  def apply(nodes: List[SceneGraphNode]): Layer =
    Layer(nodes, None, None, None, true, Blending.Normal)

  def apply(key: BindingKey, nodes: List[SceneGraphNode]): Layer =
    Layer(nodes, Option(key), None, None, true, Blending.Normal)

  def apply(key: BindingKey, magnification: Int, depth: Depth)(nodes: SceneGraphNode*): Layer =
    Layer(nodes.toList, Option(key), Option(magnification), Option(depth), true, Blending.Normal)

  def apply(key: BindingKey): Layer =
    Layer(Nil, Option(key), None, None, true, Blending.Normal)

  def apply(key: BindingKey, magnification: Int, depth: Depth): Layer =
    Layer(Nil, Option(key), Option(magnification), Option(depth), true, Blending.Normal)

}

final case class Blending(entity: Blend, layer: Blend) {

  def withEntityBlend(newBlend: Blend): Blending =
    this.copy(entity = newBlend)
  def withLayerBlend(newBlend: Blend): Blending =
    this.copy(layer = newBlend)

}
object Blending {
  def apply(blend: Blend): Blending =
    Blending(blend, blend)

  val Normal: Blending =
    Blending(Blend.Normal, Blend.Normal)
  val Alpha: Blending =
    Blending(Blend.Alpha, Blend.Alpha)
  val Lighting: Blending =
    Blending(Blend.Alpha, Blend.Normal)
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
