package indigoexamples

import indigo._
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object InputMapperExample extends IndigoSandbox[Unit, Model] {

  val magnification: Int = 3

  val config: GameConfig =
    GameConfig.default.withMagnification(magnification)

  val animations: Set[Animation] =
    Set()

  val assetName: AssetName = AssetName("dots")

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName("dots"), AssetPath("assets/dots.png"))
    )

  val fonts: Set[FontInfo] =
    Set()

  val shaders: Set[Shader] =
    Set()

  def setup(
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Model] =
    Outcome(
      Model.initial(
        config.viewport.giveDimensions(magnification).center
      )
    )

  def updateModel(
      context: FrameContext[Unit],
      model: Model
  ): GlobalEvent => Outcome[Model] = {
    case FrameTick =>
      model.update(context.delta, context.inputState)

    case _ =>
      Outcome(model)
  }

  def present(
      context: FrameContext[Unit],
      model: Model
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(drawDot(model.dot))
    )

  def drawDot(
      dot: Dot
  ): Graphic =
    Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName))
      .withCrop(Rectangle(16, 16, 16, 16))
      .withRef(8, 8)
      .moveTo(dot.center)
}

final case class Model(dot: Dot) {
  def update(timeDelta: Seconds, inputState: InputState): Outcome[Model] =
    dot.update(timeDelta, inputState).map { d =>
      this.copy(dot = d)
    }
}
object Model {
  def initial(center: Point): Model =
    Model(Dot.initial(center))
}

final case class Dot(center: Point) {
  def update(timeDelta: Seconds, inputState: InputState): Outcome[Dot] = {
    val inputForce = inputState.mapInputs(Dot.inputMapping, Vector2(0, 0))
    val nextCenter = Point(
      (center.x + math.round(inputForce.x * timeDelta.toDouble).toInt),
      (center.y + math.round(inputForce.y * timeDelta.toDouble).toInt)
    )
    Outcome(this.copy(center = nextCenter))
  }
}
object Dot {
  def initial(center: Point): Dot =
    Dot(center)

  val inputMapping: InputMapping[Vector2] = {
    val xSpeed = 30.0d
    val ySpeed = -30.0d
    InputMapping(
      // WASD keymap
      Combo.withKeyInputs(Key.KEY_W, Key.KEY_A) -> Vector2(-xSpeed, ySpeed),
      Combo.withKeyInputs(Key.KEY_W, Key.KEY_S) -> Vector2(0.0d, 0.0d),
      Combo.withKeyInputs(Key.KEY_W, Key.KEY_D) -> Vector2(xSpeed, ySpeed),
      Combo.withKeyInputs(Key.KEY_A, Key.KEY_S) -> Vector2(-xSpeed, -ySpeed),
      Combo.withKeyInputs(Key.KEY_A, Key.KEY_D) -> Vector2(0.0d, 0.0d),
      Combo.withKeyInputs(Key.KEY_S, Key.KEY_D) -> Vector2(xSpeed, -ySpeed),
      Combo.withKeyInputs(Key.KEY_W)            -> Vector2(0.0d, ySpeed),
      Combo.withKeyInputs(Key.KEY_A)            -> Vector2(-xSpeed, 0.0d),
      Combo.withKeyInputs(Key.KEY_S)            -> Vector2(0.0d, -ySpeed),
      Combo.withKeyInputs(Key.KEY_D)            -> Vector2(xSpeed, 0.0d)
    )
  }
}
