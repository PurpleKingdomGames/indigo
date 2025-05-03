package indigoextras.subsystems

import indigo.shared.Context
import indigo.shared.Outcome
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.events.FrameTick
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Layer
import indigo.shared.scenegraph.LayerKey
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Text
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemId
import indigo.shared.time.FPS
import indigo.shared.time.Seconds

final case class FPSCounter[Model](
    id: SubSystemId,
    place: (Context[Unit], Size) => Point,
    targetFPS: Option[FPS],
    layerKey: Option[LayerKey],
    fontKey: FontKey,
    fontAsset: AssetName
) extends SubSystem[Model]:
  type EventType      = GlobalEvent
  type SubSystemModel = FPSCounterState
  type ReferenceData  = Unit

  def withPlaceFunction(
      place: (Context[Unit], Size) => Point
  ): FPSCounter[Model] =
    copy(place = place)

  def moveTo(position: Point): FPSCounter[Model] =
    withPlaceFunction(place = (_, _) => position)
  def moveTo(x: Int, y: Int): FPSCounter[Model] =
    moveTo(Point(x, y))

  def placeAt(location: (Context[Unit], Size) => Point): FPSCounter[Model] =
    withPlaceFunction(place = location)

  def withTargetFPS(targetFPS: FPS): FPSCounter[Model] =
    copy(targetFPS = Option(targetFPS))
  def clearTargetFPS: FPSCounter[Model] =
    copy(targetFPS = None)

  def withLayerKey(layerKey: LayerKey): FPSCounter[Model] =
    copy(layerKey = Option(layerKey))
  def clearLayerKey: FPSCounter[Model] =
    copy(layerKey = None)

  private val idealFps: Int = targetFPS.getOrElse(FPS.`60`).toInt
  private val decideNextFps: Int => Int =
    targetFPS match
      case None =>
        frameCountSinceInterval => frameCountSinceInterval + 1

      case Some(target) =>
        frameCountSinceInterval => Math.min(target.toInt, frameCountSinceInterval + 1)

  def eventFilter: GlobalEvent => Option[EventType] = {
    case FrameTick          => Some(FrameTick)
    case e: FPSCounter.Move => Some(e)
    case _                  => None
  }

  def reference(model: Model): ReferenceData =
    ()

  def initialModel: Outcome[SubSystemModel] =
    Outcome(FPSCounterState.initial(place))

  private val textInstance: Text[Material.ImageEffects] =
    Text(
      "",
      fontKey,
      Material.ImageEffects(fontAsset)
    )

  def update(
      context: SubSystemContext[ReferenceData],
      model: FPSCounterState
  ): GlobalEvent => Outcome[FPSCounterState] = {
    case FrameTick =>
      val bounds: Rectangle =
        if model.bounds.size == Size.zero then
          context.services.bounds
            .find(textInstance.withText(formatText(idealFps.toString)))
            .getOrElse(Rectangle.zero)
            .expand(2)
        else model.bounds

      val boxSize =
        ({ (s: Size) =>
          Size(
            if s.width  % 2 == 0 then s.width else s.width + 1,
            if s.height % 2 == 0 then s.height else s.height + 1
          )
        })(bounds.size)

      if (context.frame.time.running >= (model.lastInterval + Seconds(1)))
        Outcome(
          model.copy(
            bounds = Rectangle(model.placeFunction(context.toContext, boxSize), boxSize),
            fps = decideNextFps(model.frameCountSinceInterval),
            lastInterval = context.frame.time.running,
            frameCountSinceInterval = 0
          )
        )
      else Outcome(model.copy(frameCountSinceInterval = model.frameCountSinceInterval + 1))

    case FPSCounter.Move(to) =>
      Outcome(model.copy(placeFunction = (_, _) => to))

    case _ =>
      Outcome(model)
  }

  def present(context: SubSystemContext[ReferenceData], model: FPSCounterState): Outcome[SceneUpdateFragment] =
    val text: Text[Material.ImageEffects] =
      textInstance
        .withText(formatText(model.fps.toString))
        .moveTo(model.bounds.position + 2)
        .modifyMaterial(_.withTint(pickTint(idealFps, model.fps)))

    val bg: Shape.Box =
      Shape
        .Box(model.bounds, Fill.Color(RGBA.Black.withAlpha(0.5)))

    Outcome(
      SceneUpdateFragment(
        layerKey -> Layer(bg, text)
      )
    )

  private def formatText(fps: String): String =
    s"""FPS $fps"""

  private def pickTint(targetFPS: Int, fps: Int): RGBA =
    if (fps > targetFPS - (targetFPS * 0.05)) RGBA.Green
    else if (fps > targetFPS / 2) RGBA.Yellow
    else RGBA.Red

object FPSCounter:

  val DefaultId: SubSystemId = SubSystemId("[indigo_FPSCounter_subsystem]")

  private val defaultPlaceFunction: (Context[Unit], Size) => Point =
    (_, size) => Point(0, 0)

  def apply[Model](fontKey: FontKey, fontAsset: AssetName): FPSCounter[Model] =
    FPSCounter(DefaultId, defaultPlaceFunction, None, None, fontKey, fontAsset)

  def apply[Model](fontKey: FontKey, fontAsset: AssetName, targetFPS: FPS): FPSCounter[Model] =
    FPSCounter(DefaultId, defaultPlaceFunction, Option(targetFPS), None, fontKey, fontAsset)

  def apply[Model](fontKey: FontKey, fontAsset: AssetName, layerKey: LayerKey): FPSCounter[Model] =
    FPSCounter(DefaultId, defaultPlaceFunction, None, Option(layerKey), fontKey, fontAsset)

  def apply[Model](
      fontKey: FontKey,
      fontAsset: AssetName,
      targetFPS: FPS,
      layerKey: LayerKey
  ): FPSCounter[Model] =
    FPSCounter(DefaultId, defaultPlaceFunction, Option(targetFPS), Option(layerKey), fontKey, fontAsset)

  def apply[Model](id: SubSystemId, position: Point, fontKey: FontKey, fontAsset: AssetName): FPSCounter[Model] =
    FPSCounter(id, defaultPlaceFunction, None, None, fontKey, fontAsset)

  def apply[Model](
      id: SubSystemId,
      fontKey: FontKey,
      fontAsset: AssetName,
      targetFPS: FPS
  ): FPSCounter[Model] =
    FPSCounter(id, defaultPlaceFunction, Option(targetFPS), None, fontKey, fontAsset)

  def apply[Model](
      id: SubSystemId,
      fontKey: FontKey,
      fontAsset: AssetName,
      layerKey: LayerKey
  ): FPSCounter[Model] =
    FPSCounter(id, defaultPlaceFunction, None, Option(layerKey), fontKey, fontAsset)

  def apply[Model](
      id: SubSystemId,
      fontKey: FontKey,
      fontAsset: AssetName,
      targetFPS: FPS,
      layerKey: LayerKey
  ): FPSCounter[Model] =
    FPSCounter(id, defaultPlaceFunction, Option(targetFPS), Option(layerKey), fontKey, fontAsset)

  final case class Move(to: Point) extends GlobalEvent

final case class FPSCounterState(
    placeFunction: (Context[Unit], Size) => Point,
    bounds: Rectangle,
    fps: Int,
    lastInterval: Seconds,
    frameCountSinceInterval: Int
)
object FPSCounterState:
  def initial(place: (Context[Unit], Size) => Point): FPSCounterState =
    FPSCounterState(place, Rectangle.zero, 0, Seconds.zero, 0)
