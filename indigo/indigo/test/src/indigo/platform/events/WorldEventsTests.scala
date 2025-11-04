package indigo.platform.events

import indigo.Batch
import indigo.MouseButton

class WorldEventsTests extends munit.FunSuite {

  test("calculate which buttons have been pressed based on a dom.MouseEvent.buttons Int") {
    import MouseButton.*

    assertEquals(WorldEvents.buttonsFromInt(0), Batch())
    assertEquals(WorldEvents.buttonsFromInt(1), Batch(LeftMouseButton))
    assertEquals(WorldEvents.buttonsFromInt(2), Batch(MiddleMouseButton))
    assertEquals(WorldEvents.buttonsFromInt(3), Batch(LeftMouseButton, MiddleMouseButton))
    assertEquals(WorldEvents.buttonsFromInt(4), Batch(RightMouseButton))
    assertEquals(WorldEvents.buttonsFromInt(5), Batch(LeftMouseButton, RightMouseButton))
    assertEquals(WorldEvents.buttonsFromInt(6), Batch(MiddleMouseButton, RightMouseButton))
    assertEquals(WorldEvents.buttonsFromInt(7), Batch(LeftMouseButton, MiddleMouseButton, RightMouseButton))
    assertEquals(WorldEvents.buttonsFromInt(8), Batch(BrowserBackButton))
    assertEquals(WorldEvents.buttonsFromInt(9), Batch(LeftMouseButton, BrowserBackButton))
    assertEquals(WorldEvents.buttonsFromInt(10), Batch(MiddleMouseButton, BrowserBackButton))
    assertEquals(WorldEvents.buttonsFromInt(11), Batch(LeftMouseButton, MiddleMouseButton, BrowserBackButton))
    assertEquals(WorldEvents.buttonsFromInt(12), Batch(RightMouseButton, BrowserBackButton))
    assertEquals(WorldEvents.buttonsFromInt(13), Batch(LeftMouseButton, RightMouseButton, BrowserBackButton))
    assertEquals(WorldEvents.buttonsFromInt(14), Batch(MiddleMouseButton, RightMouseButton, BrowserBackButton))
    assertEquals(
      WorldEvents.buttonsFromInt(15),
      Batch(LeftMouseButton, MiddleMouseButton, RightMouseButton, BrowserBackButton)
    )
    assertEquals(WorldEvents.buttonsFromInt(16), Batch(BrowserForwardButton))
    assertEquals(WorldEvents.buttonsFromInt(17), Batch(LeftMouseButton, BrowserForwardButton))
    assertEquals(WorldEvents.buttonsFromInt(18), Batch(MiddleMouseButton, BrowserForwardButton))
    assertEquals(WorldEvents.buttonsFromInt(19), Batch(LeftMouseButton, MiddleMouseButton, BrowserForwardButton))
    assertEquals(WorldEvents.buttonsFromInt(20), Batch(RightMouseButton, BrowserForwardButton))
    assertEquals(WorldEvents.buttonsFromInt(21), Batch(LeftMouseButton, RightMouseButton, BrowserForwardButton))
    assertEquals(WorldEvents.buttonsFromInt(22), Batch(MiddleMouseButton, RightMouseButton, BrowserForwardButton))
    assertEquals(
      WorldEvents.buttonsFromInt(23),
      Batch(LeftMouseButton, MiddleMouseButton, RightMouseButton, BrowserForwardButton)
    )
    assertEquals(WorldEvents.buttonsFromInt(24), Batch(BrowserBackButton, BrowserForwardButton))
    assertEquals(WorldEvents.buttonsFromInt(25), Batch(LeftMouseButton, BrowserBackButton, BrowserForwardButton))
    assertEquals(WorldEvents.buttonsFromInt(26), Batch(MiddleMouseButton, BrowserBackButton, BrowserForwardButton))
    assertEquals(
      WorldEvents.buttonsFromInt(27),
      Batch(LeftMouseButton, MiddleMouseButton, BrowserBackButton, BrowserForwardButton)
    )
    assertEquals(WorldEvents.buttonsFromInt(28), Batch(RightMouseButton, BrowserBackButton, BrowserForwardButton))
    assertEquals(
      WorldEvents.buttonsFromInt(29),
      Batch(LeftMouseButton, RightMouseButton, BrowserBackButton, BrowserForwardButton)
    )
    assertEquals(
      WorldEvents.buttonsFromInt(30),
      Batch(MiddleMouseButton, RightMouseButton, BrowserBackButton, BrowserForwardButton)
    )
    assertEquals(
      WorldEvents.buttonsFromInt(31),
      Batch(LeftMouseButton, MiddleMouseButton, RightMouseButton, BrowserBackButton, BrowserForwardButton)
    )
  }

}
