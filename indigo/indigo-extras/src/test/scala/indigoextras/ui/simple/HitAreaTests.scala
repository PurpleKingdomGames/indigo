package indigoextras.ui.simple

import indigo.MouseButton
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.PointerEvent.Down
import indigo.shared.events.PointerEvent.PointerId
import indigo.shared.events.PointerType
import indigo.shared.input.Pointer
import indigo.shared.input.PointerState
import indigo.shared.input.Pointers

class HitAreaTests extends munit.FunSuite {
  val bounds        = Rectangle(10, 10, 100, 100)
  val holdDownEvent = FakeEvent("mouse held down")
  val hitArea       = HitArea(bounds).withHoldDownActions(holdDownEvent)

  test("If the hit area is down and we keep the mouse pressed, hold down actions are performed.") {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch(MouseButton.LeftMouseButton), bounds.position)),
        Batch.empty
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = for {
      holdDown       <- hitArea.toDownState.update(mouse)
      holdDownLonger <- holdDown.update(mouse)
    } yield (holdDown, holdDownLonger)

    assert(actual.unsafeGlobalEvents.length == 2)
    assert(actual.unsafeGlobalEvents(0) == holdDownEvent)
    assert(actual.unsafeGlobalEvents(1) == holdDownEvent)
  }

  test("If the hit area is down and we release the mouse, the state is set to over.") {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch.empty, bounds.position)),
        Batch.empty
      )
      val pointerType = Some(PointerType.Mouse)
    }

    val actual = hitArea.toDownState.update(mouse).unsafeGet
    assert(actual.state == ButtonState.Over)
  }

  test("If the hit area is hovered and we release the mouse, the state is set to down.") {
    val mouse = new PointerState {
      val pointers = new Pointers(
        Batch(Pointer(PointerId(1), PointerType.Mouse, Batch(MouseButton.LeftMouseButton), bounds.position)),
        Batch(
          Down(bounds.position, MouseButton.LeftMouseButton, PointerType.Mouse)
        )
      )
      val pointerType = Some(PointerType.Mouse)
    }
    val actual = hitArea.toOverState.update(mouse).unsafeGet
    assert(actual.state == ButtonState.Down)
  }
}
