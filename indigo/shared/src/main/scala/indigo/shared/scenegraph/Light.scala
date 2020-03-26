package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.RGB

sealed trait Light {
  val color: RGB
}

final class PointLight(
    val position: Point,
    val height: Int,
    val color: RGB,
    val attenuation: Int
) extends Light {
  def moveTo(newPosition: Point): PointLight =
    new PointLight(newPosition, height, color, attenuation)

  def moveBy(amount: Point): PointLight =
    new PointLight(position + amount, height, color, attenuation)

  def withHeight(newHeight: Int): PointLight =
    new PointLight(position, newHeight, color, attenuation)

  def withColor(newColor: RGB): PointLight =
    new PointLight(position, height, newColor, attenuation)

  def withAttenuation(distance: Int): PointLight =
    new PointLight(position, height, color, distance)
}
object PointLight {
  def apply(position: Point, height: Int, color: RGB, attenuation: Int): PointLight =
    new PointLight(position, height, color, attenuation)

  val default: PointLight =
    apply(Point.zero, 1, RGB.White, 100)
}

final class SpotLight(
    val position: Point,
    val height: Int,
    val color: RGB,
    val attenuation: Int,
    val angle: Radians,
    val rotation: Radians,
    val near: Int,
    val far: Int
) extends Light {
  def moveTo(newPosition: Point): SpotLight =
    new SpotLight(newPosition, height, color, attenuation, angle, rotation, near, far)

  def moveBy(amount: Point): SpotLight =
    new SpotLight(position + amount, height, color, attenuation, angle, rotation, near, far)

  def withHeight(newHeight: Int): SpotLight =
    new SpotLight(position, newHeight, color, attenuation, angle, rotation, near, far)

  def withNear(distance: Int): SpotLight =
    new SpotLight(position, height, color, attenuation, angle, rotation, distance, far)

  def withFar(distance: Int): SpotLight =
    new SpotLight(position, height, color, attenuation, angle, rotation, near, distance)

  def withColor(newColor: RGB): SpotLight =
    new SpotLight(position, height, newColor, attenuation, angle, rotation, near, far)

  def withAttenuation(distance: Int): SpotLight =
    new SpotLight(position, height, color, distance, angle, rotation, near, far)

  def withAngle(newAngle: Radians): SpotLight =
    new SpotLight(position, height, color, attenuation, newAngle, rotation, near, far)

  def rotateTo(newRotation: Radians): SpotLight =
    new SpotLight(position, height, color, attenuation, angle, newRotation, near, far)

  def rotateBy(amount: Radians): SpotLight =
    new SpotLight(position, height, color, attenuation, angle, rotation + amount, near, far)
}
object SpotLight {
  def apply(position: Point, height: Int, color: RGB, attenuation: Int, angle: Radians, rotation: Radians, near: Int, far: Int): SpotLight =
    new SpotLight(position, height, color, attenuation, angle, rotation, near, far)

  val default: SpotLight =
    apply(Point.zero, 1, RGB.White, 100, Radians.fromDegrees(45), Radians.zero, 10, 300)
}

final class DirectionLight(
    val height: Int,
    val color: RGB,
    val rotation: Radians,
    val strength: Double
) extends Light {
  def withHeight(newHeight: Int): DirectionLight =
    new DirectionLight(newHeight, color, rotation, strength)

  def withColor(newColor: RGB): DirectionLight =
    new DirectionLight(height, newColor, rotation, strength)

  def rotateTo(newRotation: Radians): DirectionLight =
    new DirectionLight(height, color, newRotation, strength)

  def rotateBy(amount: Radians): DirectionLight =
    new DirectionLight(height, color, rotation + amount, strength)

  def withStrength(newStrength: Double): DirectionLight =
    new DirectionLight(height, color, rotation, newStrength)
}
object DirectionLight {
  def apply(height: Int, color: RGB, rotation: Radians, strength: Double): DirectionLight =
    new DirectionLight(height, color, rotation, strength)
}
