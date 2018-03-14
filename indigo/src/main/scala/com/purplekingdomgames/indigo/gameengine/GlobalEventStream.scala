package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Point, Rectangle}

import scala.collection.mutable

object GlobalEventStream {

  private val eventQueue: mutable.Queue[GameEvent] =
    new mutable.Queue[GameEvent]()

  def push(e: GameEvent): Unit = {
    eventQueue += e
    ()
  }

  def collect: List[GameEvent] =
    eventQueue.dequeueAll(_ => true).toList

}

sealed trait GameEvent

case object FrameTick extends GameEvent

sealed trait MouseEvent extends GameEvent {
  val x: Int
  val y: Int
  def position: Point = Point(x, y)
}
case class MouseClick(x: Int, y: Int) extends MouseEvent
case class MouseUp(x: Int, y: Int) extends MouseEvent
case class MouseDown(x: Int, y: Int) extends MouseEvent
case class MousePosition(x: Int, y: Int) extends MouseEvent

sealed trait KeyboardEvent extends GameEvent {
  val keyCode: Int
}
case class KeyUp(keyCode: Int) extends KeyboardEvent
case class KeyDown(keyCode: Int) extends KeyboardEvent
case class KeyPress(keyCode: Int) extends KeyboardEvent

trait ViewEvent extends GameEvent

object Keys {

  val BACKSPACE: Int = 8
  val TAB: Int = 9
  val ENTER: Int = 13
  val SHIFT: Int = 16
  val CTRL: Int = 17
  val ALT: Int = 18
  val PAUSE_BREAK: Int = 19
  val CAPS_LOCK: Int = 20
  val ESCAPE: Int = 27
  val SPACE: Int = 32
  val PAGE_UP: Int = 33
  val PAGE_DOWN: Int = 34
  val END: Int = 35
  val HOME: Int = 36
  val LEFT_ARROW: Int = 37
  val UP_ARROW: Int = 38
  val RIGHT_ARROW: Int = 39
  val DOWN_ARROW: Int = 40
  val INSERT: Int = 45
  val DELETE: Int = 46
  val KEY_0: Int = 48
  val KEY_1: Int = 49
  val KEY_2: Int = 50
  val KEY_3: Int = 51
  val KEY_4: Int = 52
  val KEY_5: Int = 53
  val KEY_6: Int = 54
  val KEY_7: Int = 55
  val KEY_8: Int = 56
  val KEY_9: Int = 57
  val KEY_A: Int = 65
  val KEY_B: Int = 66
  val KEY_C: Int = 67
  val KEY_D: Int = 68
  val KEY_E: Int = 69
  val KEY_F: Int = 70
  val KEY_G: Int = 71
  val KEY_H: Int = 72
  val KEY_I: Int = 73
  val KEY_J: Int = 74
  val KEY_K: Int = 75
  val KEY_L: Int = 76
  val KEY_M: Int = 77
  val KEY_N: Int = 78
  val KEY_O: Int = 79
  val KEY_P: Int = 80
  val KEY_Q: Int = 81
  val KEY_R: Int = 82
  val KEY_S: Int = 83
  val KEY_T: Int = 84
  val KEY_U: Int = 85
  val KEY_V: Int = 86
  val KEY_W: Int = 87
  val KEY_X: Int = 88
  val KEY_Y: Int = 89
  val KEY_Z: Int = 90
  val LEFT_WINDOW_KEY: Int = 91
  val RIGHT_WINDOW_KEY: Int = 92
  val SELECT_KEY: Int = 93
  val NUMPAD_0: Int = 96
  val NUMPAD_1: Int = 97
  val NUMPAD_2: Int = 98
  val NUMPAD_3: Int = 99
  val NUMPAD_4: Int = 100
  val NUMPAD_5: Int = 101
  val NUMPAD_6: Int = 102
  val NUMPAD_7: Int = 103
  val NUMPAD_8: Int = 104
  val NUMPAD_9: Int = 105
  val MULTIPLY: Int = 106
  val ADD: Int = 107
  val SUBTRACT: Int = 109
  val DECIMAL_POINT: Int = 110
  val DIVIDE: Int = 111
  val F1: Int = 112
  val F2: Int = 113
  val F3: Int = 114
  val F4: Int = 115
  val F5: Int = 116
  val F6: Int = 117
  val F7: Int = 118
  val F8: Int = 119
  val F9: Int = 120
  val F10: Int = 121
  val F11: Int = 122
  val F12: Int = 123
  val NUM_LOCK: Int = 144
  val SCROLL_LOCK: Int = 145
  val SEMI_COLON: Int = 186
  val EQUAL_SIGN: Int = 187
  val COMMA: Int = 188
  val DASH: Int = 189
  val PERIOD: Int = 190
  val FORWARD_SLASH: Int = 191
  val GRAVE_ACCENT: Int = 192
  val OPEN_BRACKET: Int = 219
  val BACK_SLASH: Int = 220
  val CLOSE_BRAKET: Int = 221
  val SINGLE_QUOTE: Int = 222

}

