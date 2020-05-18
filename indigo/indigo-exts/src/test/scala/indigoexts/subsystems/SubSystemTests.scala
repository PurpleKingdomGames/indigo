package indigoexts.subsystems

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

  import FakeFrameContext._

  val tests: Tests =
    Tests {
      "A SubSystem (PointsTracker example)" - {

        "should render the initial state correctly" - {
          val expected = PointsTrackerExample(1230).render(context(6)).gameLayer.nodes.head.asInstanceOf[Text].text

          assert(expected == "1230")
        }

        "should respond to an Add event" - {
          val expected =
            PointsTrackerExample(0)
              .update(context(6))(PointsTrackerEvent.Add(10))
              .state
              .render(context(6))
              .gameLayer
              .nodes
              .head
              .asInstanceOf[Text]
              .text

          assert(expected == "10")
        }

        "should respond to a LoseALl event and emit an event" - {
          val expected =
            PointsTrackerExample(1000)
              .update(context(6))(PointsTrackerEvent.LoseAll)
              .state
              .render(context(6))
              .gameLayer
              .nodes
              .head
              .asInstanceOf[Text]
              .text

          assert(expected == "0")
        }

      }
    }

}
