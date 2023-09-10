package indigoplugin.generators

class AssetListingTests extends munit.FunSuite {

  test("It should be able to render some asset details") {

    val paths: List[os.RelPath] =
      List(
        os.RelPath.rel / "folderA" / "folderB" / "d.jpg",
        os.RelPath.rel / "folderC" / "f.mp3",
        os.RelPath.rel / "folderA" / "folderB" / "b.png",
        os.RelPath.rel / "a.txt",
        os.RelPath.rel / "folderA" / "folderB" / "c.png",
        os.RelPath.rel / "folderA" / "e.svg"
      )

    val actual =
      AssetListing.renderContent(paths)

    val expected =
      """
      |
      |""".stripMargin.trim

    println(actual)

    assertEquals(actual, expected)

  }

  test("toSafeName should be able to convert file and folder names into something safe") {
    assertEquals(AssetListing.toSafeName("hello"), "Hello")
    assertEquals(AssetListing.toSafeName("hello-there-01"), "HelloThere01")
    assertEquals(AssetListing.toSafeName("hello-there-01.jpg"), "HelloThere01Jpg")
    assertEquals(AssetListing.toSafeName("^hello!there_0 1.jpg"), "HelloThere01Jpg")
    assertEquals(AssetListing.toSafeName("00-hello"), "_00Hello")
  }

}
