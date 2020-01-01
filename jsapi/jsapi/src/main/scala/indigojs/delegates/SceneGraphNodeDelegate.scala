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

sealed trait SceneGraphNodeDelegate {
  def toInternal: SceneGraphNode
}

sealed trait SceneGraphNodePrimitiveDelegate extends SceneGraphNodeDelegate {
  def toInternal: SceneGraphNodePrimitive
}

@JSExportTopLevel("Group")
final class GroupDelegate(
    val x: Int,
    val y: Int,
    val depth: Int,
    val rotation: Double,
    val scaleX: Double,
    val scaleY: Double,
    val children: js.Array[SceneGraphNodePrimitiveDelegate]
) extends SceneGraphNodePrimitiveDelegate {
  def toInternal: Group =
    new Group(
      Point(x, y),
      Radians(rotation),
      Vector2(scaleX, scaleY),
      Depth(depth),
      children.map(_.toInternal).toList
    )
}

@JSExportTopLevel("Graphic")
final class GraphicDelegate(
    val bounds: RectangleDelegate,
    val depth: Int,
    val rotation: Double,
    val scaleX: Double,
    val scaleY: Double,
    val imageAssetRef: String,
    val ref: PointDelegate,
    val crop: RectangleDelegate,
    val effects: EffectsDelegate
) extends SceneGraphNodePrimitiveDelegate
    with CloneableDelegate {
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

@JSExportTopLevel("Sprite")
final class SpriteDelegate(
    val bindingKey: String,
    val bounds: RectangleDelegate,
    val depth: Int,
    val rotation: Double,
    val scaleX: Double,
    val scaleY: Double,
    val animationKey: String,
    val ref: PointDelegate,
    val effects: EffectsDelegate,
    val eventHandler: js.Function2[RectangleDelegate, GlobalEventDelegate, js.Array[GlobalEventDelegate]]
) extends SceneGraphNodePrimitiveDelegate
    with CloneableDelegate {
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
      (p: (Rectangle, GlobalEvent)) => eventHandler(RectangleDelegate.fromRectangle(p._1), GlobalEventDelegate.fromGlobalEvent(p._2)).toList
    )

}

@JSExportTopLevel("Text")
final class TextDelegate(
    val text: String,
    val alignment: String,
    val x: Int,
    val y: Int,
    val depth: Int,
    val rotation: Double,
    val scaleX: Double,
    val scaleY: Double,
    val fontKey: String,
    val effects: EffectsDelegate,
    val eventHandler: js.Function2[RectangleDelegate, GlobalEventDelegate, js.Array[GlobalEventDelegate]]
) extends SceneGraphNodePrimitiveDelegate {
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
      (p: (Rectangle, GlobalEvent)) => eventHandler(RectangleDelegate.fromRectangle(p._1), GlobalEventDelegate.fromGlobalEvent(p._2)).toList
    )
}
object TextDelegate {

  val stringToAlignment: String => TextAlignment = {
    case "left"   => TextAlignment.Left
    case "center" => TextAlignment.Center
    case "right"  => TextAlignment.Right
  }

}
