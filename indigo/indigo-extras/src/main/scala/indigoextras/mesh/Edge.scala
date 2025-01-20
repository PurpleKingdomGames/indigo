package indigoextras.mesh

import indigo.shared.collections.Batch

final case class Edge(vertexA: Int, vertexB: Int):
  def indices: Batch[Int] =
    Batch(vertexA, vertexB)

  /** Approx. equals of an edge means that if you reverse one of the edges, they end up being the same.
    */
  def ~==(other: Edge): Boolean =
    (this.vertexA == other.vertexA && this.vertexB == other.vertexB) ||
      (this.vertexA == other.vertexB && this.vertexB == other.vertexA)

object Edge:

  def fromIndices(indices: List[Int]): Option[Edge] =
    if indices.length == 2 then Option(Edge(indices(0), indices(1)))
    else None

  def fromIndices(indices: Int*): Option[Edge] =
    fromIndices(indices.toList)
