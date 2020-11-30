package indigoextras.ui

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.scenegraph.Graphic
import indigo.shared.input.Mouse
import indigo.shared.datatypes.Point
import indigo.shared.events.MouseEvent
import indigo.shared.events.GlobalEvent

class ButtonTests extends munit.FunSuite {

  val bounds =
    Rectangle(10, 10, 100, 100)

  val button =
    Button(
      ButtonAssets(
        Graphic(bounds, 1, Material.Textured(AssetName("up"))),
        Graphic(bounds, 1, Material.Textured(AssetName("over"))),
        Graphic(bounds, 1, Material.Textured(AssetName("down")))
      ),
      bounds,
      Depth(1)
    ).withHoverOverActions(FakeEvent("mouse over"))
      .withHoverOutActions(FakeEvent("mouse out"))
      .withDownActions(FakeEvent("mouse down"))
      .withUpActions(FakeEvent("mouse up"))

  test("Initial state is up") {
    assertEquals(button.state.isUp, true)
  }

  test("Transition from Up -> Over when mouse over.") {
    assertEquals(button.state.isUp, true)

    val mouse =
      new Mouse(Nil, Point(20, 20), false)

    val actual = button.update(mouse)

    assertEquals(actual.state.state.isOver, true)
  }

  test("Transition from Up -> Over when mouse over.Within the button: On mouse over, the over action is performed") {

    val mouse =
      new Mouse(Nil, Point(20, 20), false)

    val actual = button.update(mouse)
    assert(actual.globalEvents.length == 1)
    assert(actual.globalEvents.contains(FakeEvent("mouse over")))
  }

  test("Transition from Over -> Up when mouse out.") {
    val mouse =
      new Mouse(Nil, Point(0, 0), false)

    val actual = button.toOverState.update(mouse)

    assertEquals(actual.state.state.isUp, true)
  }

  test("Transition from Over -> Up when mouse out.Starting within the button: On mouse out, the out action is performed") {
    val mouse =
      new Mouse(Nil, Point(0, 0), false)
    val actual = button.toOverState.update(mouse)

    assert(actual.globalEvents.length == 1)
    assert(actual.globalEvents.contains(FakeEvent("mouse out")))
  }

  test("Transition from Over -> Down on mouse press.") {
    val mouse =
      new Mouse(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false)

    val actual = button.toOverState.update(mouse)

    assertEquals(actual.state.state.isDown, true)
  }

  test("Transition from Over -> Down on mouse press.Within the button: On mouse down, the down action is performed") {

    val mouse =
      new Mouse(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false)

    val actual = button.toOverState.update(mouse)

    assert(actual.globalEvents.length == 1)
    assert(actual.globalEvents.contains(FakeEvent("mouse down")))
  }

  test("Transition from Up -> Down on mouse press.") {
    val mouse =
      new Mouse(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false)

    val actual = button.toUpState.update(mouse)

    assertEquals(actual.state.state.isDown, true)
  }

  test("Transition from Up -> Down on mouse press.Within the button: On mouse down, the down action is performed") {
    val mouse =
      new Mouse(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false)

    val actual = button.toUpState.update(mouse)

    assert(actual.globalEvents.length == 2)
    assert(actual.globalEvents.contains(FakeEvent("mouse over")))
    assert(actual.globalEvents.contains(FakeEvent("mouse down")))
    assert(actual.globalEvents == List(FakeEvent("mouse over"), FakeEvent("mouse down")))

  }

  test("Transition from Down -> Over on mouse release.") {
    val mouse =
      new Mouse(List(MouseEvent.MouseUp(20, 20)), Point(20, 20), false)

    val actual = button.toDownState.update(mouse)

    assertEquals(actual.state.state.isOver, true)

  }
  test("Transition from Down -> Over on mouse release.Within the button: On mouse release, the up action is performed") {
    val mouse =
      new Mouse(List(MouseEvent.MouseUp(20, 20)), Point(20, 20), false)

    val actual = button.toDownState.update(mouse)

    assert(actual.globalEvents.length == 1)
    assert(actual.globalEvents.contains(FakeEvent("mouse up")))
  }

  test("If the button is down, and the mouse moves out, the button stays down until release.") {
    val actual = for {
      buttonPressed <- button.update(new Mouse(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false))
      mouseOut      <- buttonPressed.update(new Mouse(Nil, Point(200, 200), false))
      mouseReleased <- mouseOut.update(new Mouse(List(MouseEvent.MouseUp(200, 200)), Point(200, 200), false))
    } yield (buttonPressed.state, mouseOut.state, mouseReleased.state)

    assert(actual.state._1.isDown)
    assert(actual.state._2.isDown)
    assert(actual.state._3.isUp)
  }

  test(
    "If the button is down, and the mouse moves out, the button stays down until release.If the mouse is moved onto and pressed down on the button, dragged out and released, only the down action is performed."
  ) {

    val actual = for {
      buttonPressed <- button.update(new Mouse(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false))
      mouseOut      <- buttonPressed.update(new Mouse(Nil, Point(200, 200), false))
      mouseReleased <- mouseOut.update(new Mouse(List(MouseEvent.MouseUp(200, 200)), Point(200, 200), false))
    } yield (buttonPressed.state, mouseOut.state, mouseReleased.state)

    assert(actual.globalEvents.length == 2)
    assert(actual.globalEvents.contains(FakeEvent("mouse over")))
    assert(actual.globalEvents.contains(FakeEvent("mouse down")))
    assert(actual.globalEvents == List(FakeEvent("mouse over"), FakeEvent("mouse down")))
  }

}

final case class FakeEvent(message: String) extends GlobalEvent
