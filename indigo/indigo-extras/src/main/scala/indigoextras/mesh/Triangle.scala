package indigoextras.mesh

import indigo.shared.collections.Batch
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.BoundingCircle
import indigo.shared.geometry.LineSegment
import indigo.shared.geometry.Vertex

final case class Triangle(a: Vertex, b: Vertex, c: Vertex):
  val vertices: Batch[Vertex] = Batch(a, b, c)
  val closed: Batch[Vertex]   = Batch(a, b, c, a)

  def withinCircumcirle(vertex: Vertex): Boolean =
    BoundingCircle.fromThreeVertices(a, b, c) match
      case None     => false
      case Some(bc) => bc.contains(vertex)

  def toLineSegments: Batch[LineSegment] =
    Batch(
      LineSegment(a, b),
      LineSegment(b, c),
      LineSegment(c, a)
    )

object Triangle:

  def fromVertices(vertices: List[Vertex]): Option[Triangle] =
    if vertices.length == 3 then Option(Triangle(vertices(0), vertices(1), vertices(2)))
    else None

  def fromVertices(vertices: Batch[Vertex]): Option[Triangle] =
    if vertices.length == 3 then Option(Triangle(vertices(0), vertices(1), vertices(2)))
    else None

  def fromVertices(vertices: Vertex*): Option[Triangle] =
    fromVertices(vertices.toList)

  /** Make a triangle big enough to comfortably contain a point cloud by calculating a suitable padding value.
    */
  def encompassing(pointCloud: Batch[Vertex]): Triangle =
    val bb      = BoundingBox.fromVertexCloud(pointCloud)
    val half    = bb.halfSize
    val padding = Math.max(half.x, half.y)
    encompassing(bb, padding)

  /** Make a triangle to encompass a point cloud, with a given amount of padding. */
  def encompassing(pointCloud: Batch[Vertex], padding: Double): Triangle =
    encompassing(BoundingBox.fromVertexCloud(pointCloud).expand(padding))

  /** Make a triangle big enough to exactly contain a BoundingBox. */
  def encompassing(b: BoundingBox): Triangle =
    encompassing(b, 0)

  /** Make a triangle big enough to contain a BoundingBox, with a given amount of padding.
    */
  def encompassing(b: BoundingBox, padding: Double): Triangle =
    val bb = b.expand(padding)
    val t  = Vertex(bb.center.x, bb.center.y - bb.height)
    val l  = Vertex(bb.center.x - bb.width, bb.bottom)
    val r  = Vertex(bb.center.x + bb.width, bb.bottom)

    Triangle(t, l, r)
