package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

/** Represents the allowable transformations of a tile clone.
  */
opaque type CloneTileData = Array[Float]

object CloneTileData:

  val dataLength: Int = 9

  extension (ctd: CloneTileData)
    inline def ++(other: CloneTileData): CloneTileData = ctd.concat(other)
    def size: Int                                      = ctd.length / dataLength
    inline def toArray: Array[Float]                   = ctd

  inline def unsafe(data: Array[Float]): CloneTileData =
    data

  inline def apply(
      x: Int,
      y: Int,
      cropX: Int,
      cropY: Int,
      cropWidth: Int,
      cropHeight: Int
  ): CloneTileData =
    Array(
      x.toFloat,
      y.toFloat,
      Radians.zero.toFloat,
      1.0f,
      1.0f,
      cropX.toFloat,
      cropY.toFloat,
      cropWidth.toFloat,
      cropHeight.toFloat
    )

  inline def apply(
      x: Int,
      y: Int,
      rotation: Radians,
      cropX: Int,
      cropY: Int,
      cropWidth: Int,
      cropHeight: Int
  ): CloneTileData =
    Array(
      x.toFloat,
      y.toFloat,
      rotation.toFloat,
      1.0f,
      1.0f,
      cropX.toFloat,
      cropY.toFloat,
      cropWidth.toFloat,
      cropHeight.toFloat
    )

  inline def apply(
      x: Int,
      y: Int,
      rotation: Radians,
      scaleX: Double,
      scaleY: Double,
      cropX: Int,
      cropY: Int,
      cropWidth: Int,
      cropHeight: Int
  ): CloneTileData =
    Array(
      x.toFloat,
      y.toFloat,
      rotation.toFloat,
      scaleX.toFloat,
      scaleY.toFloat,
      cropX.toFloat,
      cropY.toFloat,
      cropWidth.toFloat,
      cropHeight.toFloat
    )

  inline def apply(
      x: Float,
      y: Float,
      rotation: Float,
      scaleX: Float,
      scaleY: Float,
      cropX: Float,
      cropY: Float,
      cropWidth: Float,
      cropHeight: Float
  ): CloneTileData =
    Array(x, y, rotation, scaleX, scaleY, cropX, cropY, cropWidth, cropHeight)

  given CanEqual[Option[CloneTileData], Option[CloneTileData]] = CanEqual.derived
  given CanEqual[List[CloneTileData], List[CloneTileData]]     = CanEqual.derived
