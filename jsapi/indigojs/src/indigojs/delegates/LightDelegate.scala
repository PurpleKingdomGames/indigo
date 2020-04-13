package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.Light
import indigojs.delegates.geometry.Vector2Delegate
import indigo.shared.datatypes.Radians
import indigo.shared.scenegraph.PointLight
import indigo.shared.scenegraph.SpotLight
import indigo.shared.scenegraph.DirectionLight

sealed trait LightDelegate {
  def toInternal: Light
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("PointLight")
final class PointLightDelegate(
    _position: PointDelegate,
    _height: Int,
    _color: RGBDelegate,
    _power: Double,
    _attenuation: Int
) extends LightDelegate {

  @JSExport
  val position = _position
  @JSExport
  val height = _height
  @JSExport
  val color = _color
  @JSExport
  val power = _power
  @JSExport
  val attenuation = _attenuation

  @JSExport
  def moveTo(newPosition: PointDelegate): PointLightDelegate =
    new PointLightDelegate(newPosition, height, color, power, attenuation)

  @JSExport
  def moveBy(amount: PointDelegate): PointLightDelegate =
    new PointLightDelegate(PointDelegate.add(position, amount), height, color, power, attenuation)

  @JSExport
  def withHeight(newHeight: Int): PointLightDelegate =
    new PointLightDelegate(position, newHeight, color, power, attenuation)

  @JSExport
  def withColor(newColor: RGBDelegate): PointLightDelegate =
    new PointLightDelegate(position, height, newColor, power, attenuation)

  @JSExport
  def withPower(newPower: Double): PointLightDelegate =
    new PointLightDelegate(position, height, color, newPower, attenuation)

  @JSExport
  def withAttenuation(distance: Int): PointLightDelegate =
    new PointLightDelegate(position, height, color, power, distance)

  def toInternal: PointLight =
    new PointLight(position.toInternal, height, color.toInternal, power, attenuation)
}
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("PointLightHelper")
object PointLightDelegate {
  @JSExport
  val default: PointLightDelegate =
    new PointLightDelegate(new PointDelegate(0, 0), 100, RGBDelegate.White, 1.5d, 100)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SpotLight")
final class SpotLightDelegate(
    _position: PointDelegate,
    _height: Int,
    _color: RGBDelegate,
    _power: Double,
    _attenuation: Int,
    _angle: Double,
    _rotation: Double,
    _near: Int,
    _far: Int
) extends LightDelegate {

  @JSExport
  val position = _position
  @JSExport
  val height = _height
  @JSExport
  val color = _color
  @JSExport
  val power = _power
  @JSExport
  val attenuation = _attenuation
  @JSExport
  val angle = _angle
  @JSExport
  val rotation = _rotation
  @JSExport
  val near = _near
  @JSExport
  val far = _far

  @JSExport
  def moveTo(newPosition: PointDelegate): SpotLightDelegate =
    new SpotLightDelegate(newPosition, height, color, power, attenuation, angle, rotation, near, far)

  @JSExport
  def moveBy(amount: PointDelegate): SpotLightDelegate =
    new SpotLightDelegate(PointDelegate.add(position, amount), height, color, power, attenuation, angle, rotation, near, far)

  @JSExport
  def withHeight(newHeight: Int): SpotLightDelegate =
    new SpotLightDelegate(position, newHeight, color, power, attenuation, angle, rotation, near, far)

  @JSExport
  def withNear(distance: Int): SpotLightDelegate =
    new SpotLightDelegate(position, height, color, power, attenuation, angle, rotation, distance, far)

  @JSExport
  def withFar(distance: Int): SpotLightDelegate =
    new SpotLightDelegate(position, height, color, power, attenuation, angle, rotation, near, distance)

  @JSExport
  def withColor(newColor: RGBDelegate): SpotLightDelegate =
    new SpotLightDelegate(position, height, newColor, power, attenuation, angle, rotation, near, far)

  @JSExport
  def withPower(newPower: Double): SpotLightDelegate =
    new SpotLightDelegate(position, height, color, newPower, attenuation, angle, rotation, near, far)

  @JSExport
  def withAttenuation(distance: Int): SpotLightDelegate =
    new SpotLightDelegate(position, height, color, power, distance, angle, rotation, near, far)

  @JSExport
  def withAngle(newAngle: Double): SpotLightDelegate =
    new SpotLightDelegate(position, height, color, power, attenuation, newAngle, rotation, near, far)

  @JSExport
  def rotateTo(newRotation: Double): SpotLightDelegate =
    new SpotLightDelegate(position, height, color, power, attenuation, angle, newRotation, near, far)

  @JSExport
  def rotateBy(amount: Double): SpotLightDelegate =
    new SpotLightDelegate(position, height, color, power, attenuation, angle, rotation + amount, near, far)

  @JSExport
  def lookAt(point: PointDelegate): SpotLightDelegate =
    lookDirection(PointDelegate.subtract(point, position).toVector.normalise)

  @JSExport
  def lookDirection(direction: Vector2Delegate): SpotLightDelegate = {
    val r: Double = Math.atan2(direction.y, direction.x)
    rotateTo(if (r < 0) Math.abs(r) + Math.PI else r)
  }

  def toInternal: SpotLight =
    new SpotLight(
      position.toInternal,
      height,
      color.toInternal,
      power,
      attenuation,
      Radians(angle),
      Radians(rotation),
      near,
      far
    )
}
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SpotLightHelper")
object SpotLightDelegate {
  @JSExport
  val default: SpotLightDelegate =
    new SpotLightDelegate(new PointDelegate(0, 0), 100, RGBDelegate.White, 1.5, 100, Radians.fromDegrees(45).value, Radians.zero.value, 10, 300)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("DirectionLight")
final class DirectionLightDelegate(
    _height: Int,
    _color: RGBDelegate,
    _power: Double,
    _rotation: Double
) extends LightDelegate {

  @JSExport
  val height = _height
  @JSExport
  val color = _color
  @JSExport
  val power = _power
  @JSExport
  val rotation = _rotation

  @JSExport
  def withHeight(newHeight: Int): DirectionLightDelegate =
    new DirectionLightDelegate(newHeight, color, power, rotation)

  @JSExport
  def withColor(newColor: RGBDelegate): DirectionLightDelegate =
    new DirectionLightDelegate(height, newColor, power, rotation)

  @JSExport
  def withPower(newPower: Double): DirectionLightDelegate =
    new DirectionLightDelegate(height, color, newPower, rotation)

  @JSExport
  def rotateTo(newRotation: Double): DirectionLightDelegate =
    new DirectionLightDelegate(height, color, power, newRotation)

  @JSExport
  def rotateBy(amount: Double): DirectionLightDelegate =
    new DirectionLightDelegate(height, color, power, rotation + amount)

  def toInternal: DirectionLight =
    new DirectionLight(
      height,
      color.toInternal,
      power,
      Radians(rotation)
    )
}
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("DirectionLightHelper")
object DirectionLightDelegate {
  @JSExport
  val default: DirectionLightDelegate =
    new DirectionLightDelegate(100, RGBDelegate.White, 1.0, Radians.zero.value)
}
