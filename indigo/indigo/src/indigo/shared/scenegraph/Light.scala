package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

/** Parent type for all lights
  */
sealed trait Light derives CanEqual

/** Point lights emit light evenly in all directions from a point in space.
  */
final case class PointLight(
    position: Point,
    color: RGBA,
    specular: RGBA,
    intensity: Double,
    falloff: Falloff
) extends Light {
  def moveTo(newPosition: Point): PointLight =
    this.copy(position = newPosition)
  def moveTo(x: Int, y: Int): PointLight =
    moveTo(Point(x, y))

  def moveBy(amount: Point): PointLight =
    this.copy(position = position + amount)
  def moveBy(x: Int, y: Int): PointLight =
    moveBy(Point(x, y))

  def withColor(newColor: RGBA): PointLight =
    this.copy(color = newColor)

  def withSpecular(newColor: RGBA): PointLight =
    this.copy(specular = newColor)

  def withIntensity(newIntensity: Double): PointLight =
    this.copy(intensity = newIntensity)

  def withFalloff(newFalloff: Falloff): PointLight =
    this.copy(falloff = newFalloff)

  def modifyFalloff(modify: Falloff => Falloff): PointLight =
    this.copy(falloff = modify(falloff))
}
object PointLight {

  val default: PointLight =
    PointLight(Point.zero, RGBA.White, RGBA.White, 2, Falloff.default)

  def apply(position: Point, color: RGBA): PointLight =
    PointLight(position, color, RGBA.White, 2, Falloff.default)

}

/** Spot lights emit light like a lamp, they are essentially a point light, where the light is only allow to escape in a
  * particular anglular range.
  */
final case class SpotLight(
    position: Point,
    color: RGBA,
    specular: RGBA,
    intensity: Double,
    angle: Radians,
    rotation: Radians,
    falloff: Falloff
) extends Light {
  def moveTo(newPosition: Point): SpotLight =
    this.copy(position = newPosition)
  def moveTo(x: Int, y: Int): SpotLight =
    moveTo(Point(x, y))

  def moveBy(amount: Point): SpotLight =
    this.copy(position = position + amount)
  def moveBy(x: Int, y: Int): SpotLight =
    moveBy(Point(x, y))

  def withColor(newColor: RGBA): SpotLight =
    this.copy(color = newColor)

  def withSpecular(newColor: RGBA): SpotLight =
    this.copy(specular = newColor)

  def withIntensity(newIntensity: Double): SpotLight =
    this.copy(intensity = newIntensity)

  def withAngle(newAngle: Radians): SpotLight =
    this.copy(angle = newAngle)

  def rotateTo(newRotation: Radians): SpotLight =
    this.copy(rotation = newRotation)

  def rotateBy(amount: Radians): SpotLight =
    this.copy(rotation = rotation + amount)

  def withFalloff(newFalloff: Falloff): SpotLight =
    this.copy(falloff = newFalloff)

  def modifyFalloff(modify: Falloff => Falloff): SpotLight =
    this.copy(falloff = modify(falloff))

  def lookAt(point: Point): SpotLight =
    lookDirection((point - position).toVector.normalise)

  def lookDirection(direction: Vector2): SpotLight = {
    val r: Double = Math.atan2(direction.y, direction.x)
    rotateTo(Radians(if (r < 0) Math.abs(r) + Math.PI else r))
  }

}
object SpotLight {

  val default: SpotLight =
    SpotLight(Point.zero, RGBA.White, RGBA.White, 2, Radians.fromDegrees(45), Radians.zero, Falloff.default)

  def apply(position: Point, color: RGBA): SpotLight =
    SpotLight(position, color, RGBA.White, 2, Radians.fromDegrees(45), Radians.zero, Falloff.default)

}

/** Direction lights apply light to a scene evenly from a particular direction, as if from a point a very long way away,
  * e.g. the sun.
  */
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

/** Ambient light isn't emitted from anywhere in particular, it is the base amount of illumination. It's important for a
  * dark cave to light enough for your player to appreciate just how dark it really is.
  */
final case class AmbientLight(color: RGBA) extends Light {

  def withColor(newColor: RGBA): AmbientLight =
    this.copy(color = newColor)

}
object AmbientLight {

  val default: AmbientLight =
    apply(RGBA.White)

}

/** Represents different lighting falloff models, also known as attenuation, i.e. how much a light power decays over
  * distance.
  *
  * Quadratic is the most physically accurate, but possibly least useful for 2D games! All other models are unrealistic,
  * but possibly easier to work with.
  *
  * Note that "intensity" will feel different in different lighting models. Try smooth with intensity 1 or 2, Linear 5,
  * or Quadratic 500 and compare.
  */
