package indigoexts.subsystems

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.Text
import indigo.shared.dice.Dice

import utest._

object SubSystemsRegisterTests extends TestSuite {

  val dice: Dice =
    Dice.loaded(6)

  val tests: Tests =
    Tests {
      "The sub system register" - {

        "should allow you to add sub systems" - {
          val r = SubSystemsRegister.empty.add(PointsTrackerExample(10), PointsTrackerExample(20))

          r.size ==> 2
        }

        "should allow you to update sub systems" - {
          val r = SubSystemsRegister.empty.add(PointsTrackerExample(10), PointsTrackerExample(50))

          val reports = r.update(GameTime.zero, dice)(PointsTrackerEvent.Add(10)).state.reports

          assert(reports.contains("Points: 20"))
          assert(reports.contains("Points: 60"))
        }

        "should allow you to update sub systems and emit events" - {
          val r = SubSystemsRegister.empty.add(PointsTrackerExample(10), PointsTrackerExample(50))

          val updated = r.update(GameTime.zero, dice)(PointsTrackerEvent.LoseAll)

          assert(updated.state.reports == List("Points: 0", "Points: 0"))
          assert(updated.globalEvents == List(GameOver, GameOver))
        }

        "should allow you to render sub systems" - {
          val r = SubSystemsRegister.empty.add(PointsTrackerExample(10), PointsTrackerExample(50))

          val rendered =
            r.update(GameTime.zero, dice)(PointsTrackerEvent.Add(10))
              .state
              .render(GameTime.zero)
              .gameLayer
              .map(_.asInstanceOf[Text].text)

          assert(rendered.contains("20"))
          assert(rendered.contains("60"))
        }

      }
    }

}
