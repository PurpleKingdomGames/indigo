package indigo.shared.subsystems

import indigo.shared.collections.Batch
import indigo.shared.scenegraph.Text

import scala.annotation.nowarn

// @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
@nowarn("msg=unused")
class SubSystemsRegisterTests extends munit.FunSuite {

  import FakeSubSystemFrameContext._

  test("The sub system register.should allow you to add sub systems") {
    val r = new SubSystemsRegister[Int]()
    r.register(Batch(PointsTrackerExample(1, 0), PointsTrackerExample(2, 0)))

    assertEquals(r.size, 2)
  }

  test("The sub system register.should allow you to update sub systems") {
    val r = new SubSystemsRegister[Int]()
    r.register(Batch(PointsTrackerExample(1, 10), PointsTrackerExample(2, 50)))

    val data = r
      .update(context(6), 10, Batch(PointsTrackerEvent.Add(10)).toJSArray)
      .unsafeGet
      .stateMap

    val actual = data.toList.map(_._2.asInstanceOf[Int])

    assertEquals(actual.length, 2)
    assertEquals(actual.contains(30), true) // 20 + ref data of 10
    assertEquals(actual.contains(70), true) // 60 + ref data of 10
  }

  test("The sub system register.should allow you to update sub systems and emit events") {
    val r = new SubSystemsRegister[Int]()
    r.register(Batch(PointsTrackerExample(1, 10), PointsTrackerExample(2, 50)))

    val updated = r.update(context(6), 10, Batch(PointsTrackerEvent.LoseAll).toJSArray)

    val actual = updated.unsafeGet.stateMap.toList.map(_._2.asInstanceOf[Int])

    assertEquals(actual.length, 2)
    assertEquals(actual.forall(_ == 0), true)

    assert(updated.unsafeGlobalEvents == Batch(GameOver, GameOver))
  }

  test("The sub system register.should allow you to render sub systems") {
    val r = new SubSystemsRegister[Int]()
    r.register(Batch(PointsTrackerExample(1, 10), PointsTrackerExample(2, 50)))

    val rendered =
      r.update(context(6), 0, Batch(PointsTrackerEvent.Add(10)).toJSArray)
        .unsafeGet
        .present(context(6), 0)
        .unsafeGet
        .layers
        .flatMap(_.toBatch)
        .flatMap(_.nodes)
        .map(_.asInstanceOf[Text[?]].text)

    assert(rendered.contains("20"))
    assert(rendered.contains("60"))
  }

}
