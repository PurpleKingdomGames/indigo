package indigoextras.trees

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.Vertex

trait SpatialOps[S]:
  def giveBounds(ref: S): BoundingBox
  def withinBounds(ref: S, bounds: BoundingBox): Boolean
  def equals(ref: S, other: S): Boolean
  def giveDistance(ref: S, vertex: Vertex): Double

  extension (ref: S)
    def bounds: BoundingBox                       = giveBounds(ref)
    def containedBy(bounds: BoundingBox): Boolean = withinBounds(ref, bounds)
    def ===(other: S): Boolean                    = equals(ref, other)
    def distanceTo(vertex: Vertex): Double        = giveDistance(ref, vertex)

  extension (b: BoundingBox) def contains(ref: S): Boolean = withinBounds(ref, b)

object SpatialOps:

  given SpatialOps[Vertex] with
    def giveBounds(ref: Vertex): BoundingBox                    = BoundingBox.fromVertexCloud(Batch(ref))
    def withinBounds(ref: Vertex, bounds: BoundingBox): Boolean = bounds.contains(ref)
    def equals(ref: Vertex, other: Vertex): Boolean             = ref ~== other
    def giveDistance(ref: Vertex, vertex: Vertex): Double       = ref.distanceTo(vertex)

  given SpatialOps[Point] with
    def giveBounds(ref: Point): BoundingBox                    = BoundingBox.fromVertexCloud(Batch(ref.toVertex))
    def withinBounds(ref: Point, bounds: BoundingBox): Boolean = bounds.contains(ref.toVertex)
    def equals(ref: Point, other: Point): Boolean              = ref == other
    def giveDistance(ref: Point, vertex: Vertex): Double       = ref.toVertex.distanceTo(vertex)
