package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

/** Represents the standard allowable transformations of a clone.
  */
opaque type CloneBatchData = Array[Float]

object CloneBatchData:

  val dataLength: Int = 5

  extension (cbd: CloneBatchData)
    inline def ++(other: CloneBatchData): CloneBatchData = cbd.concat(other)
    def size: Int                                        = cbd.length / dataLength
    inline def toArray: Array[Float]                     = cbd

  inline def unsafe(data: Array[Float]): CloneBatchData =
    data

  inline def apply(x: Int, y: Int): CloneBatchData =
    Array(x.toFloat, y.toFloat, Radians.zero.toFloat, 1.0f, 1.0f)

  inline def apply(x: Int, y: Int, rotation: Radians): CloneBatchData =
    Array(x.toFloat, y.toFloat, rotation.toFloat, 1.0f, 1.0f)

  inline def apply(x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double): CloneBatchData =
    Array(x.toFloat, y.toFloat, rotation.toFloat, scaleX.toFloat, scaleY.toFloat)

  inline def apply(x: Float, y: Float, rotation: Float, scaleX: Float, scaleY: Float): CloneBatchData =
    Array(x, y, rotation, scaleX, scaleY)

  given CanEqual[Option[CloneBatchData], Option[CloneBatchData]] = CanEqual.derived
  given CanEqual[List[CloneBatchData], List[CloneBatchData]]     = CanEqual.derived
