package indigo.gameengine.subsystems

import indigo.gameengine.GameTime
import indigo.gameengine.scenegraph.Text
import org.scalatest.FunSpec

class SubSystemSpec extends FunSpec {

  describe("A SubSystem (PointsTracker example)") {

    it("should render the initial state correctly") {
      assert(PointsTrackerExample(1230).render(GameTime.zero(10)).gameLayer.head.asInstanceOf[Text].text == "1230")
    }

    it("should respond to an Add event") {
      assert(
        PointsTrackerExample(0)
          .update(GameTime.zero(10))(PointsTrackerEvent.Add(10))
          .model
          .render(GameTime.zero(10))
          .gameLayer
          .head
          .asInstanceOf[Text]
          .text == "10"
      )
    }

    it("should respond to a LoseALl event and emit an event") {
      assert(
        PointsTrackerExample(1000)
          .update(GameTime.zero(10))(PointsTrackerEvent.LoseAll)
          .model
          .render(GameTime.zero(10))
          .gameLayer
          .head
          .asInstanceOf[Text]
          .text == "0"
      )
    }

  }

}
