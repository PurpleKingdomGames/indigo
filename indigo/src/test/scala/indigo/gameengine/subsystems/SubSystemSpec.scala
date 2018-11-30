package indigo.gameengine.subsystems

import indigo.gameengine.GameTime
import indigo.gameengine.scenegraph.Text
import org.scalatest.FunSpec

class SubSystemSpec extends FunSpec {

  describe("A SubSystem (PointsTracker example)") {

    it("should render the initial state correctly") {
      val expected = PointsTrackerExample(1230).render(GameTime.zero(10)).gameLayer.head.asInstanceOf[Text].text

      assert(expected == "1230")
    }

    it("should respond to an Add event") {
      val expected =
        PointsTrackerExample(0)
          .update(GameTime.zero(10))(PointsTrackerEvent.Add(10))
          .subSystem
          .render(GameTime.zero(10))
          .gameLayer
          .head
          .asInstanceOf[Text]
          .text

      assert(expected == "10")
    }

    it("should respond to a LoseALl event and emit an event") {
      val expected =
        PointsTrackerExample(1000)
          .update(GameTime.zero(10))(PointsTrackerEvent.LoseAll)
          .subSystem
          .render(GameTime.zero(10))
          .gameLayer
          .head
          .asInstanceOf[Text]
          .text

      assert(expected == "0")
    }

  }

}
