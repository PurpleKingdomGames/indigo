package indigoextras.subsystems

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
    startPosition: Point,
    targetFPS: Option[FPS],
    layerKey: Option[LayerKey],
    fontKey: FontKey,
    fontAsset: AssetName
) extends SubSystem[Model]:
  type EventType      = GlobalEvent
  type SubSystemModel = FPSCounterState
  type ReferenceData  = Unit

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
    Outcome(FPSCounterState.initial(startPosition))

  def update(
      context: SubSystemContext[ReferenceData],
      model: FPSCounterState
  ): GlobalEvent => Outcome[FPSCounterState] = {
    case FrameTick =>
      if (context.frame.time.running >= (model.lastInterval + Seconds(1)))
        Outcome(
          model.copy(
            fps = decideNextFps(model.frameCountSinceInterval),
            lastInterval = context.frame.time.running,
            frameCountSinceInterval = 0
          )
        )
      else Outcome(model.copy(frameCountSinceInterval = model.frameCountSinceInterval + 1))

    case FPSCounter.Move(to) =>
      Outcome(model.copy(position = to))

    case _ =>
      Outcome(model)
  }

  def present(context: SubSystemContext[ReferenceData], model: FPSCounterState): Outcome[SceneUpdateFragment] =
    val text: Text[Material.ImageEffects] =
      Text(
        s"""FPS ${model.fps.toString}""",
        fontKey,
        Material.ImageEffects(fontAsset).withTint(pickTint(idealFps, model.fps))
      ).moveTo(model.position + 2)

    val size: Rectangle =
      context.services.bounds.find(text).getOrElse(Rectangle.zero)

    val boxSize =
      ({ (s: Size) =>
        Size(
          if s.width  % 2 == 0 then s.width else s.width + 1,
          if s.height % 2 == 0 then s.height else s.height + 1
        )
      })(size.expand(2).size)

    val bg: Shape.Box =
      Shape
        .Box(Rectangle(model.position, boxSize), Fill.Color(RGBA.Black.withAlpha(0.5)))

    Outcome(
      SceneUpdateFragment(
        layerKey -> Layer(bg, text)
      )
    )

  def pickTint(targetFPS: Int, fps: Int): RGBA =
    if (fps > targetFPS - (targetFPS * 0.05)) RGBA.Green
    else if (fps > targetFPS / 2) RGBA.Yellow
    else RGBA.Red

object FPSCounter:

  val DefaultId: SubSystemId = SubSystemId("[indigo_FPSCounter_subsystem]")

  def apply[Model](position: Point, fontKey: FontKey, fontAsset: AssetName): SubSystem[Model] =
    FPSCounter(DefaultId, position, None, None, fontKey, fontAsset)

  def apply[Model](position: Point, fontKey: FontKey, fontAsset: AssetName, targetFPS: FPS): SubSystem[Model] =
    FPSCounter(DefaultId, position, Option(targetFPS), None, fontKey, fontAsset)

  def apply[Model](position: Point, fontKey: FontKey, fontAsset: AssetName, layerKey: LayerKey): SubSystem[Model] =
    FPSCounter(DefaultId, position, None, Option(layerKey), fontKey, fontAsset)

  def apply[Model](
      position: Point,
      fontKey: FontKey,
      fontAsset: AssetName,
      targetFPS: FPS,
      layerKey: LayerKey
  ): SubSystem[Model] =
    FPSCounter(DefaultId, position, Option(targetFPS), Option(layerKey), fontKey, fontAsset)

  def apply[Model](id: SubSystemId, position: Point, fontKey: FontKey, fontAsset: AssetName): SubSystem[Model] =
    FPSCounter(id, position, None, None, fontKey, fontAsset)

  def apply[Model](
      id: SubSystemId,
      position: Point,
      fontKey: FontKey,
      fontAsset: AssetName,
      targetFPS: FPS
  ): SubSystem[Model] =
    FPSCounter(id, position, Option(targetFPS), None, fontKey, fontAsset)

  def apply[Model](
      id: SubSystemId,
      position: Point,
      fontKey: FontKey,
      fontAsset: AssetName,
      layerKey: LayerKey
  ): SubSystem[Model] =
    FPSCounter(id, position, None, Option(layerKey), fontKey, fontAsset)

  def apply[Model](
      id: SubSystemId,
      position: Point,
      fontKey: FontKey,
      fontAsset: AssetName,
      targetFPS: FPS,
      layerKey: LayerKey
  ): SubSystem[Model] =
    FPSCounter(id, position, Option(targetFPS), Option(layerKey), fontKey, fontAsset)

  final case class Move(to: Point) extends GlobalEvent

final case class FPSCounterState(position: Point, fps: Int, lastInterval: Seconds, frameCountSinceInterval: Int)
object FPSCounterState:
  def initial(initialPosition: Point): FPSCounterState =
    FPSCounterState(initialPosition, 0, Seconds.zero, 0)
