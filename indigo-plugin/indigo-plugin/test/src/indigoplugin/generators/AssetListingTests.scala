package indigoplugin.generators

class AssetListingTests extends munit.FunSuite {

  test("Can convert a single rel path to a path tree") {
    val path = os.RelPath.rel / "folderA" / "folderB" / "d.jpg"

    val actual =
      PathTree.pathToPathTree(path)

    val expected =
      PathTree.Root(
        List(
          PathTree.Folder(
            "folderA",
            List(
              PathTree.Folder(
                "folderB",
                List(
                  PathTree.File("d", "jpg", os.RelPath.rel / "folderA" / "folderB" / "d.jpg")
                )
              )
            )
          )
        )
      )

    assert(clue(actual.isDefined))
    assertEquals(clue(actual.get), clue(expected))
  }

  test("Can combine two path trees") {
    val pathA = os.RelPath.rel / "folderA" / "folderB" / "a.jpg"
    val pathB = os.RelPath.rel / "folderA" / "folderC" / "b.png"

    val actual =
      PathTree.pathToPathTree(pathA).get |+| PathTree.pathToPathTree(pathB).get

    val expected =
      PathTree.Root(
        List(
          PathTree.Folder(
            "folderA",
            List(
              PathTree.Folder(
                "folderB",
                List(
                  PathTree.File("a", "jpg", pathA)
                )
              ),
              PathTree.Folder(
                "folderC",
                List(
                  PathTree.File("b", "png", pathB)
                )
              )
            )
          )
        )
      )

    assertEquals(clue(actual), clue(expected))

  }

  test("Can convert a list of relative paths to a PathTree") {

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
      AssetListing.pathsToTree(paths)

    val expected =
      PathTree.Root(
        List(
          PathTree.Folder(
            "folderC",
            List(
              PathTree.File("f", "mp3", os.RelPath.rel / "folderC" / "f.mp3")
            )
          ),
          PathTree.File("a", "txt", os.RelPath.rel / "a.txt"),
          PathTree.Folder(
            "folderA",
            List(
              PathTree.Folder(
                "folderB",
                List(
                  PathTree.File("d", "jpg", os.RelPath.rel / "folderA" / "folderB" / "d.jpg"),
                  PathTree.File("b", "png", os.RelPath.rel / "folderA" / "folderB" / "b.png"),
                  PathTree.File("c", "png", os.RelPath.rel / "folderA" / "folderB" / "c.png")
                )
              ),
              PathTree.File("e", "svg", os.RelPath.rel / "folderA" / "e.svg")
            )
          )
        )
      )

    assertEquals(clue(actual), clue(expected))
  }

}
