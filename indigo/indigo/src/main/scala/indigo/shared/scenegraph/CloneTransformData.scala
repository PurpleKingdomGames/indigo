package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

/** Represents the standard allowable transformations of a clone.
  */
opaque type CloneTransformData = Array[Float]

object CloneTransformData:

  val dataLength: Int = 5

  extension (ctd: CloneTransformData)
    inline def ++(other: CloneTransformData): CloneTransformData = ctd.concat(other)
    def size: Int                                         = ctd.length / dataLength
    inline def toArray: Array[Float]                      = ctd

  inline def apply(x: Int, y: Int): CloneTransformData =
    Array(x.toFloat, y.toFloat, Radians.zero.toFloat, 1.0f, 1.0f)

  inline def apply(x: Int, y: Int, rotation: Radians): CloneTransformData =
    Array(x.toFloat, y.toFloat, rotation.toFloat, 1.0f, 1.0f)

  inline def apply(x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double): CloneTransformData =
    Array(x.toFloat, y.toFloat, rotation.toFloat, scaleX.toFloat, scaleY.toFloat)

  inline def apply(x: Float, y: Float, rotation: Float, scaleX: Float, scaleY: Float): CloneTransformData =
    Array(x, y, rotation, scaleX, scaleY)

  val identity: CloneTransformData =
    Array(0.0f, 0.0f, 0.0f, 1.0f, 1.0f)

  given CanEqual[Option[CloneTransformData], Option[CloneTransformData]] = CanEqual.derived
