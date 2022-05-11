package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

/** Represents the standard allowable transformations of a clone.
  */
final case class CloneBatchData(x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double)

object CloneBatchData:

  def apply(x: Int, y: Int): CloneBatchData =
    CloneBatchData(x, y, Radians.zero, 1.0d, 1.0d)

  def apply(x: Int, y: Int, rotation: Radians): CloneBatchData =
    CloneBatchData(x, y, rotation, 1.0d, 1.0d)

  given CanEqual[Option[CloneBatchData], Option[CloneBatchData]] = CanEqual.derived
  given CanEqual[Batch[CloneBatchData], Batch[CloneBatchData]]     = CanEqual.derived
