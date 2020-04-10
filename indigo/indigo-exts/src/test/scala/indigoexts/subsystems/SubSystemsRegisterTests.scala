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

          val subSystemPoints = r
            .update(GameTime.zero, dice)(PointsTrackerEvent.Add(10))
            .state
            .registeredSubSystems
            .toList
            .collect { case PointsTrackerExample(points) => points }

          assert(subSystemPoints.contains(20))
          assert(subSystemPoints.contains(60))
        }

        "should allow you to update sub systems and emit events" - {
          val r = SubSystemsRegister.empty.add(PointsTrackerExample(10), PointsTrackerExample(50))

          val updated = r.update(GameTime.zero, dice)(PointsTrackerEvent.LoseAll)

          val subSystemPoints: List[Int] =
            updated.state.registeredSubSystems.toList
              .collect { case PointsTrackerExample(points) => points }

          assert(subSystemPoints == List(0, 0))
          assert(updated.globalEvents == List(GameOver, GameOver))
        }

        "should allow you to render sub systems" - {
          val r = SubSystemsRegister.empty.add(PointsTrackerExample(10), PointsTrackerExample(50))

          val rendered =
            r.update(GameTime.zero, dice)(PointsTrackerEvent.Add(10))
              .state
              .render(GameTime.zero)
              .gameLayer
              .nodes
              .map(_.asInstanceOf[Text].text)

          assert(rendered.contains("20"))
          assert(rendered.contains("60"))
        }

      }
    }

}
