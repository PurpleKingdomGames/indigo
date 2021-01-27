package indigo.shared.scenegraph

import scala.annotation.tailrec

final case class Layer(nodes: List[SceneGraphNode]) {

  def |+|(other: Layer): Layer =
    Layer(nodes ++ other.nodes)

  def ++(moreNodes: List[SceneGraphNode]): Layer =
    Layer(nodes ++ moreNodes)

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

}

object Layer {

  def None: Layer =
    Layer(Nil)

}
