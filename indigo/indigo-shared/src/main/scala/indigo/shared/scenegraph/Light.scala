package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.Vector2

sealed trait Light {
  val height: Int
  val color: RGB
  val power: Double
}

final case class PointLight(
    position: Point,
    height: Int,
    color: RGB,
    power: Double,
    attenuation: Int
) extends Light {
  def moveTo(newPosition: Point): PointLight =
    this.copy(position = newPosition)

  def moveBy(amount: Point): PointLight =
    this.copy(position = position + amount)

  def withHeight(newHeight: Int): PointLight =
    this.copy(height = newHeight)

  def withColor(newColor: RGB): PointLight =
    this.copy(color = newColor)

  def withPower(newPower: Double): PointLight =
    this.copy(power = newPower)

  def withAttenuation(distance: Int): PointLight =
    this.copy(attenuation = distance)
}
object PointLight {

  val default: PointLight =
    apply(Point.zero, 100, RGB.White, 1.5d, 100)

}

final case class SpotLight(
    position: Point,
    height: Int,
    color: RGB,
    power: Double,
    attenuation: Int,
    angle: Radians,
    rotation: Radians,
    near: Int,
    far: Int
) extends Light {
  def moveTo(newPosition: Point): SpotLight =
    new SpotLight(newPosition, height, color, power, attenuation, angle, rotation, near, far)

  def moveBy(amount: Point): SpotLight =
    new SpotLight(position + amount, height, color, power, attenuation, angle, rotation, near, far)

  def withHeight(newHeight: Int): SpotLight =
    new SpotLight(position, newHeight, color, power, attenuation, angle, rotation, near, far)

  def withNear(distance: Int): SpotLight =
    new SpotLight(position, height, color, power, attenuation, angle, rotation, distance, far)

  def withFar(distance: Int): SpotLight =
    new SpotLight(position, height, color, power, attenuation, angle, rotation, near, distance)

  def withColor(newColor: RGB): SpotLight =
    new SpotLight(position, height, newColor, power, attenuation, angle, rotation, near, far)

  def withPower(newPower: Double): SpotLight =
    new SpotLight(position, height, color, newPower, attenuation, angle, rotation, near, far)

  def withAttenuation(distance: Int): SpotLight =
    new SpotLight(position, height, color, power, distance, angle, rotation, near, far)

  def withAngle(newAngle: Radians): SpotLight =
    new SpotLight(position, height, color, power, attenuation, newAngle, rotation, near, far)

  def rotateTo(newRotation: Radians): SpotLight =
    new SpotLight(position, height, color, power, attenuation, angle, newRotation, near, far)

  def rotateBy(amount: Radians): SpotLight =
    new SpotLight(position, height, color, power, attenuation, angle, rotation + amount, near, far)

  def lookAt(point: Point): SpotLight =
    lookDirection((point - position).toVector.normalise)

  def lookDirection(direction: Vector2): SpotLight = {
    val r: Double = Math.atan2(direction.y, direction.x)
    rotateTo(Radians(if (r < 0) Math.abs(r) + Math.PI else r))
  }

}
object SpotLight {

  val default: SpotLight =
    apply(Point.zero, 100, RGB.White, 1.5, 100, Radians.fromDegrees(45), Radians.zero, 10, 300)

}

final case class DirectionLight(
    height: Int,
    color: RGB,
    power: Double,
    rotation: Radians
) extends Light {

  def withHeight(newHeight: Int): DirectionLight =
    this.copy(height = newHeight)

  def withColor(newColor: RGB): DirectionLight =
    this.copy(color = newColor)

  def withPower(newPower: Double): DirectionLight =
    this.copy(power = newPower)

  def rotateTo(newRotation: Radians): DirectionLight =
    this.copy(rotation = newRotation)

  def rotateBy(amount: Radians): DirectionLight =
    this.copy(rotation = rotation + amount)

}
object DirectionLight {

  val default: DirectionLight =
    apply(100, RGB.White, 1.0, Radians.zero)

}
