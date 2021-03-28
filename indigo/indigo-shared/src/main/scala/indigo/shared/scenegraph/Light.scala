package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.RGBA

sealed trait Light {
  val color: RGB
  val power: Double
}

final case class PointLight(
    position: Point,
    height: Double,
    color: RGB,
    power: Double,
    specular: RGB,
    specularPower: Double,
    attenuation: Int,
    near: Int,
    far: Int
) extends Light {
  def moveTo(newPosition: Point): PointLight =
    this.copy(position = newPosition)

  def moveBy(amount: Point): PointLight =
    this.copy(position = position + amount)

  def withHeight(newHeight: Double): PointLight =
    this.copy(height = newHeight)

  def withColor(newColor: RGB): PointLight =
    this.copy(color = newColor)

  def withPower(newPower: Double): PointLight =
    this.copy(power = newPower)

  def withSpecularColor(newColor: RGB): PointLight =
    this.copy(specular = newColor)

  def withSpecularPower(newPower: Double): PointLight =
    this.copy(specularPower = newPower)

  def withAttenuation(distance: Int): PointLight =
    this.copy(attenuation = distance)

  def withNear(distance: Int): PointLight =
    this.copy(near = distance)

  def withFar(distance: Int): PointLight =
    this.copy(far = distance)
}
object PointLight {

  val default: PointLight =
    PointLight(Point.zero, 1, RGB.White, 1.0d, RGB.White, 1.0d, 100, 0, 300)

  def apply(position: Point, color: RGBA): PointLight =
    PointLight(position, 1, color.toRGB, color.a, RGB.White, 1.0d, 100, 0, 300)

}

final case class SpotLight(
    position: Point,
    height: Double,
    color: RGB,
    power: Double,
    specular: RGB,
    specularPower: Double,
    attenuation: Int,
    angle: Radians,
    rotation: Radians,
    near: Int,
    far: Int
) extends Light {
  def moveTo(newPosition: Point): SpotLight =
    this.copy(position = newPosition)

  def moveBy(amount: Point): SpotLight =
    this.copy(position = position + amount)

  def withHeight(newHeight: Double): SpotLight =
    this.copy(height = newHeight)

  def withColor(newColor: RGB): SpotLight =
    this.copy(color = newColor)

  def withPower(newPower: Double): SpotLight =
    this.copy(power = newPower)

  def withSpecularColor(newColor: RGB): SpotLight =
    this.copy(specular = newColor)

  def withSpecularPower(newPower: Double): SpotLight =
    this.copy(specularPower = newPower)

  def withAttenuation(distance: Int): SpotLight =
    this.copy(attenuation = distance)

  def withAngle(newAngle: Radians): SpotLight =
    this.copy(angle = newAngle)

  def rotateTo(newRotation: Radians): SpotLight =
    this.copy(rotation = newRotation)

  def rotateBy(amount: Radians): SpotLight =
    this.copy(rotation = rotation + amount)

  def withNear(distance: Int): SpotLight =
    this.copy(near = distance)

  def withFar(distance: Int): SpotLight =
    this.copy(far = distance)

  def lookAt(point: Point): SpotLight =
    lookDirection((point - position).toVector.normalise)

  def lookDirection(direction: Vector2): SpotLight = {
    val r: Double = Math.atan2(direction.y, direction.x)
    rotateTo(Radians(if (r < 0) Math.abs(r) + Math.PI else r))
  }

}
object SpotLight {

  val default: SpotLight =
    SpotLight(Point.zero, 1, RGB.White, 1.0, RGB.White, 1.0, 100, Radians.fromDegrees(45), Radians.zero, 10, 300)

  def apply(position: Point, color: RGBA): SpotLight =
    SpotLight(position, 1, color.toRGB, color.a, RGB.White, 1.0, 100, Radians.fromDegrees(45), Radians.zero, 10, 300)

}

final case class DirectionLight(
    height: Double,
    color: RGB,
    power: Double,
    rotation: Radians
) extends Light {

  def withHeight(newHeight: Double): DirectionLight =
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
    DirectionLight(1, RGB.White, 1.0, Radians.zero)

  def apply(rotation: Radians, color: RGBA): DirectionLight =
    DirectionLight(1, color.toRGB, color.a, rotation)
}

final case class AmbientLight(
    color: RGB,
    power: Double
) extends Light {

  def withColor(newColor: RGB): AmbientLight =
    this.copy(color = newColor)

  def withPower(newPower: Double): AmbientLight =
    this.copy(power = newPower)

}
object AmbientLight {

  val default: AmbientLight =
    apply(RGB.White, 1.0)

  def apply(color: RGBA): AmbientLight =
    AmbientLight(color.toRGB, color.a)

}
