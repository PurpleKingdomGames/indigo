package indigoextras.trees

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.Vertex

trait SpatialOps[S]:

  /** The bounds of this ref */
  def bounds(ref: S): BoundingBox

  /** Does the ref intersect the bounds */
  def intersects(ref: S, bounds: BoundingBox): Boolean

  /** Check if two refs are equal by some appropriate notion of equality. */
  def equals(ref: S, other: S): Boolean

  /** Distance from the ref to the vertex */
  def distance(ref: S, vertex: Vertex): Double

  /** The ref's bounds somehow cover the whole area of the provided bounding box. */
  def surrounds(ref: S, bounds: BoundingBox): Boolean

object SpatialOps:

  given SpatialOps[Vertex] with
    def bounds(ref: Vertex): BoundingBox                      = BoundingBox.fromVertexCloud(Batch(ref))
    def intersects(ref: Vertex, bounds: BoundingBox): Boolean = bounds.contains(ref)
    def equals(ref: Vertex, other: Vertex): Boolean           = ref ~== other
    def distance(ref: Vertex, vertex: Vertex): Double         = ref.distanceTo(vertex)
    def surrounds(ref: Vertex, bounds: BoundingBox): Boolean  = false

  given SpatialOps[Point] with
    def bounds(ref: Point): BoundingBox                      = BoundingBox.fromVertexCloud(Batch(ref.toVertex))
    def intersects(ref: Point, bounds: BoundingBox): Boolean = bounds.contains(ref.toVertex)
    def equals(ref: Point, other: Point): Boolean            = ref == other
    def distance(ref: Point, vertex: Vertex): Double         = ref.toVertex.distanceTo(vertex)
    def surrounds(ref: Point, bounds: BoundingBox): Boolean  = false

  given SpatialOps[BoundingBox] with
    def bounds(ref: BoundingBox): BoundingBox                      = ref
    def intersects(ref: BoundingBox, bounds: BoundingBox): Boolean = bounds.overlaps(ref)
    def equals(ref: BoundingBox, other: BoundingBox): Boolean      = ref ~== other
    def distance(ref: BoundingBox, vertex: Vertex): Double         = ref.sdf(vertex)
    def surrounds(ref: BoundingBox, bounds: BoundingBox): Boolean  = ref.encompasses(bounds)

  given SpatialOps[Rectangle] with
    def bounds(ref: Rectangle): BoundingBox                      = ref.toBoundingBox
    def intersects(ref: Rectangle, bounds: BoundingBox): Boolean = bounds.overlaps(ref.toBoundingBox)
    def equals(ref: Rectangle, other: Rectangle): Boolean        = ref == other
    def distance(ref: Rectangle, vertex: Vertex): Double         = ref.toBoundingBox.sdf(vertex)
    def surrounds(ref: Rectangle, bounds: BoundingBox): Boolean  = ref.toBoundingBox.encompasses(bounds)
