package com.purplekingdomgames.indigo.gameengine

/**
  * This has probably been over thought, but the idea is to have two parallel collections in an attempt to ensure messages are never lost,
  * While ensuring that the data doesn't grow infinitely. Messages are collected in one queue and just before the queue is collected the other
  * queue is activated so that all new events are placed onto that queue instead.
  */
object GlobalEventStream {

  private var useBlue: Boolean = true
  private var blueEvents: List[GameEvent] = Nil
  private var greenEvents: List[GameEvent] = Nil

  def push(e: GameEvent): Unit = {
    if(useBlue) {
      blueEvents = blueEvents ++ List(e)
    } else {
      greenEvents = greenEvents ++ List(e)
    }
  }

  def collect: List[GameEvent] = {
    if(useBlue) {
      useBlue = !useBlue
      val l = blueEvents
      blueEvents = Nil
      l
    } else {
      useBlue = !useBlue
      val l = greenEvents
      greenEvents = Nil
      l
    }
  }

}

sealed trait GameEvent

case object FrameTick extends GameEvent

case class MouseClick(x: Int, y: Int) extends GameEvent
case class MouseUp(x: Int, y: Int) extends GameEvent
case class MouseDown(x: Int, y: Int) extends GameEvent
case class MousePosition(x: Int, y: Int) extends GameEvent

case class KeyUp(keyCode: Int) extends GameEvent
case class KeyDown(keyCode: Int) extends GameEvent

object Keys {

  val LeftArrow = 37
  val UpArrow = 38
  val RightArrow = 39
  val DownArrow = 40

}