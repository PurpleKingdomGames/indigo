package indigo.shared.scenegraph

import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Depth

final case class Layer(nodes: List[SceneGraphNode], key: Option[BindingKey], magnification: Option[Int], depth: Option[Depth], visible: Boolean) {

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
}

object Layer {

  def empty: Layer =
    Layer(Nil, None, None, None, true)

  def apply(nodes: SceneGraphNode*): Layer =
    Layer(nodes.toList, None, None, None, true)

  def apply(nodes: List[SceneGraphNode]): Layer =
    Layer(nodes, None, None, None, true)

  def apply(key: BindingKey, magnification: Int, depth: Depth)(nodes: SceneGraphNode*): Layer =
    Layer(nodes.toList, Option(key), Option(magnification), Option(depth), true)

  def apply(key: BindingKey): Layer =
    Layer(Nil, Option(key), None, None, true)

  def apply(key: BindingKey, magnification: Int, depth: Depth): Layer =
    Layer(Nil, Option(key), Option(magnification), Option(depth), true)

}
