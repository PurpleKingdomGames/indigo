package indigo.shared.events

import utest._
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point

object InputSignalsTests extends TestSuite {

  val tests: Tests =
    Tests {

      val bounds: Rectangle =
        Rectangle(10, 10, 100, 100)

      val signals: InputSignals =
        InputSignals.default

      "The default signals object does the expected thing" - {
        signals.mouse.leftMouseIsDown ==> false
        signals.mouse.position ==> Point.zero

        signals.mouse.wasMouseClickedWithin(bounds) ==> false
      }

      "Mouse signals" - {

        "position" - { 1 ==> 2 }
        "leftMouseIsDown" - { 1 ==> 2 }
        "mousePressed" - { 1 ==> 2 }
        "mouseReleased" - { 1 ==> 2 }
        "mouseClicked" - { 1 ==> 2 }
        "mouseClickAt" - { 1 ==> 2 }
        "mouseUpAt" - { 1 ==> 2 }
        "mouseDownAt" - { 1 ==> 2 }
        "wasMouseClickedAt" - { 1 ==> 2 }
        "wasMouseUpAt" - { 1 ==> 2 }
        "wasMouseDownAt" - { 1 ==> 2 }
        "wasMousePositionAt" - { 1 ==> 2 }
        "wasMouseClickedWithin" - { 1 ==> 2 }
        "wasMouseUpWithin" - { 1 ==> 2 }
        "wasMouseDownWithin" - { 1 ==> 2 }
        "wasMousePositionWithin" - { 1 ==> 2 }

      }

      "Keyboard Signals" - {

        "keys up" - { 1 ==> 2 }
        "keys down" - { 1 ==> 2 }
        "keys are down" - { 1 ==> 2 }
        "keys are up" - { 1 ==> 2 }
        "last key held down" - { 1 ==> 2 }

      }

    }

}
