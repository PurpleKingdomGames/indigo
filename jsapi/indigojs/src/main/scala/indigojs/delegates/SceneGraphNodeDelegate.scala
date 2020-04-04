package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.Group
import indigo.shared.datatypes.TextAlignment
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneGraphNodePrimitive
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Sprite
import indigo.shared.datatypes.BindingKey
import indigo.shared.animation.AnimationKey

import indigojs.delegates.clones.CloneableDelegate
import indigojs.delegates.geometry.Vector2Delegate
import indigojs.delegates.PointDelegate
import indigojs.delegates.EffectsUtilities._

trait SceneGraphNodeDelegate {
  def toInternal: SceneGraphNode
}

sealed trait SceneGraphNodePrimitiveDelegate extends SceneGraphNodeDelegate {
  def toInternal: SceneGraphNodePrimitive
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Group")
final class GroupDelegate(
    _x: Int,
    _y: Int,
    _depth: Int,
    _rotation: Double,
    _scaleX: Double,
    _scaleY: Double,
    _children: js.Array[SceneGraphNodePrimitiveDelegate]
) extends SceneGraphNodePrimitiveDelegate {

  @JSExport
  val x = _x
  @JSExport
  val y = _y
  @JSExport
  val depth = _depth
  @JSExport
  val rotation = _rotation
  @JSExport
  val scaleX = _scaleX
  @JSExport
  val scaleY = _scaleY
  @JSExport
  val children = _children

  def toInternal: Group =
    new Group(
      Point(x, y),
      Radians(rotation),
      Vector2(scaleX, scaleY),
      Depth(depth),
      children.map(_.toInternal).toList
    )
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Graphic")
final class GraphicDelegate(
    _bounds: RectangleDelegate,
    _depth: Int,
    _rotation: Double,
    _scaleX: Double,
    _scaleY: Double,
    _ref: PointDelegate,
    _crop: RectangleDelegate,
    _effects: EffectsDelegate,
    _material: MaterialDelegate
) extends SceneGraphNodePrimitiveDelegate
    with CloneableDelegate {

  @JSExport
  val bounds = _bounds
  @JSExport
  val depth = _depth
  @JSExport
  val rotation = _rotation
  @JSExport
  val scaleX = _scaleX
  @JSExport
  val scaleY = _scaleY
  @JSExport
  val ref = _ref
  @JSExport
  val crop = _crop
  @JSExport
  val effects = _effects
  @JSExport
  val material = _material

  @JSExport
  def moveTo(pt: Point): GraphicDelegate =
    fromInternal(toInternal.moveTo(pt))

  @JSExport
  def moveTo(x: Int, y: Int): GraphicDelegate =
    moveTo(Point(x, y))

  @JSExport
  def moveBy(pt: PointDelegate): GraphicDelegate =
    fromInternal(toInternal.moveBy(pt.toInternal))

  @JSExport
  def moveBy(x: Int, y: Int): GraphicDelegate =
    moveBy(new PointDelegate(x, y))

  @JSExport
  def rotate(angle: Double): GraphicDelegate =
    fromInternal(toInternal.rotate(Radians(angle)))

  @JSExport
  def rotateBy(angle: Double): GraphicDelegate =
    rotate(rotation + angle)

  @JSExport
  def scaleBy(amount: Vector2): GraphicDelegate =
    fromInternal(toInternal.scaleBy(amount))

  @JSExport
  def scaleBy(x: Double, y: Double): GraphicDelegate =
    scaleBy(Vector2(x, y))

  @JSExport
  def transformTo(newPosition: PointDelegate, newRotation: Double, newScale: Vector2Delegate): SceneGraphNodePrimitive =
    toInternal.transformTo(newPosition.toInternal, Radians(newRotation), newScale.toInternal)

  @JSExport
  def transformBy(positionDiff: PointDelegate, rotationDiff: Double, scaleDiff: Vector2Delegate): SceneGraphNodePrimitive =
    toInternal.transformBy(positionDiff.toInternal, Radians(rotationDiff), scaleDiff.toInternal)

  @JSExport
  def withDepth(depthValue: Int): GraphicDelegate =
    fromInternal(toInternal.withDepth(Depth(depthValue)))

  @JSExport
  def withAlpha(a: Double): GraphicDelegate =
    fromInternal(toInternal.withAlpha(a))

  @JSExport
  def withTint(tint: RGBADelegate): GraphicDelegate =
    fromInternal(toInternal.withTint(tint.toInternal))

  @JSExport
  def withTint(red: Double, green: Double, blue: Double): GraphicDelegate =
    fromInternal(toInternal.withTint(red, green, blue))

  @JSExport
  def withTint(red: Double, green: Double, blue: Double, amount: Double): GraphicDelegate =
    fromInternal(toInternal.withTint(red, green, blue, amount))

  @JSExport
  def flipHorizontal(hValue: Boolean): GraphicDelegate =
    fromInternal(toInternal.flipHorizontal(hValue))

  @JSExport
  def flipVertical(vValue: Boolean): GraphicDelegate =
    fromInternal(toInternal.flipVertical(vValue))

  @JSExport
  def withRef(refValue: PointDelegate): GraphicDelegate =
    fromInternal(toInternal.withRef(refValue.toInternal))

  @JSExport
  def withRef(xValue: Int, yValue: Int): GraphicDelegate =
    withRef(new PointDelegate(xValue, yValue))

  @JSExport
  def withCrop(crop: RectangleDelegate): GraphicDelegate =
    fromInternal(toInternal.withCrop(crop.toInternal))

  @JSExport
  def withCrop(xValue: Int, yValue: Int, widthValue: Int, heightValue: Int): GraphicDelegate =
    withCrop(new RectangleDelegate(xValue, yValue, widthValue, heightValue))

  def fromInternal(orig: Graphic): GraphicDelegate =
    new GraphicDelegate(
      RectangleDelegate.fromRectangle(orig.bounds),
      orig.depth.zIndex,
      orig.rotation.value,
      orig.scale.x,
      orig.scale.y,
      PointDelegate.fromPoint(orig.ref),
      RectangleDelegate.fromRectangle(orig.crop),
      EffectsDelegate.fromInternal(orig.effects),
      MaterialDelegate.fromInternal(orig.material)
    )

  def toInternal: Graphic =
    new Graphic(
      bounds.toInternal,
      Depth(depth),
      Radians(rotation),
      Vector2(scaleX, scaleY),
      ref.toInternal,
      crop.toInternal,
      effects.toInternal,
      material.toInternal
    )
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Sprite")
final class SpriteDelegate(
    _bindingKey: String,
    _bounds: RectangleDelegate,
    _depth: Int,
    _rotation: Double,
    _scaleX: Double,
    _scaleY: Double,
    _animationKey: String,
    _ref: PointDelegate,
    _effects: EffectsDelegate,
    _eventHandler: js.Function2[RectangleDelegate, GlobalEventDelegate, js.Array[GlobalEventDelegate]]
) extends SceneGraphNodePrimitiveDelegate
    with CloneableDelegate {

  @JSExport
  val bindingKey = _bindingKey
  @JSExport
  val bounds = _bounds
  @JSExport
  val depth = _depth
  @JSExport
  val rotation = _rotation
  @JSExport
  val scaleX = _scaleX
  @JSExport
  val scaleY = _scaleY
  @JSExport
  val animationKey = _animationKey
  @JSExport
  val ref = _ref
  @JSExport
  val effects = _effects
  @JSExport
  val eventHandler = _eventHandler

  def toInternal: Sprite =
    new Sprite(
      new BindingKey(bindingKey),
      bounds.toInternal,
      Depth(depth),
      Radians(rotation),
      Vector2(scaleX, scaleY),
      AnimationKey(animationKey),
      ref.toInternal,
      effects.toInternal,
      (p: (Rectangle, GlobalEvent)) => eventHandler(RectangleDelegate.fromRectangle(p._1), GlobalEventDelegate.fromGlobalEvent(p._2)).toList.map(_.toInternal)
    )

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Text")
final class TextDelegate(
    _text: String,
    _alignment: String,
    _x: Int,
    _y: Int,
    _depth: Int,
    _rotation: Double,
    _scaleX: Double,
    _scaleY: Double,
    _fontKey: String,
    _effects: EffectsDelegate,
    _eventHandler: js.Function2[RectangleDelegate, GlobalEventDelegate, js.Array[GlobalEventDelegate]]
) extends SceneGraphNodePrimitiveDelegate {

  @JSExport
  val text = _text
  @JSExport
  val alignment = _alignment
  @JSExport
  val x = _x
  @JSExport
  val y = _y
  @JSExport
  val depth = _depth
  @JSExport
  val rotation = _rotation
  @JSExport
  val scaleX = _scaleX
  @JSExport
  val scaleY = _scaleY
  @JSExport
  val fontKey = _fontKey
  @JSExport
  val effects = _effects
  @JSExport
  val eventHandler = _eventHandler

  def toInternal: Text =
    new Text(
      text,
      TextDelegate.stringToAlignment(alignment.toLowerCase()),
      Point(x, y),
      Depth(depth),
      Radians(rotation),
      Vector2(scaleX, scaleY),
      FontKey(fontKey),
      effects.toInternal,
      (p: (Rectangle, GlobalEvent)) => eventHandler(RectangleDelegate.fromRectangle(p._1), GlobalEventDelegate.fromGlobalEvent(p._2)).toList.map(_.toInternal)
    )
}
object TextDelegate {

  val stringToAlignment: String => TextAlignment = {
    case "left"   => TextAlignment.Left
    case "center" => TextAlignment.Center
    case "right"  => TextAlignment.Right
  }

}

object SceneGraphNodeUtilities {
  implicit class SpriteConvert(val obj: Sprite) {
    def toJsDelegate = new SpriteDelegate(
      obj.bindingKey.value,
      new RectangleDelegate(obj.bounds.x, obj.bounds.y, obj.bounds.width, obj.bounds.height),
      obj.depth.zIndex,
      obj.rotation.value,
      obj.scale.x,
      obj.scale.y,
      obj.animationsKey.toString,
      new PointDelegate(obj.ref.x, obj.ref.y),
      obj.effects.toJsDelegate,
      (rect: RectangleDelegate, event: GlobalEventDelegate) => obj.eventHandler((rect.toInternal, event.toInternal)).map(GlobalEventDelegate.fromGlobalEvent(_)).toJSArray
    )
  }
}