sealed trait Falloff {
  def withRange(newNear: Int, newFar: Int): Falloff
  def withNear(newNear: Int): Falloff
  def withFar(newFar: Int): Falloff
}
object Falloff {

  val default: Falloff =
    SmoothQuadratic(0, 100)

  val none: None                       = None.default
  val smoothLinear: SmoothLinear       = SmoothLinear.default
  val smoothQuadratic: SmoothQuadratic = SmoothQuadratic.default
  val linear: Linear                   = Linear.default
  val quadratic: Quadratic             = Quadratic.default

  /** Light does not decay.
    */
  final case class None(near: Int, far: Option[Int]) extends Falloff {
    def withRange(newNear: Int, newFar: Int): None =
      this.copy(near = newNear, far = Some(newFar))

    def withNear(newNear: Int): None =
      this.copy(near = newNear)

    def withFar(newFar: Int): None =
      this.copy(far = Some(newFar))

    def noFarLimit: None =
      this.copy(far = scala.None)
  }
  object None {
    def default: None =
      None(0, scala.None)

    def apply(far: Int): None =
      None(0, Option(far))

    def apply(near: Int, far: Int): None =
      None(near, Option(far))
  }

  /** A big smooth circle of light that falls to zero at the "far" distance.
    *
    * @param near
    * @param far
    */
  final case class SmoothLinear(near: Int, far: Int) extends Falloff {
    def withRange(newNear: Int, newFar: Int): SmoothLinear =
      this.copy(near = newNear, far = newFar)

    def withNear(newNear: Int): SmoothLinear =
      this.copy(near = newNear)

    def withFar(newFar: Int): SmoothLinear =
      this.copy(far = newFar)
  }
  object SmoothLinear {
    def default: SmoothLinear =
      SmoothLinear(0, 100)

    def apply(far: Int): SmoothLinear =
      SmoothLinear(0, far)
  }

  /** A smooth circle of light that decays pleasingly to zero at the "far" distance.
    *
    * @param near
    * @param far
    */
  final case class SmoothQuadratic(near: Int, far: Int) extends Falloff {
    def withRange(newNear: Int, newFar: Int): SmoothQuadratic =
      this.copy(near = newNear, far = newFar)

    def withNear(newNear: Int): SmoothQuadratic =
      this.copy(near = newNear)

    def withFar(newFar: Int): SmoothQuadratic =
      this.copy(far = newFar)
  }
  object SmoothQuadratic {
    def default: SmoothQuadratic =
      SmoothQuadratic(0, 100)

    def apply(far: Int): SmoothQuadratic =
      SmoothQuadratic(0, far)
  }

  /** Light decays linearly forever. If a "far" distance is specified then the light will be artificially attenuated to
    * zero by the time it reaches the limit.
    *
    * @param near
    * @param far
    */
  final case class Linear(near: Int, far: Option[Int]) extends Falloff {
    def withRange(newNear: Int, newFar: Int): Linear =
      this.copy(near = newNear, far = Some(newFar))

    def withNear(newNear: Int): Linear =
      this.copy(near = newNear)

    def withFar(newFar: Int): Linear =
      this.copy(far = Some(newFar))

    def noFarLimit: Linear =
      this.copy(far = scala.None)
  }
  object Linear {
    def default: Linear =
      Linear(0, scala.None)

    def apply(far: Int): Linear =
      Linear(0, Option(far))

    def apply(near: Int, far: Int): Linear =
      Linear(near, Option(far))
  }

  /** Light decays quadratically (inverse-square) forever. If a "far" distance is specified then the light will be
    * artificially attenuated to zero by the time it reaches the limit.
    *
    * @param near
    * @param far
    */
  final case class Quadratic(near: Int, far: Option[Int]) extends Falloff {
    def withRange(newNear: Int, newFar: Int): Quadratic =
      this.copy(near = newNear, far = Some(newFar))

    def withNear(newNear: Int): Quadratic =
      this.copy(near = newNear)

    def withFar(newFar: Int): Quadratic =
      this.copy(far = Some(newFar))

    def noFarLimit: Quadratic =
      this.copy(far = scala.None)
  }
  object Quadratic {
    def default: Quadratic =
      Quadratic(0, scala.None)

    def apply(far: Int): Quadratic =
      Quadratic(0, Option(far))

    def apply(near: Int, far: Int): Quadratic =
      Quadratic(near, Option(far))
  }

}
