package indigoextras.ui

import indigo.MouseButton
import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.events.MouseEvent
import indigo.shared.events.PointerEvent.Down
import indigo.shared.events.PointerEvent.PointerId
import indigo.shared.events.PointerEvent.PointerUp
import indigo.shared.events.PointerType
import indigo.shared.input.Pointer
import indigo.shared.input.PointerState
import indigo.shared.input.Pointers
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Graphic

class ButtonTests extends munit.FunSuite {

  val bounds =
    Rectangle(10, 10, 100, 100)

  val holdDownEvent = FakeEvent("mouse held down")

  val button =
    Button(
      ButtonAssets(
        Graphic(bounds, 1, Material.Bitmap(AssetName("up"))),
        Graphic(bounds, 1, Material.Bitmap(AssetName("over"))),
        Graphic(bounds, 1, Material.Bitmap(AssetName("down")))
      ),
      bounds,
      Depth.zero
    ).withHoverOverActions(FakeEvent("mouse over"))
      .withHoverOutActions(FakeEvent("mouse out"))
      .withDownActions(FakeEvent("mouse down"))
      .withUpActions(FakeEvent("mouse up"))
      .withHoldDownActions(holdDownEvent)

  test("Initial state is up") {
    assertEquals(button.state.isUp, true)
  }

  test("Transition from Up -> Over when mouse over.") {
    assertEquals(button.state.isUp, true)
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch.empty
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = button.update(mouse)

    assertEquals(actual.unsafeGet.state.isOver, true)
  }

  test("Transition from Up -> Over when mouse over.Within the button: On mouse over, the over action is performed") {

    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch.empty
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = button.update(mouse)
    assert(actual.unsafeGlobalEvents.length == 1)
    assert(actual.unsafeGlobalEvents.contains(FakeEvent("mouse over")))
  }

  test("Transition from Over -> Up when mouse out.") {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(0, 0))),
        Batch.empty
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = button.toOverState.update(mouse)

    assertEquals(actual.unsafeGet.state.isUp, true)
  }

  test(
    "Transition from Over -> Up when mouse out.Starting within the button: On mouse out, the out action is performed"
  ) {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(0, 0))),
        Batch.empty
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val actual = button.toOverState.update(mouse)

    assert(actual.unsafeGlobalEvents.length == 1)
    assert(actual.unsafeGlobalEvents.contains(FakeEvent("mouse out")))
  }

  test("Transition from Over -> Down on mouse press.") {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch(Down(20, 20, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = button.toOverState.update(mouse)

    assertEquals(actual.unsafeGet.state.isDown, true)
  }

  test("Transition from Over -> Down on mouse press.Within the button: On mouse down, the down action is performed") {

    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch(Down(20, 20, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = button.toOverState.update(mouse)

    assert(actual.unsafeGlobalEvents.length == 1)
    assert(actual.unsafeGlobalEvents.contains(FakeEvent("mouse down")))
  }

  test("Transition from Up -> Down on mouse press.") {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch(Down(20, 20, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val actual = button.toUpState.update(mouse)

    assertEquals(actual.unsafeGet.state.isDown, true)
  }

  test("Transition from Up -> Down on mouse press.Within the button: On mouse down, the down action is performed") {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch(Down(20, 20, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = button.toUpState.update(mouse)

    assert(actual.unsafeGlobalEvents.length == 2)
    assert(actual.unsafeGlobalEvents.contains(FakeEvent("mouse over")))
    assert(actual.unsafeGlobalEvents.contains(FakeEvent("mouse down")))
    assert(actual.unsafeGlobalEvents == Batch(FakeEvent("mouse over"), FakeEvent("mouse down")))

  }

  test("Transition from Down -> Over on mouse release.") {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch(PointerUp(20, 20, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = button.toDownState.update(mouse)

    assertEquals(actual.unsafeGet.state.isOver, true)

  }
  test(
    "Transition from Down -> Over on mouse release.Within the button: On mouse release, the up action is performed"
  ) {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch(PointerUp(20, 20, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = button.toDownState.update(mouse)

    assert(clue(actual.unsafeGlobalEvents.length) == clue(2))
    assert(clue(actual.unsafeGlobalEvents.contains(FakeEvent("mouse over"))))
    assert(clue(actual.unsafeGlobalEvents.contains(FakeEvent("mouse up"))))
  }

  test("If the button is down, and the mouse moves out, the button stays down until release.") {
    val mouse1 = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch(Down(20, 20, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val mouse2 = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch(MouseButton.LeftMouseButton), Point(200, 200))),
        Batch.empty
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val mouse3 = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(200, 200))),
        Batch(PointerUp(200, 200, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val actual = for {
      buttonPressed <- button.update(mouse1)
      mouseOut      <- buttonPressed.update(mouse2)
      mouseReleased <- mouseOut.update(mouse3)
    } yield (buttonPressed.state, mouseOut.state, mouseReleased.state)

    assert(clue(actual.unsafeGet._1.isDown))
    assert(clue(actual.unsafeGet._2.isDown))
    assert(clue(actual.unsafeGet._3.isUp))
  }

  test(
    "If the button is down, and the mouse moves out, the button stays down until release.If the mouse is moved onto and pressed down on the button, dragged out and released, only the down action is performed."
  ) {
    val mouse1 = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(20, 20))),
        Batch(Down(20, 20, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val mouse2 = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(200, 200))),
        Batch.empty
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val mouse3 = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, Point(200, 200))),
        Batch(PointerUp(200, 200, PointerType.Mouse))
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val actual = for {
      buttonPressed <- button.update(mouse1)
      mouseOut      <- buttonPressed.update(mouse2)
      mouseReleased <- mouseOut.update(mouse3)
    } yield (buttonPressed.state, mouseOut.state, mouseReleased.state)

    assert(clue(actual.unsafeGlobalEvents.length) == clue(3))
    assert(clue(actual.unsafeGlobalEvents.contains(FakeEvent("mouse over"))))
    assert(clue(actual.unsafeGlobalEvents.contains(FakeEvent("mouse down"))))
    assert(clue(actual.unsafeGlobalEvents.contains(FakeEvent("mouse out"))))
  }

  test("If the button is down and we keep the mouse pressed, hold down actions are performed.") {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch(MouseButton.LeftMouseButton), button.bounds.position)),
        Batch.empty
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val actual = for {
      holdDown       <- button.toDownState.update(mouse)
      holdDownLonger <- holdDown.update(mouse)
    } yield (holdDown, holdDownLonger)

    assert(actual.unsafeGlobalEvents.length == 2)
    assert(actual.unsafeGlobalEvents(0) == holdDownEvent)
    assert(actual.unsafeGlobalEvents(1) == holdDownEvent)
  }
}
