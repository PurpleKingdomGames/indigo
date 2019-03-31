package indigo.gameengine.subsystems

import indigo.gameengine.GameTime
import indigo.gameengine.scenegraph.Text
import indigo.dice.Dice

import org.scalatest.FunSpec

class SubSystemSpec extends FunSpec {

val dice: Dice =
  Dice.loaded(6)

  describe("A SubSystem (PointsTracker example)") {

    it("should render the initial state correctly") {
      val expected = PointsTrackerExample(1230).render(GameTime.zero).gameLayer.head.asInstanceOf[Text].text

      assert(expected == "1230")
    }

    it("should respond to an Add event") {
      val expected =
        PointsTrackerExample(0)
          .update(GameTime.zero, dice)(PointsTrackerEvent.Add(10))
          .state
          .render(GameTime.zero)
          .gameLayer
          .head
          .asInstanceOf[Text]
          .text

      assert(expected == "10")
    }

    it("should respond to a LoseALl event and emit an event") {
      val expected =
        PointsTrackerExample(1000)
          .update(GameTime.zero, dice)(PointsTrackerEvent.LoseAll)
          .state
          .render(GameTime.zero)
          .gameLayer
          .head
          .asInstanceOf[Text]
          .text

      assert(expected == "0")
    }

  }

}
