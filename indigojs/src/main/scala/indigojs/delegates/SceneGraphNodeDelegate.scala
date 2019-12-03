package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.scenegraph.Text
import indigo.shared.datatypes.TextAlignment
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent

trait SceneGraphNodeDelegate {
  def toInternal: SceneGraphNode
}

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
) extends SceneGraphNodeDelegate {
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
