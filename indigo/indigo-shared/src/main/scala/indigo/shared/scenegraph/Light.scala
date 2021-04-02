package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.RGBA

sealed trait Light

final case class PointLight(
    position: Point,
    color: RGBA,
    specular: RGBA,
    attenuation: Int,
    near: Int,
    far: Int
) extends Light {
  def moveTo(newPosition: Point): PointLight =
    this.copy(position = newPosition)

  def moveBy(amount: Point): PointLight =
    this.copy(position = position + amount)

  def withColor(newColor: RGBA): PointLight =
    this.copy(color = newColor)

  def withSpecular(newColor: RGBA): PointLight =
    this.copy(specular = newColor)

  def withAttenuation(distance: Int): PointLight =
    this.copy(attenuation = distance)

  def withNear(distance: Int): PointLight =
    this.copy(near = distance)

  def withFar(distance: Int): PointLight =
    this.copy(far = distance)
}
object PointLight {

  val default: PointLight =
    PointLight(Point.zero, RGBA.White, RGBA.White, 100, 0, 100)

  def apply(position: Point, color: RGBA): PointLight =
    PointLight(position, color, RGBA.White, 100, 0, 100)

}

final case class SpotLight(
    position: Point,
    color: RGBA,
    specular: RGBA,
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

  def withColor(newColor: RGBA): SpotLight =
    this.copy(color = newColor)

  def withSpecular(newColor: RGBA): SpotLight =
    this.copy(specular = newColor)

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
    SpotLight(Point.zero, RGBA.White, RGBA.White, 100, Radians.fromDegrees(45), Radians.zero, 10, 300)

  def apply(position: Point, color: RGBA): SpotLight =
    SpotLight(position, color, RGBA.White, 100, Radians.fromDegrees(45), Radians.zero, 10, 300)

}

final case class DirectionLight(
    color: RGBA,
    specular: RGBA,
    rotation: Radians
) extends Light {

  def withColor(newColor: RGBA): DirectionLight =
    this.copy(color = newColor)

  def withSpecular(newColor: RGBA): DirectionLight =
    this.copy(specular = newColor)

  def rotateTo(newRotation: Radians): DirectionLight =
    this.copy(rotation = newRotation)

  def rotateBy(amount: Radians): DirectionLight =
    this.copy(rotation = rotation + amount)

}
object DirectionLight {

  val default: DirectionLight =
    DirectionLight(RGBA.White, RGBA.White, Radians.zero)

  def apply(rotation: Radians, color: RGBA): DirectionLight =
    DirectionLight(color, RGBA.White, rotation)
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
