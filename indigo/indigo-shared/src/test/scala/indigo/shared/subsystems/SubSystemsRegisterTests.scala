package indigo.shared.subsystems

import indigo.shared.scenegraph.Text

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
class SubSystemsRegisterTests extends munit.FunSuite {

  import FakeSubSystemFrameContext._

  test("The sub system register.should allow you to add sub systems") {
    val r = new SubSystemsRegister()
    r.register(List(PointsTrackerExample(0), PointsTrackerExample(0)))

    assertEquals(r.size, 2)
  }

  test("The sub system register.should allow you to update sub systems") {
    val r = new SubSystemsRegister()
    r.register(List(PointsTrackerExample(10), PointsTrackerExample(50)))

    val data = r
      .update(context(6), List(PointsTrackerEvent.Add(10)))
      .unsafeGet
      .stateMap

    val actual = data.toList.map(_._2.asInstanceOf[Int])

    assertEquals(actual.length, 2)
    assertEquals(actual.contains(20), true)
    assertEquals(actual.contains(60), true)
  }

  test("The sub system register.should allow you to update sub systems and emit events") {
    val r = new SubSystemsRegister()
    r.register(List(PointsTrackerExample(10), PointsTrackerExample(50)))

    val updated = r.update(context(6), List(PointsTrackerEvent.LoseAll))

    val actual = updated.unsafeGet.stateMap.toList.map(_._2.asInstanceOf[Int])

    assertEquals(actual.length, 2)
    assertEquals(actual.forall(_ == 0), true)

    assert(updated.unsafeGlobalEvents == List(GameOver, GameOver))
  }

  test("The sub system register.should allow you to render sub systems") {
    val r = new SubSystemsRegister()
    r.register(List(PointsTrackerExample(10), PointsTrackerExample(50)))

    val rendered =
      r.update(context(6), List(PointsTrackerEvent.Add(10)))
        .unsafeGet
        .present(context(6))
        .unsafeGet
        .layers.flatMap(_.nodes)
        .map(_.asInstanceOf[Text].text)

    assert(rendered.contains("20"))
    assert(rendered.contains("60"))
  }

}
