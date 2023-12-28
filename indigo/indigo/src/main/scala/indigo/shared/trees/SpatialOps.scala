package indigo.shared.trees

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Circle
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.BoundingCircle
import indigo.shared.geometry.LineSegment
import indigo.shared.geometry.Vertex

trait SpatialOps[S]:

  /** The bounds of this ref */
  def bounds(ref: S): BoundingBox

  /** Does the ref intersect the bounds */
  def intersects(ref: S, bounds: BoundingBox): Boolean

  /** Does the ref intersect the line segement */
  def intersects(ref: S, lineSegment: LineSegment): Boolean

  /** Check if two refs are equal by some appropriate notion of equality. */
  def equals(ref: S, other: S): Boolean

  /** Distance from the ref to the vertex */
  def distance(ref: S, vertex: Vertex): Double

  /** The ref's bounds somehow cover the whole area of the provided bounding box. */
  def surrounds(ref: S, bounds: BoundingBox): Boolean

object SpatialOps:

  given SpatialOps[Vertex] with
    def bounds(ref: Vertex): BoundingBox                           = BoundingBox.fromVertexCloud(Batch(ref))
    def intersects(ref: Vertex, bounds: BoundingBox): Boolean      = bounds.contains(ref)
    def intersects(ref: Vertex, lineSegment: LineSegment): Boolean = lineSegment.contains(ref)
    def equals(ref: Vertex, other: Vertex): Boolean                = ref ~== other
    def distance(ref: Vertex, vertex: Vertex): Double              = ref.distanceTo(vertex)
    def surrounds(ref: Vertex, bounds: BoundingBox): Boolean       = false

  given SpatialOps[Point] with
    def bounds(ref: Point): BoundingBox                           = BoundingBox.fromVertexCloud(Batch(ref.toVertex))
    def intersects(ref: Point, bounds: BoundingBox): Boolean      = bounds.contains(ref.toVertex)
    def intersects(ref: Point, lineSegment: LineSegment): Boolean = lineSegment.contains(ref.toVertex)
    def equals(ref: Point, other: Point): Boolean                 = ref == other
    def distance(ref: Point, vertex: Vertex): Double              = ref.toVertex.distanceTo(vertex)
    def surrounds(ref: Point, bounds: BoundingBox): Boolean       = false

  given SpatialOps[BoundingBox] with
    def bounds(ref: BoundingBox): BoundingBox                           = ref
    def intersects(ref: BoundingBox, bounds: BoundingBox): Boolean      = bounds.overlaps(ref)
    def intersects(ref: BoundingBox, lineSegment: LineSegment): Boolean = ref.overlaps(lineSegment)
    def equals(ref: BoundingBox, other: BoundingBox): Boolean           = ref ~== other
    def distance(ref: BoundingBox, vertex: Vertex): Double              = ref.sdf(vertex)
    def surrounds(ref: BoundingBox, bounds: BoundingBox): Boolean       = ref.encompasses(bounds)

  given SpatialOps[Rectangle] with
    def bounds(ref: Rectangle): BoundingBox                           = ref.toBoundingBox
    def intersects(ref: Rectangle, bounds: BoundingBox): Boolean      = bounds.overlaps(ref.toBoundingBox)
    def intersects(ref: Rectangle, lineSegment: LineSegment): Boolean = ref.toBoundingBox.overlaps(lineSegment)
    def equals(ref: Rectangle, other: Rectangle): Boolean             = ref == other
    def distance(ref: Rectangle, vertex: Vertex): Double              = ref.toBoundingBox.sdf(vertex)
    def surrounds(ref: Rectangle, bounds: BoundingBox): Boolean       = ref.toBoundingBox.encompasses(bounds)

  given SpatialOps[BoundingCircle] with
    def bounds(ref: BoundingCircle): BoundingBox                           = ref.toIncircleBoundingBox
    def intersects(ref: BoundingCircle, bounds: BoundingBox): Boolean      = bounds.overlaps(ref)
    def intersects(ref: BoundingCircle, lineSegment: LineSegment): Boolean = ref.overlaps(lineSegment)
    def equals(ref: BoundingCircle, other: BoundingCircle): Boolean        = ref ~== other
    def distance(ref: BoundingCircle, vertex: Vertex): Double              = ref.sdf(vertex)
    def surrounds(ref: BoundingCircle, bounds: BoundingBox): Boolean = ref.toIncircleBoundingBox.encompasses(bounds)

  given SpatialOps[Circle] with
    def bounds(ref: Circle): BoundingBox                           = ref.toIncircleBoundingBox
    def intersects(ref: Circle, bounds: BoundingBox): Boolean      = bounds.overlaps(ref.toBoundingCircle)
    def intersects(ref: Circle, lineSegment: LineSegment): Boolean = ref.toBoundingCircle.overlaps(lineSegment)
    def equals(ref: Circle, other: Circle): Boolean                = ref == other
    def distance(ref: Circle, vertex: Vertex): Double              = ref.toBoundingCircle.sdf(vertex)
    def surrounds(ref: Circle, bounds: BoundingBox): Boolean       = ref.toIncircleBoundingBox.encompasses(bounds)

  given SpatialOps[LineSegment] with
    def bounds(ref: LineSegment): BoundingBox                           = ref.toBoundingBox
    def intersects(ref: LineSegment, bounds: BoundingBox): Boolean      = bounds.overlaps(ref)
    def intersects(ref: LineSegment, lineSegment: LineSegment): Boolean = lineSegment.intersectsWith(ref)
    def equals(ref: LineSegment, other: LineSegment): Boolean           = ref ~== other
    def distance(ref: LineSegment, vertex: Vertex): Double              = ref.sdf(vertex)
    def surrounds(ref: LineSegment, bounds: BoundingBox): Boolean       = false
