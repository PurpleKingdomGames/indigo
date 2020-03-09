package indigo.shared.assets

import utest._
import indigo.shared.assets.AssetType.Image

object AssetTypeTests extends TestSuite {

  val tests: Tests =
    Tests {

      "should be able to tag images" - {

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

        assets.toList.flatMap(_.toList).collect { case i: Image => i }.find(_.name.value == "image 1").get.tag ==> Some(AssetTag("fish"))
        assets.toList.flatMap(_.toList).collect { case i: Image => i }.find(_.name.value == "image 2").get.tag ==> Some(AssetTag("fish"))
        assets.toList.flatMap(_.toList).collect { case i: Image => i }.find(_.name.value == "image 3").get.tag ==> Some(AssetTag("fish"))
        assets.toList.flatMap(_.toList).collect { case i: Image => i }.find(_.name.value == "image 4").get.tag ==> None

      }

    }

}
