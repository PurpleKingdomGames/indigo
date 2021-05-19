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
      def withPosition(newPosition: Point): Group =
        group.copy(position = newPosition)
      def withRotation(newRotation: Radians): Group =
        group.copy(rotation = newRotation)
      def withScale(newScale: Vector2): Group =
        group.copy(scale = newScale)
      def withDepth(newDepth: Depth): Group =
        group.copy(depth = newDepth)
      def withFlip(newFlip: Flip): Group =
        group.copy(flip = newFlip)

  given spatialGroup(using bs: BasicSpatial[Group]): Spatial[Group] with
    extension (group: Group)
      def moveTo(pt: Point): Group =
        group.copy(position = pt)
      def moveTo(x: Int, y: Int): Group =
        moveTo(Point(x, y))

      def moveBy(pt: Point): Group =
        moveTo(group.position + pt)
      def moveBy(x: Int, y: Int): Group =
        moveBy(Point(x, y))

      def rotateTo(angle: Radians): Group =
        group.copy(rotation = angle)
      def rotateBy(angle: Radians): Group =
        rotateTo(group.rotation + angle)

      def scaleBy(amount: Vector2): Group =
        group.copy(scale = group.scale * amount)
      def scaleBy(x: Double, y: Double): Group =
        scaleBy(Vector2(x, y))

      def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Group =
        group.copy(position = newPosition, rotation = newRotation, scale = newScale)
      def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Group =
        transformTo(group.position + positionDiff, group.rotation + rotationDiff, group.scale * scaleDiff)

      def withRef(newRef: Point): Group =
        group.copy(ref = newRef)
      def withRef(x: Int, y: Int): Group =
        withRef(Point(x, y))

      def flipHorizontal(isFlipped: Boolean): Group =
        group.copy(flip = group.flip.withHorizontalFlip(isFlipped))
      def flipVertical(isFlipped: Boolean): Group =
        group.copy(flip = group.flip.withVerticalFlip(isFlipped))
}
