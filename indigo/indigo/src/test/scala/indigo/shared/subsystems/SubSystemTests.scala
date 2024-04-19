package indigo.shared.subsystems

import indigo.shared.scenegraph.Text

// @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
class SubSystemTests extends munit.FunSuite {

  import FakeSubSystemFrameContext._

  val subSystem = PointsTrackerExample(1, 0)

  test("A SubSystem (PointsTracker example).should render the initial state correctly") {
    val expected = subSystem
      .present(context(6).copy(reference = 10), 1230)
      .unsafeGet
      .layers
      .flatMap(_.toBatch)
      .head
      .nodes
      .head
      .asInstanceOf[Text[?]]
      .text

    assert(expected == "1230")
  }

  test("A SubSystem (PointsTracker example).should respond to an Add event") {
    val expected = {
      val points = subSystem
        .update(context(6).copy(reference = 10), 0)(PointsTrackerEvent.Add(10))

      subSystem
        .present(context(6).copy(reference = 10), points.unsafeGet)
        .unsafeGet
        .layers
        .flatMap(_.toBatch)
        .head
        .nodes
        .head
        .asInstanceOf[Text[?]]
        .text
    }

    assertEquals(expected, "20") // 10 + reference data of 10
  }

  test("A SubSystem (PointsTracker example).should respond to a LoseAll event and emit an event") {
    val expected = {
      val points = subSystem
        .update(context(6).copy(reference = 10), 1000)(PointsTrackerEvent.LoseAll)

      subSystem
        .present(context(6).copy(reference = 10), points.unsafeGet)
        .unsafeGet
        .layers
        .flatMap(_.toBatch)
        .head
        .nodes
        .head
        .asInstanceOf[Text[?]]
        .text
    }

    assertEquals(expected, "0")
  }

}
