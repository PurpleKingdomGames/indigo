package com.purplekingdomgames.indigo.gameengine.events

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
