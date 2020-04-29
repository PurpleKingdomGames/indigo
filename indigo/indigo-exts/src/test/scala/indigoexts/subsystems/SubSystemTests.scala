package indigoexts.subsystems

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.Text
import indigo.shared.dice.Dice

import utest._
import indigo.shared.events.InputState

object SubSystemTests extends TestSuite {

  val dice: Dice =
    Dice.loaded(6)

  val tests: Tests =
    Tests {
      "A SubSystem (PointsTracker example)" - {

        "should render the initial state correctly" - {
          val expected = PointsTrackerExample(1230).render(GameTime.zero).gameLayer.nodes.head.asInstanceOf[Text].text

          assert(expected == "1230")
        }

        "should respond to an Add event" - {
          val expected =
            PointsTrackerExample(0)
              .update(GameTime.zero, InputState.default, dice)(PointsTrackerEvent.Add(10))
              .state
              .render(GameTime.zero)
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
              .update(GameTime.zero, InputState.default, dice)(PointsTrackerEvent.LoseAll)
              .state
              .render(GameTime.zero)
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
