package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.collections.Batch
import indigo.shared.datatypes._
import indigo.shared.events.GlobalEvent
import indigo.shared.shader.UniformBlock

/** Represents many identical clones of the same clone blank, differentiated only by their shader data. Intended for use
  * with custom entities in particular.
  */
final case class Mutants(
    id: CloneId,
    depth: Depth,
    uniformBlocks: Array[Batch[UniformBlock]]
) extends DependentNode[Mutants]
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

  def addBlocks(additionalBlocks: Array[Batch[UniformBlock]]): Mutants =
    this.copy(uniformBlocks = uniformBlocks ++ additionalBlocks)

  val eventHandlerEnabled: Boolean                                  = false
  def eventHandler: ((Mutants, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

object Mutants:

  def apply(id: CloneId, uniformBlocks: Array[Batch[UniformBlock]]): Mutants =
    Mutants(
      id,
      Depth.zero,
      uniformBlocks
    )

  def apply(id: CloneId, uniformBlocks: Batch[UniformBlock]): Mutants =
    Mutants(
      id,
      Depth.zero,
      Array(uniformBlocks)
    )
