package indigoextras.ui.datatypes

import indigo.*

class UIContextTests extends munit.FunSuite {

  test("UIContext should correctly calculate the pointer coords") {
    val uiContext =
      UIContext(Context.initial, 1)
        .withPointerCoords(Coords(10, 20))

    assertEquals(uiContext.pointerCoords, Coords(10, 20))
    assertEquals(uiContext.withSnapGrid(Size(2)).pointerCoords, Coords(5, 10))
    assertEquals(uiContext.withSnapGrid(Size(10)).pointerCoords, Coords(1, 2))

    val ctxA =
      UIContext(SubSystemContext.fromContext(Context.initial), Size(10), 2)
        .withPointerCoords(Coords(10, 20))
    val ctxB =
      UIContext(Context.initial, 1)
        .withSnapGrid(Size(10))
        .withPointerCoords(Coords(10, 20))

    assertEquals(ctxA.pointerCoords, ctxB.pointerCoords)
  }

}
