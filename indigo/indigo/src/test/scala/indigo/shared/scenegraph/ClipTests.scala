package indigo.shared.scenegraph

import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Size
import indigo.shared.materials.Material
import indigo.shared.time.FPS
import indigo.shared.time.Seconds

class ClipTests extends munit.FunSuite {

  val clip = Clip(
    Point.zero,
    Size(32),
    ClipSheet(10, FPS(10)),
    Material.Bitmap(AssetName("test"))
  )

  test("Calling reverse changes play direction (forward)") {
    val actual =
      clip.reverse.playMode.direction

    val expected =
      ClipPlayDirection.Backward

    assertEquals(actual, expected)
  }

  test("Calling reverse changes play direction (backwards)") {
    val actual =
      clip
        .withPlayMode(ClipPlayMode.PlayOnce(ClipPlayDirection.Backward, Seconds(1)))
        .reverse
        .playMode
        .direction

    val expected =
      ClipPlayDirection.Forward

    assertEquals(actual, expected)
  }

  test("Calling reverse changes play direction and back again") {
    val actual =
      clip.reverse.reverse.playMode.direction

    val expected =
      ClipPlayDirection.Forward

    assertEquals(actual, expected)
  }

}
