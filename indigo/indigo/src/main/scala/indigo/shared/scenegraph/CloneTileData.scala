package indigo.shared.scenegraph

import indigo.shared.datatypes.Radians

/** Represents the allowable transformations of a tile clone.
  */
final case class CloneTileData(
    x: Int,
    y: Int,
    rotation: Radians,
    scaleX: Double,
    scaleY: Double,
    cropX: Int,
    cropY: Int,
    cropWidth: Int,
    cropHeight: Int
)

object CloneTileData:

  def apply(
      x: Int,
      y: Int,
      cropX: Int,
      cropY: Int,
      cropWidth: Int,
      cropHeight: Int
  ): CloneTileData =
    CloneTileData(
      x,
      y,
      Radians.zero,
      1.0d,
      1.0d,
      cropX,
      cropY,
      cropWidth,
      cropHeight
    )

  def apply(
      x: Int,
      y: Int,
      rotation: Radians,
      cropX: Int,
      cropY: Int,
      cropWidth: Int,
      cropHeight: Int
  ): CloneTileData =
    CloneTileData(
      x,
      y,
      rotation,
      1.0d,
      1.0d,
      cropX,
      cropY,
      cropWidth,
      cropHeight
    )

  given CanEqual[Option[CloneTileData], Option[CloneTileData]] = CanEqual.derived
  given CanEqual[List[CloneTileData], List[CloneTileData]]     = CanEqual.derived
