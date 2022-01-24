package indigoexamples

import indigo._

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object MouseEventsExample extends IndigoSandbox[Unit, Model]:

  val config: GameConfig = GameConfig.default

  val animations: Set[Animation] = Set()

  val assetName: AssetName = AssetName("dots")

  val assets: Set[AssetType] = Set(AssetType.Image(AssetName("dots"), AssetPath("assets/dots.png")))

  val fonts: Set[FontInfo] = Set()

  val shaders: Set[Shader] = Set()

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Model] =
    Outcome(Model.initial(config.viewport.center))

  def updateModel(context: FrameContext[Unit], model: Model): GlobalEvent => Outcome[Model] =
    case FrameTick if context.mouse.pressed(MouseButton.LeftMouseButton) =>
      val newDot = model.dot.copy(magnification = model.dot.magnification * 2)
      Outcome(model.copy(dot = newDot))
    case FrameTick if context.mouse.pressed(MouseButton.RightMouseButton) =>
      val newDot = model.dot.copy(magnification = Math.max(model.dot.magnification / 2, 1))
      Outcome(model.copy(dot = newDot))
    case FrameTick if context.mouse.scrolled.exists(_ == MouseWheel.ScrollUp) =>
      val newDot = model.dot.copy(magnification = model.dot.magnification * 2)
      Outcome(model.copy(dot = newDot))
    case FrameTick if context.mouse.scrolled.exists(_ == MouseWheel.ScrollDown) =>
      val newDot = model.dot.copy(magnification = Math.max(model.dot.magnification / 2, 1))
      Outcome(model.copy(dot = newDot))
    case FrameTick =>
      Outcome(model)
    case _ =>
      Outcome(model)

  def present(context: FrameContext[Unit], model: Model): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment(drawDot(model.dot)))

  def drawDot(dot: Dot): Graphic[Material.Bitmap] =
    Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName))
      .withCrop(Rectangle(16, 16, 16, 16))
      .withScale(Vector2(dot.magnification, dot.magnification))
      .withRef(8, 8)
      .moveTo(dot.center)

end MouseEventsExample

final case class Model(dot: Dot)

object Model:
  def initial(center: Point): Model = Model(Dot.initial(center))

final case class Dot(center: Point, magnification: Int)

object Dot:
  def initial(center: Point): Dot = Dot(center, magnification = 1)
