package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.RGB

sealed trait Light {
  val color: RGB
}

final class PointLight(
    val position: Point,
    val color: RGB,
    val attenuation: Int
) extends Light {
  def moveTo(newPosition: Point): PointLight =
    new PointLight(newPosition, color, attenuation)

  def moveBy(amount: Point): PointLight =
    new PointLight(position + amount, color, attenuation)

  def withColor(newColor: RGB): PointLight =
    new PointLight(position, newColor, attenuation)

  def withAttenuation(distance: Int): PointLight =
    new PointLight(position, color, distance)
}
object PointLight {
  def apply(position: Point, color: RGB, attenuation: Int): PointLight =
    new PointLight(position, color, attenuation)

  val default: PointLight =
    apply(Point.zero, RGB.White, 100)
}

final class SpotLight(
    val position: Point,
    val color: RGB,
    val attenuation: Int,
    val angle: Radians,
    val rotation: Radians
) extends Light {
  def moveTo(newPosition: Point): SpotLight =
    new SpotLight(newPosition, color, attenuation, angle, rotation)

  def moveBy(amount: Point): SpotLight =
    new SpotLight(position + amount, color, attenuation, angle, rotation)

  def withColor(newColor: RGB): SpotLight =
    new SpotLight(position, newColor, attenuation, angle, rotation)

  def withAttenuation(distance: Int): SpotLight =
    new SpotLight(position, color, distance, angle, rotation)

  def withAngle(newAngle: Radians): SpotLight =
    new SpotLight(position, color, attenuation, newAngle, rotation)

  def rotateTo(newRotation: Radians): SpotLight =
    new SpotLight(position, color, attenuation, angle, newRotation)

  def rotateBy(amount: Radians): SpotLight =
    new SpotLight(position, color, attenuation, angle, rotation + amount)
}
object SpotLight {
  def apply(position: Point, color: RGB, attenuation: Int, angle: Radians, rotation: Radians): SpotLight =
    new SpotLight(position, color, attenuation, angle, rotation)

  val default: SpotLight =
    apply(Point.zero, RGB.White, 100, Radians.fromDegrees(30), Radians.zero)
}

final class DirectionLight(
    val color: RGB,
    val rotation: Radians,
    val strength: Double
) extends Light {
  def withColor(newColor: RGB): DirectionLight =
    new DirectionLight(newColor, rotation, strength)

  def rotateTo(newRotation: Radians): DirectionLight =
    new DirectionLight(color, newRotation, strength)

  def rotateBy(amount: Radians): DirectionLight =
    new DirectionLight(color, rotation + amount, strength)

  def withStrength(newStrength: Double): DirectionLight =
    new DirectionLight(color, rotation, newStrength)
}
object DirectionLight {
  def apply(color: RGB, rotation: Radians, strength: Double): DirectionLight =
    new DirectionLight(color, rotation, strength)
}
