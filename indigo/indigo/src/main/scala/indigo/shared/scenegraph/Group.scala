package indigo.shared.scenegraph

import indigo.shared.datatypes._
import indigo.shared.BoundaryLocator

/** Used to group elements to allow them to be manipulated as a collection.
  */
final case class Group(
    children: List[RenderNode],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends CompositeNode
    with SpatialModifiers[Group] derives CanEqual {

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withDepth(newDepth: Depth): Group =
    this.copy(depth = newDepth)

  def withRef(newRef: Point): Group =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Group =
    withRef(Point(x, y))

  def moveTo(pt: Point): Group =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Group =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Group =
    moveTo(newPosition)

  def moveBy(pt: Point): Group =
    moveTo(position + pt)
  def moveBy(x: Int, y: Int): Group =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Group =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Group =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Group =
    rotateTo(newRotation)

  def scaleBy(x: Double, y: Double): Group =
    scaleBy(Vector2(x, y))
  def scaleBy(amount: Vector2): Group =
    this.copy(scale = scale * amount)
  def withScale(newScale: Vector2): Group =
    this.copy(scale = newScale)

  def flipHorizontal(isFlipped: Boolean): Group =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Group =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Group =
    this.copy(flip = newFlip)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Group =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Group =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def calculatedBounds(locator: BoundaryLocator): Option[Rectangle] = {
    def giveBounds(n: SceneNode): Option[Rectangle] =
      n match {
        case n: EntityNode =>
          Option(n.bounds)

        case n: CompositeNode =>
          n.calculatedBounds(locator)

        case _ =>
          None
      }

    children match {
      case Nil =>
        Option(Rectangle.zero)

      case x :: xs =>
        val maybe: Option[Rectangle] =
          xs.foldLeft(giveBounds(x)) { (acc, node) =>
            (acc, giveBounds(node)) match
              case (Some(a), Some(b)) => Option(Rectangle.expandToInclude(a, b))
              case (r @ Some(_), _)   => r
              case (_, r @ Some(_))   => r
              case (r, _)             => r
          }

        maybe match
          case None    => None
          case Some(b) => Option(b)
    }
  }

  def addChild(child: RenderNode): Group =
    this.copy(children = children ++ List(child))

  def addChildren(additionalChildren: List[RenderNode]): Group =
    this.copy(children = children ++ additionalChildren)
}

object Group {

  def apply(children: RenderNode*): Group =
    Group(children.toList, Point.zero, Radians.zero, Vector2.one, Depth.Zero, Point.zero, Flip.default)

  def apply(children: List[RenderNode]): Group =
    Group(children, Point.zero, Radians.zero, Vector2.one, Depth.Zero, Point.zero, Flip.default)

  def empty: Group =
    apply(Nil)
}
