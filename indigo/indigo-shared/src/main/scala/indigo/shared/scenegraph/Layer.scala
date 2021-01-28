package indigo.shared.scenegraph

import scala.annotation.tailrec

final case class Layer(nodes: List[SceneGraphNode], magnification: Option[Int]) {

  def |+|(other: Layer): Layer =
    this.copy(nodes = nodes ++ other.nodes)

  def withNodes(newNodes: List[SceneGraphNode]): Layer =
    this.copy(nodes = newNodes)
  def addNodes(moreNodes: List[SceneGraphNode]): Layer =
    withNodes(nodes ++ moreNodes)
  def ++(moreNodes: List[SceneGraphNode]): Layer =
    addNodes(moreNodes)

  def visibleNodeCount: Int = {
    @tailrec
    def rec(remaining: List[SceneGraphNode], count: Int): Int =
      remaining match {
        case Nil =>
          count

        case (g: Group) :: xs =>
          rec(g.children ++ xs, count)

        case _ :: xs =>
          rec(xs, count + 1)
      }

    rec(nodes, 0)
  }

  def withMagnification(level: Int): Layer =
    this.copy(magnification = Option(Math.max(1, Math.min(256, level))))

}

object Layer {

  def None: Layer =
    Layer(Nil, Option.empty[Int])

  def apply(nodes: SceneGraphNode*): Layer =
    Layer(nodes.toList, Option.empty[Int])

  def apply(nodes: List[SceneGraphNode]): Layer =
    Layer(nodes, Option.empty[Int])

}
