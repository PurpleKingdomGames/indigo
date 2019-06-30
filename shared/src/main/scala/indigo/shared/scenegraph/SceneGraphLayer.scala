package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import scala.annotation.tailrec

final class SceneGraphLayer(val nodes: List[SceneGraphNode]) extends AnyVal
object SceneGraphLayer {
  val empty: SceneGraphLayer =
    new SceneGraphLayer(Nil)

  def collectViewEvents(nodes: List[SceneGraphNode], inputEvents: List[GlobalEvent]): List[GlobalEvent] = {
    @tailrec
    def eventRec(remaining: List[GlobalEvent], node: Renderable, acc: List[GlobalEvent]): List[GlobalEvent] =
      remaining match {
        case Nil =>
          acc

        case e :: es =>
          eventRec(es, node, acc ++ node.eventHandlerWithBoundsApplied(e))
      }

    @tailrec
    def nodeRec(remaining: List[SceneGraphNode], acc: List[GlobalEvent]): List[GlobalEvent] =
      remaining match {
        case Nil =>
          acc

        case (n: Renderable) :: ns =>
          nodeRec(ns, eventRec(inputEvents, n, Nil))

        case _ :: ns =>
          nodeRec(ns, acc)
      }

    nodeRec(nodes, Nil)
  }

}
