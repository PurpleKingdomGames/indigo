package indigo.shared.geometry

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Vector2

import scala.annotation.tailrec

sealed trait Polygon derives CanEqual:
  val vertices: Batch[Vertex]

  def moveTo(newPosition: Vertex): Polygon
  def moveTo(x: Double, y: Double): Polygon =
    moveTo(Vertex(x, y))
  def moveTo(newPosition: Vector2): Polygon =
    moveTo(Vertex.fromVector2(newPosition))

  def moveBy(amount: Vertex): Polygon
  def moveBy(x: Double, y: Double): Polygon =
    moveBy(Vertex(x, y))
  def moveBy(amount: Vector2): Polygon =
    moveBy(Vertex.fromVector2(amount))

  def scaleBy(vec: Vertex): Polygon
  def scaleBy(amount: Double): Polygon =
    scaleBy(Vertex(amount))
  def scaleBy(vec: Vector2): Polygon =
    scaleBy(Vertex.fromVector2(vec))

  def bounds: BoundingBox =
    BoundingBox.fromVertices(vertices)

  def edgeCount: Int =
    this match
      case Polygon.Open(vs) =>
        vs.length - 1

      case Polygon.Closed(vs) =>
        vs.length

  lazy val lineSegments: Batch[LineSegment] =
    Polygon.toLineSegments(this)

  def addVertex(vertex: Vertex): Polygon =
    this match
      case Polygon.Open(vs) =>
        Polygon.Open(vs ++ Batch(vertex))

      case Polygon.Closed(vs) =>
        Polygon.Closed(vs ++ Batch(vertex))

  def contains(vertex: Vertex): Boolean =
    this match
      case Polygon.Open(_) =>
        false

      case p @ Polygon.Closed(_) =>
        bounds.contains(vertex) && p.lineSegments.forall(l => !l.isFacingVertex(vertex))

  def lineIntersectCheck(lineSegment: LineSegment): Boolean =
    lineSegments.exists(_.intersectsWith(lineSegment))

  def rectangleIntersectCheck(rectangle: Rectangle): Boolean =
    Polygon.fromRectangle(rectangle).lineSegments.exists(lineIntersectCheck)

  def polygonIntersectCheck(polygon: Polygon): Boolean =
    polygon.lineSegments.exists(lineIntersectCheck)

  override def toString: String =
    this match
      case Polygon.Open(vs) =>
        s"Polygon.Open(${vs.toString()})"

      case Polygon.Closed(vs) =>
        s"Polygon.Closed(${vs.toString()})"

object Polygon:

  def fromRectangle(rectangle: Rectangle): Closed =
    Closed(
      Vertex.fromPoint(rectangle.topLeft),
      Vertex.fromPoint(rectangle.bottomLeft),
      Vertex.fromPoint(rectangle.bottomRight),
      Vertex.fromPoint(rectangle.topRight)
    )

  def fromBoundingBox(boundingBox: BoundingBox): Closed =
    Closed(
      boundingBox.topLeft,
      boundingBox.bottomLeft,
      boundingBox.bottomRight,
      boundingBox.topRight
    )

  def toLineSegments(polygon: Polygon): Batch[LineSegment] =
    @tailrec
    def rec(remaining: List[Vertex], current: Vertex, acc: Batch[LineSegment]): Batch[LineSegment] =
      remaining match
        case Nil =>
          acc.reverse

        case x :: xs =>
          rec(xs, x, LineSegment(current, x) :: acc)

    polygon match
      case Open(b) if b.isEmpty =>
        Batch.empty

      case Closed(b) if b.isEmpty =>
        Batch.empty

      case Open(b) =>
        rec(b.tail.toList, b.head, Batch.empty)

      case Closed(b) =>
        rec(b.tail.toList ++ List(b.head), b.head, Batch.empty)

  final case class Open(vertices: Batch[Vertex]) extends Polygon:
    def moveTo(newPosition: Vertex): Open =
      moveBy(vertices.headOption.map(v => newPosition - v).getOrElse(Vertex.zero))

    def moveBy(amount: Vertex): Open =
      this.copy(vertices = vertices.map(_.moveBy(amount)))

    def scaleBy(vec: Vertex): Open =
      this.copy(vertices = vertices.map(_.scaleBy(vec)))

  object Open:

    val empty: Open =
      Open(Batch.empty)

    def apply(vertices: Vertex*): Open =
      new Open(Batch.fromSeq(vertices))

  final case class Closed(vertices: Batch[Vertex]) extends Polygon:
    def moveTo(newPosition: Vertex): Closed =
      moveBy(vertices.headOption.map(v => newPosition - v).getOrElse(Vertex.zero))

    def moveBy(amount: Vertex): Closed =
      this.copy(vertices = vertices.map(_.moveBy(amount)))

    def scaleBy(vec: Vertex): Closed =
      this.copy(vertices = vertices.map(_.scaleBy(vec)))

  object Closed:

    val empty: Closed =
      Closed(Batch.empty)

    def apply(vertices: Vertex*): Closed =
      new Closed(Batch.fromSeq(vertices))
