package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point

object GlobalSignals {

  def MousePosition: Point = GlobalSignalsManager.MousePosition

}

private[indigo] object GlobalSignalsManager {

  private var signals: Signals = Signals.default

  def update(events: List[GameEvent], magnification: Int): Signals = {
    signals = events.foldLeft(signals) { (sigs, e) =>
      e match {
        case mp: MousePosition =>
          sigs.copy(mousePosition = mp.position / magnification)

        case _ =>
          sigs
      }
    }

    signals
  }

  def MousePosition: Point = signals.mousePosition

}

private[indigo] case class Signals(mousePosition: Point)
private[indigo] object Signals {
  val default: Signals = Signals(
    mousePosition = Point.zero
  )
}