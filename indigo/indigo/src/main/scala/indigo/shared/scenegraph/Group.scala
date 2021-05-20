package indigo.shared.scenegraph

import indigo.shared.scenegraph.syntax.BasicSpatial
import indigo.shared.scenegraph.syntax.Spatial

import indigo.shared.datatypes._
import indigo.shared.BoundaryLocator

/** Used to group elements to allow them to be manipulated as a collection.
  */
final case class Group(
    children: List[SceneNode],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends DependentNode derives CanEqual {

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withDepth(newDepth: Depth): Group =
    this.copy(depth = newDepth)

  def calculatedBounds(locator: BoundaryLocator): Rectangle =
    val rect = locator.groupBounds(this)
    BoundaryLocator.findBounds(this, rect.position, rect.size, ref)

  def addChild(child: SceneNode): Group =
    this.copy(children = children ++ List(child))

  def addChildren(additionalChildren: List[SceneNode]): Group =
    this.copy(children = children ++ additionalChildren)
}

object Group {

  def apply(children: SceneNode*): Group =
    Group(children.toList, Point.zero, Radians.zero, Vector2.one, Depth.Zero, Point.zero, Flip.default)

  def apply(children: List[SceneNode]): Group =
    Group(children, Point.zero, Radians.zero, Vector2.one, Depth.Zero, Point.zero, Flip.default)

  def empty: Group =
    apply(Nil)

  given BasicSpatial[Group] with
    extension (group: Group)
      def position: Point =
        group.position
      def rotation: Radians =
        group.rotation
      def scale: Vector2 =
        group.scale
      def depth: Depth =
        group.depth
      def ref: Point =
        group.ref
      def flip: Flip =
        group.flip

      def withPosition(newPosition: Point): Group =
        group.copy(position = newPosition)
      def withRotation(newRotation: Radians): Group =
        group.copy(rotation = newRotation)
      def withScale(newScale: Vector2): Group =
        group.copy(scale = newScale)
      def withRef(newRef: Point): Group =
        group.copy(ref = newRef)
      def withRef(x: Int, y: Int): Group =
        withRef(Point(x, y))
      def withDepth(newDepth: Depth): Group =
        group.copy(depth = newDepth)
      def withFlip(newFlip: Flip): Group =
        group.copy(flip = newFlip)

  given Spatial[Group] = Spatial.default[Group]
}
