package indigo.gameengine.events

import indigo.gameengine.constants.KeyCode
import indigo.gameengine.scenegraph.datatypes.Point

// GameEvents are a fixed set of events that move through the game engine and live for 1 frame.
sealed trait GameEvent {
  val isGameEvent: Boolean
}

case object FrameTick extends GameEvent {
  val isGameEvent: Boolean = true
}

sealed trait MouseEvent extends GameEvent {
  val x: Int
  val y: Int
  def position: Point      = Point(x, y)
  val isGameEvent: Boolean = true
}
object MouseEvent {
  case class Click(x: Int, y: Int)     extends MouseEvent
  case class MouseUp(x: Int, y: Int)   extends MouseEvent
  case class MouseDown(x: Int, y: Int) extends MouseEvent
  case class Move(x: Int, y: Int)      extends MouseEvent
}

sealed trait KeyboardEvent extends GameEvent {
  val keyCode: KeyCode
  val isGameEvent: Boolean = true
}
object KeyboardEvent {
  case class KeyUp(keyCode: KeyCode)    extends KeyboardEvent
  case class KeyDown(keyCode: KeyCode)  extends KeyboardEvent
  case class KeyPress(keyCode: KeyCode) extends KeyboardEvent
}

// ViewEvents are emitted by the view function
trait ViewEvent extends GameEvent {
  val isGameEvent: Boolean = false
}

// FrameEvents are passed from Model->ViewModel->View
trait FrameEvent
