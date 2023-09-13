package indigoplugin.core

import indigoplugin.IndigoAssets

class AcceptanceTests extends munit.FunSuite {

  val sourceDir = os.RelPath("test-assets")

  val targetDir = os.pwd / "out" / "indigo-plugin-acceptance-test-output" / sourceDir

  override def beforeAll(): Unit = {
    if (os.exists(targetDir)) {
      os.remove.all(target = targetDir)
    }

    os.makeDir.all(targetDir)
  }

  val indigoAssets =
    IndigoAssets(
      gameAssetsDirectory = sourceDir,
      include = {
        case p if p.endsWith(os.RelPath("taken.txt"))      => true
        case p if p.toString.matches("(.*)also-taken.txt") => true
        case _                                             => false
      },
      exclude = {
        case p if p.startsWith(os.RelPath("ignored-folder")) => true
        case p if p.startsWith(os.RelPath("mixed"))          => true
        case _                                               => false
      },
      None
    )

  test("List assets to copy") {
    val baseDirectory = os.pwd

    val actual: List[os.Path] =
      indigoAssets.filesToCopy(baseDirectory)

    val expected: List[os.Path] =
      List(
        baseDirectory / sourceDir / "data",
        baseDirectory / sourceDir / "data" / "stats.csv",
        baseDirectory / sourceDir / "mixed" / "also-taken.txt",
        baseDirectory / sourceDir / "mixed" / "taken.txt",
        baseDirectory / sourceDir / "foo.txt"
      )

    assertEquals(actual, expected)
  }

  test("List all asset files as relative paths") {
    val baseDirectory = os.pwd

    val actual: List[os.RelPath] =
      indigoAssets.listAssetFiles(baseDirectory)

    val expected: List[os.RelPath] =
      List(
        sourceDir / "data" / "stats.csv",
        sourceDir / "mixed" / "also-taken.txt",
        sourceDir / "mixed" / "taken.txt",
        sourceDir / "foo.txt"
      )

    assertEquals(actual, expected)
  }

  test("Copy assets and assert expected output files") {
    val baseDirectory = os.pwd

    IndigoBuild.copyAssets(baseDirectory, indigoAssets, targetDir)

    // Basics
    assert(os.exists(targetDir))
    assert(os.exists(targetDir / "foo.txt"))
    assert(os.exists(targetDir / "data" / "stats.csv"))

    // Ignored folder
    assert(!os.exists(targetDir / "ignored-folder"))
    assert(!os.exists(targetDir / "ignored-folder" / "ignored-file.txt"))

    // Generally excluded, but some taken.
    assert(os.exists(targetDir / "mixed"))
    assert(!os.exists(targetDir / "mixed" / "ignored-file.txt"))
    assert(os.exists(targetDir / "mixed" / "taken.txt"))
    assert(os.exists(targetDir / "mixed" / "also-taken.txt"))

  }

}
