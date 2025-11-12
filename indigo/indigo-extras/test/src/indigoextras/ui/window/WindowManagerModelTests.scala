package indigoextras.ui.window

import indigo.*
import indigo.syntax.*
import indigoextras.ui.*

class WindowManagerModelTests extends munit.FunSuite {

  def unfocusedWindow(id: String): Window[Unit, Unit] = Window(
    WindowId(id),
    Size.zero,
    Dimensions.zero,
    ()
  )

  def windowBatch(len: Int): Batch[Window[Unit, Unit]] =
    (1 to len).map(i => unfocusedWindow(s"test-window-$i")).toBatch

  test("should return None when there are no windows") {
    val model  = WindowManagerModel[Unit](Batch.empty)
    val result = model.focused
    assertEquals(result, None)
  }

  test("should return None when there are no focused windows") {
    val model  = WindowManagerModel[Unit](windowBatch(10))
    val result = model.focused
    assertEquals(result, None)
  }

  test("should return the focused window") {
    val window: Window[Unit, Unit] = Window(
      WindowId("test-window-focused"),
      Size.zero,
      Dimensions.zero,
      ()
    ).focus
    val model  = WindowManagerModel[Unit](windowBatch(3) ++ Batch(window) ++ windowBatch(5))
    val result = model.focused
    assertEquals(result, Some(window))
  }
}
