package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.RGBA

sealed trait Light {
  val color: RGBA
}

final class PointLight(
    val position: Point,
    val color: RGBA,
    val attenuation: Int
) extends Light {
  def moveTo(newPosition: Point): PointLight =
    new PointLight(newPosition, color, attenuation)

  def moveBy(amount: Point): PointLight =
    new PointLight(position + amount, color, attenuation)

  def withColor(newColor: RGBA): PointLight =
    new PointLight(position, newColor, attenuation)

  def withAttenuation(distance: Int): PointLight =
    new PointLight(position, color, distance)
}
object PointLight {
  def apply(position: Point, color: RGBA, attenuation: Int): PointLight =
    new PointLight(position, color, attenuation)

  val default: PointLight =
    apply(Point.zero, RGBA.White, 100)
}

final class SpotLight(
    val position: Point,
    val color: RGBA,
    val attenuation: Int,
    val angle: Radians,
    val rotation: Radians
) extends Light {
  def moveTo(newPosition: Point): SpotLight =
    new SpotLight(newPosition, color, attenuation, angle, rotation)

  def moveBy(amount: Point): SpotLight =
    new SpotLight(position + amount, color, attenuation, angle, rotation)

  def withColor(newColor: RGBA): SpotLight =
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
  def apply(position: Point, color: RGBA, attenuation: Int, angle: Radians, rotation: Radians): SpotLight =
    new SpotLight(position, color, attenuation, angle, rotation)
}

final class DirectionLight(
    val color: RGBA,
    val rotation: Radians
) extends Light {
  def withColor(newColor: RGBA): DirectionLight =
    new DirectionLight(newColor, rotation)

  def rotateTo(newRotation: Radians): DirectionLight =
    new DirectionLight(color, newRotation)

  def rotateBy(amount: Radians): DirectionLight =
    new DirectionLight(color, rotation + amount)
}
object DirectionLight {
  def apply(color: RGBA, rotation: Radians): DirectionLight =
    new DirectionLight(color, rotation)
}
