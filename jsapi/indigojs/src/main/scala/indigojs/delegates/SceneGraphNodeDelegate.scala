package indigojs.delegates

import scala.scalajs.js.annotation._
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
    _imageAssetRef: String,
    _ref: PointDelegate,
    _crop: RectangleDelegate,
    _effects: EffectsDelegate
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
  val imageAssetRef = _imageAssetRef
  @JSExport
  val ref = _ref
  @JSExport
  val crop = _crop
  @JSExport
  val effects = _effects

  def toInternal: Graphic =
    new Graphic(
      bounds.toInternal,
      Depth(depth),
      Radians(rotation),
      Vector2(scaleX, scaleY),
      imageAssetRef,
      ref.toInternal,
      crop.toInternal,
      effects.toInternal
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
