package indigoextras.trees

import indigo.BoundingBox
import indigo.shared.collections.Batch
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
    def giveBounds(ref: Vertex): BoundingBox = BoundingBox.fromVertexCloud(Batch(ref))
    def withinBounds(ref: Vertex, bounds: BoundingBox): Boolean =
      bounds.contains(ref)
    def equals(ref: Vertex, other: Vertex): Boolean       = ref ~== other
    def giveDistance(ref: Vertex, vertex: Vertex): Double = ref.distanceTo(vertex)
