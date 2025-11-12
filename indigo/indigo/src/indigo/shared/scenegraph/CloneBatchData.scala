package indigo.shared.scenegraph

import indigo.shared.datatypes.Radians

/** Represents the standard allowable transformations of a clone.
  */
final case class CloneBatchData(x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double)

object CloneBatchData:

  def apply(x: Int, y: Int): CloneBatchData =
    CloneBatchData(x, y, Radians.zero, 1.0d, 1.0d)

  def apply(x: Int, y: Int, rotation: Radians): CloneBatchData =
    CloneBatchData(x, y, rotation, 1.0d, 1.0d)

  given CanEqual[Option[CloneBatchData], Option[CloneBatchData]] = CanEqual.derived
  given CanEqual[List[CloneBatchData], List[CloneBatchData]]     = CanEqual.derived
