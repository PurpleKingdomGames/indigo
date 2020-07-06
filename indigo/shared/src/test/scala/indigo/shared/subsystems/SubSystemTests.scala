package indigo.shared.subsystems

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.Text
import indigo.shared.dice.Dice

import utest._
import indigo.shared.events.InputState
import indigo.shared.FrameContext
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister

object SubSystemTests extends TestSuite {

  import FakeSubSystemFrameContext._

  val subSystem = PointsTrackerExample(0)

  val tests: Tests =
    Tests {
      "A SubSystem (PointsTracker example)" - {

        "should render the initial state correctly" - {
          val expected = subSystem.present(context(6), 1230).gameLayer.nodes.head.asInstanceOf[Text].text

          assert(expected == "1230")
        }

        "should respond to an Add event" - {
          val expected = {
            val points = subSystem
              .update(context(6), 0)(PointsTrackerEvent.Add(10))

            subSystem
              .present(context(6), points.state)
              .gameLayer
              .nodes
              .head
              .asInstanceOf[Text]
              .text
          }

          assert(expected == "10")
        }

        "should respond to a LoseAll event and emit an event" - {
          val expected = {
            val points = subSystem
              .update(context(6), 1000)(PointsTrackerEvent.LoseAll)

            subSystem
              .present(context(6), points.state)
              .gameLayer
              .nodes
              .head
              .asInstanceOf[Text]
              .text
          }

          assert(expected == "0")
        }

      }
    }

}
