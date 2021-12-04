package indigo.shared.scenegraph

import indigo.shared.datatypes._
import indigo.shared.shader.UniformBlock

/** Represents many identical clones of the same clone blank, differentiated only by their shader data. Intended for use
  * with custom entities in particular.
  */
final case class Mutants(
    id: CloneId,
    depth: Depth,
    uniformBlocks: Array[List[UniformBlock]]
) extends DependentNode
    derives CanEqual:

  lazy val scale: Vector2    = Vector2.one
  lazy val rotation: Radians = Radians.zero
  lazy val ref: Point        = Point.zero
  lazy val position: Point   = Point.zero
  lazy val flip: Flip        = Flip.default

  def withCloneId(newCloneId: CloneId): Mutants =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): Mutants =
    this.copy(depth = newDepth)

  def addBlocks(additionalBlocks: Array[List[UniformBlock]]): Mutants =
    this.copy(uniformBlocks = uniformBlocks ++ additionalBlocks)

object Mutants:

  def apply(id: CloneId, uniformBlocks: Array[List[UniformBlock]]): Mutants =
    Mutants(
      id,
      Depth.zero,
      uniformBlocks
    )

  def apply(id: CloneId, uniformBlocks: List[UniformBlock]): Mutants =
    Mutants(
      id,
      Depth.zero,
      Array(uniformBlocks)
    )
