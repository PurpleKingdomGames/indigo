package indigoextras.ui

import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.events.MouseEvent
import indigo.shared.input.Mouse
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Graphic

class HitAreaTests extends munit.FunSuite {
  val bounds        = Rectangle(10, 10, 100, 100)
  val holdDownEvent = FakeEvent("mouse held down")
  val hitArea       = HitArea(bounds).withHoldDownActions(holdDownEvent)

  test("If the hit area is down and we keep the mouse pressed, hold down actions are performed.") {
    val mouse = new Mouse(Batch.empty, bounds.position, true)
    val actual = for {
      holdDown       <- hitArea.toDownState.update(mouse)
      holdDownLonger <- holdDown.update(mouse)
    } yield (holdDown, holdDownLonger)

    assert(actual.unsafeGlobalEvents.length == 2)
    assert(actual.unsafeGlobalEvents(0) == holdDownEvent)
    assert(actual.unsafeGlobalEvents(1) == holdDownEvent)
  }

  test("If the hit area is down and we release the mouse, the state is set to over.") {
    val mouse  = new Mouse(Batch.empty, bounds.position, false)
    val actual = hitArea.toDownState.update(mouse).unsafeGet
    assert(actual.state == ButtonState.Over)
  }

  test("If the hit area is hovered and we release the mouse, the state is set to down.") {
    val mouse  = new Mouse(Batch(MouseEvent.MouseDown(bounds.x, bounds.y)), bounds.position, true)
    val actual = hitArea.toOverState.update(mouse).unsafeGet
    assert(actual.state == ButtonState.Down)
  }
}
