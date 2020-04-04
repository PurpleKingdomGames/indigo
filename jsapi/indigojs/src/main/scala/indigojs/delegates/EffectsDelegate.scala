package indigojs.delegates

import indigo.shared.datatypes.Effects
import scala.scalajs.js.annotation._
import indigo.shared.datatypes.Border
import indigo.shared.datatypes.Glow
import indigo.shared.datatypes.Thickness
import indigo.shared.datatypes.Overlay
import indigo.shared.datatypes.Overlay.Color
import indigo.shared.datatypes.Overlay.LinearGradiant

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Effects")
final class EffectsDelegate(_tint: RGBADelegate, _overlay: OverlayDelegate, _border: BorderDelegate, _glow: GlowDelegate, _alpha: Double, _flip: FlipDelegate) {

  @JSExport
  val tint = _tint
  @JSExport
  val overlay = _overlay
  @JSExport
  val border = _border
  @JSExport
  val glow = _glow
  @JSExport
  val alpha = _alpha
  @JSExport
  val flip = _flip

  @JSExport
  def withTint(newTint: RGBADelegate): EffectsDelegate =
    new EffectsDelegate(newTint, overlay, border, glow, alpha, flip)

  @JSExport
  def withOverlay(newOverlay: OverlayDelegate): EffectsDelegate =
    new EffectsDelegate(tint, newOverlay, border, glow, alpha, flip)

  @JSExport
  def withBorder(newBorder: BorderDelegate): EffectsDelegate =
    new EffectsDelegate(tint, overlay, newBorder, glow, alpha, flip)

  @JSExport
  def withGlow(newGlow: GlowDelegate): EffectsDelegate =
    new EffectsDelegate(tint, overlay, border, newGlow, alpha, flip)

  @JSExport
  def withAlpha(newAlpha: Double): EffectsDelegate =
    new EffectsDelegate(tint, overlay, border, glow, newAlpha, flip)

  @JSExport
  def withFlip(newFlip: FlipDelegate): EffectsDelegate =
    new EffectsDelegate(tint, overlay, border, glow, alpha, newFlip)

  def toInternal: Effects =
    new Effects(tint.toInternal, overlay.toInternal, border.toInternal, glow.toInternal, alpha, flip.toInternal)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("EffectsHelper")
object EffectsDelegate {

  @JSExport
  def None: EffectsDelegate =
    new EffectsDelegate(RGBADelegate.None, ColorDelegate.default, BorderDelegate.None, GlowDelegate.None, 1.0d, FlipDelegate.None)

  def fromInternal(effects: Effects): EffectsDelegate =
    new EffectsDelegate(
      new RGBADelegate(effects.tint.r, effects.tint.g, effects.tint.b, effects.tint.a),
      OverlayDelegate.fromInternal(effects.overlay),
      BorderDelegate.fromInternal(effects.border),
      GlowDelegate.fromInternal(effects.glow),
      effects.alpha,
      new FlipDelegate(effects.flip.horizontal, effects.flip.vertical)
    )

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Border")
final class BorderDelegate(_color: RGBADelegate, _innerThickness: Int, _outerThickness: Int) {

  @JSExport
  val color = _color
  @JSExport
  val innerThickness = _innerThickness
  @JSExport
  val outerThickness = _outerThickness

  private def intToThickness(i: Int): Thickness =
    i match {
      case 0 => Thickness.None
      case 1 => Thickness.Thin
      case 2 => Thickness.Thick
      case _ => Thickness.None
    }

  def toInternal: Border =
    new Border(color.toInternal, intToThickness(innerThickness), intToThickness(outerThickness))
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("BorderHelper")
object BorderDelegate {
  @JSExport
  val None: BorderDelegate =
    new BorderDelegate(RGBADelegate.None, 0, 0)

  private def thicknessToInt(t: Thickness): Int =
    t match {
      case Thickness.None  => 0
      case Thickness.Thin  => 1
      case Thickness.Thick => 2
    }

  def fromInternal(b: Border): BorderDelegate =
    new BorderDelegate(
      RGBADelegate.fromInternal(b.color),
      thicknessToInt(b.innerThickness),
      thicknessToInt(b.outerThickness)
    )
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Glow")
final class GlowDelegate(_color: RGBADelegate, _innerGlowAmount: Double, _outerGlowAmount: Double) {

  @JSExport
  val color = _color
  @JSExport
  val innerGlowAmount = _innerGlowAmount
  @JSExport
  val outerGlowAmount = _outerGlowAmount

  def toInternal: Glow =
    new Glow(color.toInternal, innerGlowAmount, outerGlowAmount)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GlowHelper")
object GlowDelegate {
  @JSExport
  val None: GlowDelegate =
    new GlowDelegate(RGBADelegate.None, 0.0d, 0.0d)

  def fromInternal(g: Glow): GlowDelegate =
    new GlowDelegate(
      RGBADelegate.fromInternal(g.color),
      g.innerGlowAmount,
      g.outerGlowAmount
    )
}

sealed trait OverlayDelegate {
  def toInternal: Overlay =
    this match {
      case c: ColorDelegate =>
        new Overlay.Color(c.color.toInternal)

      case l: LinearGradiantDelegate =>
        new Overlay.LinearGradiant(
          l.fromPoint.toInternal,
          l.fromColor.toInternal,
          l.toPoint.toInternal,
          l.toColor.toInternal
        )
    }
}
object OverlayDelegate {
  def fromInternal(o: Overlay): OverlayDelegate =
    o match {
      case c: Color =>
        new ColorDelegate(RGBADelegate.fromInternal(c.color))

      case l: LinearGradiant =>
        new LinearGradiantDelegate(
          PointDelegate.fromPoint(l.fromPoint),
          RGBADelegate.fromInternal(l.fromColor),
          PointDelegate.fromPoint(l.toPoint),
          RGBADelegate.fromInternal(l.toColor)
        )
    }
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Color")
final class ColorDelegate(_color: RGBADelegate) extends OverlayDelegate {
  @JSExport
  val color = _color
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("ColorHelper")
object ColorDelegate {
  @JSExport
  val default: ColorDelegate =
    new ColorDelegate(RGBADelegate.Zero)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("LinearGradiant")
final class LinearGradiantDelegate(
    _fromPoint: PointDelegate,
    _fromColor: RGBADelegate,
    _toPoint: PointDelegate,
    _toColor: RGBADelegate
) extends OverlayDelegate {
  @JSExport
  val fromPoint = _fromPoint
  @JSExport
  val fromColor = _fromColor
  @JSExport
  val toPoint = _toPoint
  @JSExport
  val toColor = _toColor
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("LinearGradiantHelper")
object LinearGradiantDelegate {
  @JSExport
  val default: LinearGradiantDelegate =
    new LinearGradiantDelegate(
      new PointDelegate(0, 0),
      RGBADelegate.Zero,
      new PointDelegate(0, 0),
      RGBADelegate.Zero
    )
}

object EffectsUtilities {
  implicit class EffectsConvert(val obj: Effects) {
    def toJsDelegate =
      new EffectsDelegate(
        new RGBADelegate(obj.tint.r, obj.tint.g, obj.tint.b, obj.tint.a),
        OverlayDelegate.fromInternal(obj.overlay),
        BorderDelegate.fromInternal(obj.border),
        GlowDelegate.fromInternal(obj.glow),
        obj.alpha,
        new FlipDelegate(obj.flip.horizontal, obj.flip.vertical)
      )
  }
}
