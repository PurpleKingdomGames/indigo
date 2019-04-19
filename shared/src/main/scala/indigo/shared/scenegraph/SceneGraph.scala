package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.datatypes._

object SceneGraphNode {
  def empty: Group = Group(Point.zero, Depth.Base, Nil)
}

sealed trait SceneGraphNode extends Product with Serializable {
  def bounds: Rectangle
  val depth: Depth

  def withDepth(depth: Depth): SceneGraphNode
  def moveTo(pt: Point): SceneGraphNode
  def moveTo(x: Int, y: Int): SceneGraphNode
  def moveBy(pt: Point): SceneGraphNode
  def moveBy(x: Int, y: Int): SceneGraphNode

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def flatten: List[Renderable] = {
    def rec(acc: List[Renderable]): List[Renderable] =
      this match {
        case l: Renderable =>
          l :: acc

        case b: Group =>
          b.children
            .map(c => c.withDepth(c.depth + b.depth).moveBy(b.positionOffset))
            .flatMap(n => n.flatten) ++ acc
      }

    rec(Nil)
  }
}

final case class Group(positionOffset: Point, depth: Depth, children: List[SceneGraphNode]) extends SceneGraphNode {

  def withDepth(depth: Depth): Group =
    this.copy(depth = depth)

  def moveTo(pt: Point): Group =
    this.copy(positionOffset = pt)
  def moveTo(x: Int, y: Int): Group =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Group =
    this.copy(
      positionOffset = this.positionOffset + pt
    )
  def moveBy(x: Int, y: Int): Group =
    moveBy(Point(x, y))

  def bounds: Rectangle =
    children match {
      case Nil =>
        Rectangle.zero

      case x :: xs =>
        xs.foldLeft(x.bounds) { (acc, node) =>
          Rectangle.expandToInclude(acc, node.bounds)
        }
    }

  def addChild(child: SceneGraphNode): Group =
    this.copy(children = children :+ child)

  def addChildren(additionalChildren: List[SceneGraphNode]): Group =
    this.copy(children = children ++ additionalChildren)

}

object Group {
  def apply(position: Point, depth: Depth, children: SceneGraphNode*): Group =
    Group(position, depth, children.toList)

  def apply(children: SceneGraphNode*): Group =
    Group(Point.zero, Depth.Base, children.toList)

  def apply(children: List[SceneGraphNode]): Group =
    Group(Point.zero, Depth.Base, children)
}

sealed trait Renderable extends SceneGraphNode {
  val bounds: Rectangle
  val effects: Effects
  val eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]

  def x: Int
  def y: Int

  def moveTo(pt: Point): Renderable
  def moveTo(x: Int, y: Int): Renderable

  def moveBy(pt: Point): Renderable
  def moveBy(x: Int, y: Int): Renderable

  def withDepth(depth: Depth): Renderable
  def withAlpha(a: Double): Renderable
  def withTint(tint: Tint): Renderable
  def withTint(red: Double, green: Double, blue: Double): Renderable
  def flipHorizontal(h: Boolean): Renderable
  def flipVertical(v: Boolean): Renderable

  def onEvent(e: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]): Renderable

  //TODO: Review this.
  def eventHandlerWithBoundsApplied(e: GlobalEvent): Option[GlobalEvent]

}

