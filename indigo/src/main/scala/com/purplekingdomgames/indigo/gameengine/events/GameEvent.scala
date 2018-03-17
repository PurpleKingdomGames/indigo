package com.purplekingdomgames.indigo.gameengine.events

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point

sealed trait GameEvent

case object FrameTick extends GameEvent

sealed trait MouseEvent extends GameEvent {
  val x: Int
  val y: Int
  def position: Point = Point(x, y)
}
object MouseEvent {
  case class Click(x: Int, y: Int) extends MouseEvent
  case class MouseUp(x: Int, y: Int) extends MouseEvent
  case class MouseDown(x: Int, y: Int) extends MouseEvent
  case class Move(x: Int, y: Int) extends MouseEvent
}

sealed trait KeyboardEvent extends GameEvent {
  val keyCode: Int
}
object KeyboardEvent {
  case class KeyUp(keyCode: Int) extends KeyboardEvent
  case class KeyDown(keyCode: Int) extends KeyboardEvent
  case class KeyPress(keyCode: Int) extends KeyboardEvent
}

trait ViewEvent extends GameEvent
