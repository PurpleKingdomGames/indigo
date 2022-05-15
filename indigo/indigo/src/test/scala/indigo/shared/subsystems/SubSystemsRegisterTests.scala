package indigo.shared.subsystems

import indigo.shared.collections.Batch
import indigo.shared.scenegraph.Text

// @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
class SubSystemsRegisterTests extends munit.FunSuite {

  import FakeSubSystemFrameContext._

  test("The sub system register.should allow you to add sub systems") {
    val r = new SubSystemsRegister()
    r.register(Batch(PointsTrackerExample(1, 0), PointsTrackerExample(2, 0)))

    assertEquals(r.size, 2)
  }

  test("The sub system register.should allow you to update sub systems") {
    val r = new SubSystemsRegister()
    r.register(Batch(PointsTrackerExample(1, 10), PointsTrackerExample(2, 50)))

    val data = r
      .update(context(6), Batch(PointsTrackerEvent.Add(10)).toJSArray)
      .unsafeGet
      .stateMap

    val actual = data.toList.map(_._2.asInstanceOf[Int])

    assertEquals(actual.length, 2)
    assertEquals(actual.contains(20), true)
    assertEquals(actual.contains(60), true)
  }

  test("The sub system register.should allow you to update sub systems and emit events") {
    val r = new SubSystemsRegister()
    r.register(Batch(PointsTrackerExample(1, 10), PointsTrackerExample(2, 50)))

    val updated = r.update(context(6), Batch(PointsTrackerEvent.LoseAll).toJSArray)

    val actual = updated.unsafeGet.stateMap.toList.map(_._2.asInstanceOf[Int])

    assertEquals(actual.length, 2)
    assertEquals(actual.forall(_ == 0), true)

    assert(updated.unsafeGlobalEvents == Batch(GameOver, GameOver))
  }

  test("The sub system register.should allow you to render sub systems") {
    val r = new SubSystemsRegister()
    r.register(Batch(PointsTrackerExample(1, 10), PointsTrackerExample(2, 50)))

    val rendered =
      r.update(context(6), Batch(PointsTrackerEvent.Add(10)).toJSArray)
        .unsafeGet
        .present(context(6))
        .unsafeGet
        .layers
        .flatMap(_.nodes)
        .map(_.asInstanceOf[Text[_]].text)

    assert(rendered.contains("20"))
    assert(rendered.contains("60"))
  }

}
