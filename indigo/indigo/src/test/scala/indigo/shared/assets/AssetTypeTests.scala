package indigo.shared.assets

import indigo.shared.assets.AssetType.Image
import indigo.shared.collections.Batch

class AssetTypeTests extends munit.FunSuite {

  test("should be able to tag images") {

    val assets: Set[AssetType] =
      Set(
        AssetType.Audio(AssetName("audio"), AssetPath("audio path")),
        AssetType.Tagged("fish")(
          AssetType.Image(AssetName("image 1"), AssetPath("image path 1")),
          AssetType.Image(AssetName("image 2"), AssetPath("image path 2")),
          AssetType.Image(AssetName("image 3"), AssetPath("image path 3"))
        ),
        AssetType.Image(AssetName("image 4"), AssetPath("image path 4")),
        AssetType.Text(AssetName("text"), AssetPath("text path"))
      )

    assertEquals(
      Batch
        .fromSet(assets)
        .flatMap(_.toBatch)
        .collect { case i: Image => i }
        .find(_.name == AssetName("image 1"))
        .get
        .tag,
      Some(AssetTag("fish"))
    )
    assertEquals(
      Batch
        .fromSet(assets)
        .flatMap(_.toBatch)
        .collect { case i: Image => i }
        .find(_.name == AssetName("image 2"))
        .get
        .tag,
      Some(AssetTag("fish"))
    )
    assertEquals(
      Batch
        .fromSet(assets)
        .flatMap(_.toBatch)
        .collect { case i: Image => i }
        .find(_.name == AssetName("image 3"))
        .get
        .tag,
      Some(AssetTag("fish"))
    )
    assertEquals(
      Batch
        .fromSet(assets)
        .flatMap(_.toBatch)
        .collect { case i: Image => i }
        .find(_.name == AssetName("image 4"))
        .get
        .tag,
      None
    )

  }

}
