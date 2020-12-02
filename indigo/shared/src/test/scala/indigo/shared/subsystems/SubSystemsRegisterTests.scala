package indigo.shared.subsystems

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.Text
import indigo.shared.dice.Dice

import indigo.shared.events.InputState
import indigo.shared.FrameContext
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister
import indigo.shared.AnimationsRegister

class SubSystemsRegisterTests extends munit.FunSuite {

  import FakeSubSystemFrameContext._

  test("The sub system register.should allow you to add sub systems") {
    val r = new SubSystemsRegister(List(PointsTrackerExample(0), PointsTrackerExample(0)))

    assertEquals(r.size, 2)
  }

  test("The sub system register.should allow you to update sub systems") {
    val r = new SubSystemsRegister(List(PointsTrackerExample(10), PointsTrackerExample(50)))

    val data = r
      .update(context(6), List(PointsTrackerEvent.Add(10)))
      .state
      .stateMap

    val actual = data.toList.map(_._2.asInstanceOf[Int])

    assertEquals(actual.length, 2)
    assertEquals(actual.contains(20), true)
    assertEquals(actual.contains(60), true)
  }

  test("The sub system register.should allow you to update sub systems and emit events") {
    val r = new SubSystemsRegister(List(PointsTrackerExample(10), PointsTrackerExample(50)))

    val updated = r.update(context(6), List(PointsTrackerEvent.LoseAll))

    val actual = updated.state.stateMap.toList.map(_._2.asInstanceOf[Int])

    assertEquals(actual.length, 2)
    assertEquals(actual.forall(_ == 0), true)

    assert(updated.globalEvents == List(GameOver, GameOver))
  }

  test("The sub system register.should allow you to render sub systems") {
    val r = new SubSystemsRegister(List(PointsTrackerExample(10), PointsTrackerExample(50)))

    val rendered =
      r.update(context(6), List(PointsTrackerEvent.Add(10)))
        .state
        .present(context(6))
        .gameLayer
        .nodes
        .map(_.asInstanceOf[Text].text)

    assert(rendered.contains("20"))
    assert(rendered.contains("60"))
  }

}
