package indigojs.delegates

import indigojs.IndigoJSException
import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js
import indigo.shared.animation.AnimationAction.JumpToFrame
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.AnimationAction.Play
import indigo.shared.animation.AnimationAction.ChangeCycle
import indigo.shared.animation.AnimationAction.JumpToFirstFrame
import indigo.shared.animation.AnimationAction.JumpToLastFrame
import indigo.shared.animation.CycleLabel
import indigo.shared.animation.Animation
import indigo.shared.animation.AnimationKey
import indigo.shared.datatypes.ImageAssetRef
import indigo.shared.collections.NonEmptyList
import indigo.shared.animation.Frame
import indigo.shared.animation.Cycle
import indigo.shared.datatypes.Point

@SuppressWarnings(Array("org.wartremover.warts.Throw", "org.wartremover.warts.Any"))
@JSExportTopLevel("Animation")
final class AnimationDelegate(
    _animationsKey: String,
    _imageAssetRef: String,
    _spriteSheetWidth: Int,
    _spriteSheetHeight: Int,
    _cycles: js.Array[CycleDelegate]
) {

  @JSExport
  val animationsKey = _animationsKey
  @JSExport
  val imageAssetRef = _imageAssetRef
  @JSExport
  val spriteSheetWidth = _spriteSheetWidth
  @JSExport
  val spriteSheetHeight = _spriteSheetHeight
  @JSExport
  val cycles = _cycles

  def toInternal: Animation =
    NonEmptyList.fromList(cycles.map(_.toInternal).toList) match {
      case None =>
        throw new IndigoJSException("Animations cycle list cannot be empty.")

      case Some(animationsNel) =>
        new Animation(
          AnimationKey(animationsKey),
          ImageAssetRef(imageAssetRef),
          Point(spriteSheetWidth, spriteSheetHeight),
          animationsNel.head.label,
          animationsNel,
          Nil
        )
    }
}

@SuppressWarnings(Array("org.wartremover.warts.Throw", "org.wartremover.warts.Any"))
@JSExportTopLevel("Cycle")
final class CycleDelegate(_label: String, _frames: js.Array[FrameDelegate]) {

  @JSExport
  val label = _label
  @JSExport
  val frames = _frames

  def toInternal: Cycle =
    NonEmptyList.fromList(frames.map(_.toInternal).toList) match {
      case None =>
        throw new IndigoJSException("Cycle frames list cannot be empty.")

      case Some(framesNel) =>
        new Cycle(
          CycleLabel(label),
          framesNel,
          0,
          0
        )
    }
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Frame")
final class FrameDelegate(_bounds: RectangleDelegate, _duration: Int) {

  @JSExport
  val bounds = _bounds
  @JSExport
  val duration = _duration

  def toInternal: Frame =
    new Frame(bounds.toInternal, duration)
}

sealed trait AnimationActionDelegate {
  def toInternal: AnimationAction
}

@JSExportTopLevel("Play")
final class PlayDelegate extends AnimationActionDelegate {
  def toInternal: AnimationAction =
    Play
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("ChangeCycle")
final class ChangeCycleDelegate(_label: String) extends AnimationActionDelegate {

  @JSExport
  val label = _label

  def toInternal: AnimationAction =
    ChangeCycle(CycleLabel(label))
}

@JSExportTopLevel("JumpToFirstFrame")
final class JumpToFirstFrameDelegate extends AnimationActionDelegate {
  def toInternal: AnimationAction =
    JumpToFirstFrame
}

@JSExportTopLevel("JumpToLastFrame")
final class JumpToLastFrameDelegate extends AnimationActionDelegate {
  def toInternal: AnimationAction =
    JumpToLastFrame
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("JumpToFrame")
final class JumpToFrameDelegate(_number: Int) extends AnimationActionDelegate {

  @JSExport
  val number = _number

  def toInternal: AnimationAction =
    JumpToFrame(number)
}

object AnimationUtilities {
    implicit class AnimationConvert(val obj: Animation) {
        def toJsDelegate =
            new AnimationDelegate(
                obj.animationsKey.toString,
                obj.imageAssetRef.ref,
                obj.spriteSheetSize.x,
                obj.spriteSheetSize.y,
                obj.cycles.map(_.toJsDelegate).toList.toJSArray
            )
    }

    implicit class CycleConvert(val obj: Cycle) {
        def toJsDelegate =
            new CycleDelegate(
                obj.label.value,
                obj.frames.map(f =>
                    new FrameDelegate(
                        new RectangleDelegate(f.bounds.x, f.bounds.y, f.bounds.width, f.bounds.height),
                        f.duration
                    )
                ).toList.toJSArray
            )
    }
}