/**
  * Holds all the events that will be passed on to the view. These are world events only! View events go to the model.
  * @param events A list of GameEvents
  */
case class FrameInputEvents(events: List[GameEvent]) extends FrameMouseEvents with FrameKeyboardEvents

trait FrameMouseEvents {

  val events: List[GameEvent]

  val mouseEvents: List[MouseEvent] = events.collect { case e: MouseEvent => e }

  val mouseClickAt: Option[Point] = mouseEvents.collectFirst { case m: MouseClick => m.position }
  val mouseUpAt: Option[Point] = mouseEvents.collectFirst { case m: MouseUp => m.position }
  val mouseDownAt: Option[Point] = mouseEvents.collectFirst { case m: MouseDown => m.position }
  val mousePositionAt: Option[Point] = mouseEvents.collectFirst { case m: MousePosition => m.position }

  // At
  private def wasMouseAt(position: Point, maybePosition: Option[Point]): Boolean =
    maybePosition match {
      case Some(pt) => position == pt
      case None => false
    }

  def wasMouseClickedAt(position: Point): Boolean = wasMouseAt(position, mouseClickAt)
  def wasMouseClickedAt(x: Int, y: Int): Boolean = wasMouseClickedAt(Point(x, y))

  def wasMouseUpAt(position: Point): Boolean = wasMouseAt(position, mouseUpAt)
  def wasMouseUpAt(x: Int, y: Int): Boolean = wasMouseUpAt(Point(x, y))

  def wasMouseDownAt(position: Point): Boolean = wasMouseAt(position, mouseDownAt)
  def wasMouseDownAt(x: Int, y: Int): Boolean = wasMouseDownAt(Point(x, y))

  def wasMousePositionAt(position: Point): Boolean = wasMouseAt(position, mousePositionAt)
  def wasMousePositionAt(x: Int, y: Int): Boolean = wasMousePositionAt(Point(x, y))

  //Within
  private def wasMouseWithin(bounds: Rectangle, maybePosition: Option[Point]): Boolean =
    maybePosition match {
      case Some(pt) => bounds.isPointWithin(pt)
      case None => false
    }

  def wasMouseClickedWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mouseClickAt)
  def wasMouseClickedWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseClickedWithin(Rectangle(x, y, width, height))

  def wasMouseUpWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mouseUpAt)
  def wasMouseUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseUpWithin(Rectangle(x, y, width, height))

  def wasMouseDownWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mouseDownAt)
  def wasMouseDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseDownWithin(Rectangle(x, y, width, height))

  def wasMousePositionWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mousePositionAt)
  def wasMousePositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMousePositionWithin(Rectangle(x, y, width, height))

}

trait FrameKeyboardEvents {

  val events: List[GameEvent]

  val keyboardEvents: List[KeyboardEvent] = events.collect { case e: KeyboardEvent => e }

  val keysUp: List[Int] = keyboardEvents.collect { case k: KeyUp => k.keyCode }
  val keysDown: List[Int] = keyboardEvents.collect { case k: KeyDown => k.keyCode }
  val keysPressed: List[Int] = keyboardEvents.collect { case k: KeyPress => k.keyCode }

  def keysAreDown(keys: Int*): Boolean = keys.forall(keyCode => keysDown.contains(keyCode))
  def keysAreUp(keys: Int*): Boolean = keys.forall(keyCode => keysUp.contains(keyCode))
  def keysWerePressed(keys: Int*): Boolean = keys.forall(keyCode => keysPressed.contains(keyCode))

}