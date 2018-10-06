package indigo.gameengine.events

import indigo.gameengine.constants.KeyCode
import indigo.gameengine.scenegraph.datatypes.Point

trait GameEvent

case object FrameTick extends GameEvent

sealed trait MouseEvent extends GameEvent {
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

sealed trait KeyboardEvent extends GameEvent {
  val keyCode: KeyCode
}
object KeyboardEvent {
  case class KeyUp(keyCode: KeyCode)    extends KeyboardEvent
  case class KeyDown(keyCode: KeyCode)  extends KeyboardEvent
  case class KeyPress(keyCode: KeyCode) extends KeyboardEvent
}

trait ViewEvent extends GameEvent
