package indigo.gameengine.events

import indigo.gameengine.constants.KeyCode
import indigo.gameengine.scenegraph.Volume
import indigo.gameengine.scenegraph.datatypes.Point

// FrameEvents are passed from Model->ViewModel->View
trait InFrameEvent

// Events that are passed to the GlobalEventStream
trait GlobalEvent

case object FrameTick extends GlobalEvent

sealed trait MouseEvent extends GlobalEvent {
  val x: Int
  val y: Int
  def position: Point = Point(x, y)
}
object MouseEvent {
  case class Click(x: Int, y: Int)     extends MouseEvent
  case class MouseUp(x: Int, y: Int)   extends MouseEvent
  case class MouseDown(x: Int, y: Int) extends MouseEvent
  case class Move(x: Int, y: Int)      extends MouseEvent
}

sealed trait KeyboardEvent extends GlobalEvent {
  val keyCode: KeyCode
}
object KeyboardEvent {
  case class KeyUp(keyCode: KeyCode)    extends KeyboardEvent
  case class KeyDown(keyCode: KeyCode)  extends KeyboardEvent
  case class KeyPress(keyCode: KeyCode) extends KeyboardEvent
}

case class PlaySound(assetRef: String, volume: Volume) extends GlobalEvent

trait NetworkSendEvent    extends GlobalEvent
trait NetworkReceiveEvent extends GlobalEvent
