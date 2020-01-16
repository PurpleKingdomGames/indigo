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

    }

}
