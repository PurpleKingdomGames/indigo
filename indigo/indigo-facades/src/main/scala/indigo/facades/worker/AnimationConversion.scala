package indigo.facades.worker

import indigo.shared.animation.Animation
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.AnimationKey
import indigo.shared.animation.Cycle
import indigo.shared.animation.CycleLabel
import indigo.shared.animation.Frame
import indigo.shared.collections.NonEmptyList
import indigo.shared.time.Millis

import scala.scalajs.js
import scalajs.js.JSConverters._

object AnimationConversion {

  def toJS(animation: Animation): js.Any =
    js.Dynamic.literal(
      animationKey = animation.animationKey.value,
      material = MaterialConversion.toJS(animation.material),
      currentCycleLabel = animation.currentCycleLabel.value,
      cycles = animation.cycles.toList.map(CycleConversion.toJS).toJSArray
    )

  def fromJS(obj: js.Any): Animation =
    fromAnimationJS(obj.asInstanceOf[AnimationJS])

  def fromAnimationJS(res: AnimationJS): Animation =
    Animation(
      animationKey = AnimationKey(res.animationKey),
      material = MaterialConversion.fromMaterialJS(res.material),
      currentCycleLabel = CycleLabel(res.currentCycleLabel),
      cycles = NonEmptyList.fromList(res.cycles.toList.map(CycleConversion.fromCycleJS)).get
    )

  object CycleConversion {

    def toJS(cycle: Cycle): js.Any =
      js.Dynamic.literal(
        label = cycle.label.value,
        frames = cycle.frames.toList.map(FrameConversion.toJS).toJSArray,
        playheadPosition = cycle.playheadPosition,
        lastFrameAdvance = cycle.lastFrameAdvance.value.toDouble
      )

    def fromJS(obj: js.Any): Cycle =
      fromCycleJS(obj.asInstanceOf[CycleJS])

    def fromCycleJS(res: CycleJS): Cycle =
      Cycle(
        label = CycleLabel(res.label),
        frames = NonEmptyList.fromList(res.frames.toList.map(FrameConversion.fromFrameJS)).get,
        playheadPosition = res.playheadPosition,
        lastFrameAdvance = Millis(res.lastFrameAdvance.toLong)
      )

  }

  object FrameConversion {

    def toJS(frame: Frame): js.Any =
      js.Dynamic.literal(
        crop = RectangleConversion.toJS(frame.crop),
        duration = frame.duration.value.toDouble,
        frameMaterial = frame.frameMaterial.map(MaterialConversion.toJS).orUndefined
      )

    def fromJS(obj: js.Any): Frame =
      fromFrameJS(obj.asInstanceOf[FrameJS])

    def fromFrameJS(res: FrameJS): Frame =
      Frame(
        crop = RectangleConversion.fromRectangleJS(res.crop),
        duration = Millis(res.duration.toLong),
        frameMaterial = res.frameMaterial.toOption.map(MaterialConversion.fromMaterialJS)
      )

  }

}
