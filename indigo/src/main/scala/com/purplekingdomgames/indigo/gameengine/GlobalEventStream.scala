package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Point, Rectangle}

import scala.collection.mutable

object GlobalEventStream {

  private var eventQueue: mutable.Queue[GameEvent] =
    new mutable.Queue[GameEvent]()

  def push(e: GameEvent): Unit = {
    eventQueue += e
    ()
  }

  def collect: List[GameEvent] = eventQueue.dequeueAll(_ => true).toList

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

case class ViewEvent[EventDataType](eventDataType: EventDataType) extends GameEvent

object Keys {

  val BACKSPACE = 8
  val TAB = 9
  val ENTER = 13
  val SHIFT = 16
  val CTRL = 17
  val ALT = 18
  val PAUSE_BREAK = 19
  val CAPS_LOCK = 20
  val ESCAPE = 27
  val PAGE_UP = 33
  val PAGE_DOWN = 34
  val END = 35
  val HOME = 36
  val LEFT_ARROW = 37
  val UP_ARROW = 38
  val RIGHT_ARROW = 39
  val DOWN_ARROW = 40
  val INSERT = 45
  val DELETE = 46
  val KEY_0 = 48
  val KEY_1 = 49
  val KEY_2 = 50
  val KEY_3 = 51
  val KEY_4 = 52
  val KEY_5 = 53
  val KEY_6 = 54
  val KEY_7 = 55
  val KEY_8 = 56
  val KEY_9 = 57
  val KEY_A = 65
  val KEY_B = 66
  val KEY_C = 67
  val KEY_D = 68
  val KEY_E = 69
  val KEY_F = 70
  val KEY_G = 71
  val KEY_H = 72
  val KEY_I = 73
  val KEY_J = 74
  val KEY_K = 75
  val KEY_L = 76
  val KEY_M = 77
  val KEY_N = 78
  val KEY_O = 79
  val KEY_P = 80
  val KEY_Q = 81
  val KEY_R = 82
  val KEY_S = 83
  val KEY_T = 84
  val KEY_U = 85
  val KEY_V = 86
  val KEY_W = 87
  val KEY_X = 88
  val KEY_Y = 89
  val KEY_Z = 90
  val LEFT_WINDOW_KEY = 91
  val RIGHT_WINDOW_KEY = 92
  val SELECT_KEY = 93
  val NUMPAD_0 = 96
  val NUMPAD_1 = 97
  val NUMPAD_2 = 98
  val NUMPAD_3 = 99
  val NUMPAD_4 = 100
  val NUMPAD_5 = 101
  val NUMPAD_6 = 102
  val NUMPAD_7 = 103
  val NUMPAD_8 = 104
  val NUMPAD_9 = 105
  val MULTIPLY = 106
  val ADD = 107
  val SUBTRACT = 109
  val DECIMAL_POINT = 110
  val DIVIDE = 111
  val F1 = 112
  val F2 = 113
  val F3 = 114
  val F4 = 115
  val F5 = 116
  val F6 = 117
  val F7 = 118
  val F8 = 119
  val F9 = 120
  val F10 = 121
  val F11 = 122
  val F12 = 123
  val NUM_LOCK = 144
  val SCROLL_LOCK = 145
  val SEMI_COLON = 186
  val EQUAL_SIGN = 187
  val COMMA = 188
  val DASH = 189
  val PERIOD = 190
  val FORWARD_SLASH = 191
  val GRAVE_ACCENT = 192
  val OPEN_BRACKET = 219
  val BACK_SLASH = 220
  val CLOSE_BRAKET = 221
  val SINGLE_QUOTE = 222

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

  def keysAreDown(keys: Int*): Boolean = keys.forall(keyCode => keysDown.contains(keyCode))
  def keysAreUp(keys: Int*): Boolean = keys.forall(keyCode => keysUp.contains(keyCode))

}