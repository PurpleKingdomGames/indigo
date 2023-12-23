package indigoextras.trees

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.Vertex

trait SpatialOps[S]:
  def bounds(ref: S): BoundingBox
  def within(ref: S, bounds: BoundingBox): Boolean
  def equals(ref: S, other: S): Boolean
  def distance(ref: S, vertex: Vertex): Double

object SpatialOps:

  given SpatialOps[Vertex] with
    def bounds(ref: Vertex): BoundingBox                  = BoundingBox.fromVertexCloud(Batch(ref))
    def within(ref: Vertex, bounds: BoundingBox): Boolean = bounds.contains(ref)
    def equals(ref: Vertex, other: Vertex): Boolean       = ref ~== other
    def distance(ref: Vertex, vertex: Vertex): Double     = ref.distanceTo(vertex)

  given SpatialOps[Point] with
    def bounds(ref: Point): BoundingBox                  = BoundingBox.fromVertexCloud(Batch(ref.toVertex))
    def within(ref: Point, bounds: BoundingBox): Boolean = bounds.contains(ref.toVertex)
    def equals(ref: Point, other: Point): Boolean        = ref == other
    def distance(ref: Point, vertex: Vertex): Double     = ref.toVertex.distanceTo(vertex)

  given SpatialOps[BoundingBox] with
    def bounds(ref: BoundingBox): BoundingBox                  = ref
    def within(ref: BoundingBox, bounds: BoundingBox): Boolean = bounds.overlaps(ref)
    def equals(ref: BoundingBox, other: BoundingBox): Boolean  = ref ~== other
    def distance(ref: BoundingBox, vertex: Vertex): Double     = ref.sdf(vertex)

  given SpatialOps[Rectangle] with
    def bounds(ref: Rectangle): BoundingBox                  = ref.toBoundingBox
    def within(ref: Rectangle, bounds: BoundingBox): Boolean = bounds.overlaps(ref.toBoundingBox)
    def equals(ref: Rectangle, other: Rectangle): Boolean    = ref == other
    def distance(ref: Rectangle, vertex: Vertex): Double     = ref.toBoundingBox.sdf(vertex)
