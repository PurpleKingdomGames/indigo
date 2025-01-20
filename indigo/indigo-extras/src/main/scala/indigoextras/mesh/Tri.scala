package indigoextras.mesh

import indigo.shared.collections.Batch

final case class Tri(edgeA: Int, edgeB: Int, edgeC: Int) derives CanEqual:
  def indices: Batch[Int] =
    Batch(edgeA, edgeB, edgeC)

object Tri:

  def fromIndices(indices: List[Int]): Option[Tri] =
    if indices.length == 3 then Option(Tri(indices(0), indices(1), indices(2)))
    else None

  def fromIndices(indices: Int*): Option[Tri] =
    fromIndices(indices.toList)
