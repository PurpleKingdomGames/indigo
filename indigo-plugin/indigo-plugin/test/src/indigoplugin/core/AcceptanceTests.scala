package indigoplugin.core

import indigoplugin.IndigoOptions
import indigoplugin.IndigoTemplate
import indigoplugin.utils.Utils
import indigoplugin.IndigoAssets

class AcceptanceTests extends munit.FunSuite {

  val workspaceDir    = Utils.findWorkspace
  val assetsDirectory = workspaceDir / "test-assets"

  val targetBaseDir = os.pwd / "out" / "indigo-plugin-acceptance-test-output"
  val targetDir     = targetBaseDir / assetsDirectory.last

  private def cleanUp(): Unit = {
    if (os.exists(targetBaseDir)) {
      os.remove.all(target = targetBaseDir)
    }

    os.makeDir.all(targetBaseDir)
  }

  override def beforeAll(): Unit                     = cleanUp()
  override def beforeEach(context: BeforeEach): Unit = cleanUp()

  val include: os.RelPath => Boolean = {
    case p if p.endsWith(os.RelPath("taken.txt"))      => true
    case p if p.toString.matches("(.*)also-taken.txt") => true
    case _                                             => false
  }
  val exclude: os.RelPath => Boolean = {
    case p if p.startsWith(os.RelPath("ignored-folder")) => true
    case p if p.startsWith(os.RelPath("mixed"))          => true
    case p if p.startsWith(os.RelPath("captain"))        => true
    case p if p.endsWith(os.RelPath("stats.md"))         => true
    case p if p.endsWith(os.RelPath("armour.md"))        => true
    case p if p.endsWith(os.RelPath("colours.txt"))      => true
    case _                                               => false
  }

  val indigoOptions =
    IndigoOptions.defaults
      .includeAssets(include)
      .excludeAssets(exclude)

  test("List assets to copy") {
    val baseDirectory = workspaceDir

    val actual: List[os.Path] =
      IndigoAssets.filesToCopy(indigoOptions.assets, assetsDirectory)

    val expected: List[os.Path] =
      List(
        assetsDirectory / "data",
        assetsDirectory / "data" / "stats.csv",
        assetsDirectory / "mixed" / "also-taken.txt",
        assetsDirectory / "mixed" / "taken.txt",
        assetsDirectory / "foo.txt"
      )

    assertEquals(actual.length, expected.length)
    assert(clue(expected).forall(clue(actual).contains))
  }

  test("List all asset files as relative paths") {
    val baseDirectory = workspaceDir

    val actual: List[os.RelPath] =
      IndigoAssets.listAssetFiles(indigoOptions.assets, assetsDirectory)

    val expected: List[os.RelPath] =
      List(
        os.RelPath.rel / assetsDirectory.last / "data" / "stats.csv",
        os.RelPath.rel / assetsDirectory.last / "mixed" / "also-taken.txt",
        os.RelPath.rel / assetsDirectory.last / "mixed" / "taken.txt",
        os.RelPath.rel / assetsDirectory.last / "foo.txt"
      )

    assertEquals(actual.length, expected.length)
    assert(clue(expected).forall(clue(actual).contains))
  }

  test("List all asset files as relative paths - sub dir") {
    val baseDirectory = workspaceDir

    val actual: List[os.RelPath] =
      IndigoAssets.listAssetFiles(
        indigoOptions.assets,
        assetsDirectory / "mixed"
      )

    val expected: List[os.RelPath] =
      List(
        os.RelPath.rel / "mixed" / "ignored-file.txt",
        os.RelPath.rel / "mixed" / "also-taken.txt",
        os.RelPath.rel / "mixed" / "taken.txt"
      )

    assertEquals(actual.length, expected.length)
    assert(clue(expected).forall(clue(actual).contains))
  }

  test("Copy assets and assert expected output files") {
    val baseDirectory = workspaceDir

    IndigoBuild.copyAssets(indigoOptions.assets, assetsDirectory, targetDir)

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

  test("Build a game using the default template") {
    IndigoBuild.build(
      scriptPathBase = workspaceDir / "test-files",
      options = indigoOptions,
      assetsDirectory = assetsDirectory,
      baseDirectory = targetBaseDir,
      scriptNames = List("game.js")
    )

    // Template files
    assert(os.exists(targetBaseDir))
    assert(os.exists(targetBaseDir / "index.html"))
    assert(os.exists(targetBaseDir / "cordova.js"))
    assert(os.exists(targetBaseDir / "scripts" / "game.js"))
    assert(os.exists(targetBaseDir / "scripts" / "game.js.map"))
    assert(os.exists(targetBaseDir / "scripts" / "indigo-support.js"))

    // Assets - Basics
    assert(os.exists(targetBaseDir / "assets"))
    assert(os.exists(targetBaseDir / "assets" / "foo.txt"))
    assert(os.exists(targetBaseDir / "assets" / "data" / "stats.csv"))

    // Assets - Ignored folder
    assert(!os.exists(targetBaseDir / "assets" / "ignored-folder"))
    assert(!os.exists(targetBaseDir / "assets" / "ignored-folder" / "ignored-file.txt"))

    // Assets - Generally excluded, but some taken.
    assert(os.exists(targetBaseDir / "assets" / "mixed"))
    assert(!os.exists(targetBaseDir / "assets" / "mixed" / "ignored-file.txt"))
    assert(os.exists(targetBaseDir / "assets" / "mixed" / "taken.txt"))
    assert(os.exists(targetBaseDir / "assets" / "mixed" / "also-taken.txt"))
  }

  test("Build a game using a custom template") {
    val custom =
      indigoOptions.useCustomTemplate(
        IndigoTemplate.Inputs(workspaceDir / "test-custom-template"),
        IndigoTemplate.Outputs(
          os.rel / "game-assets",
          os.rel / "game-scripts"
        )
      )

    IndigoBuild.build(
      scriptPathBase = workspaceDir / "test-files",
      options = custom,
      assetsDirectory = assetsDirectory,
      baseDirectory = targetBaseDir,
      scriptNames = List("game.js")
    )

    // Template files
    assert(os.exists(targetBaseDir))
    assert(os.exists(targetBaseDir / "index.html"))
    assert(os.exists(targetBaseDir / "test.css"))
    assert(os.exists(targetBaseDir / "game-scripts" / "game.js"))
    assert(os.exists(targetBaseDir / "game-scripts" / "game.js.map"))

    // Assets
    assert(os.exists(targetBaseDir / "game-assets"))
    assert(os.exists(targetBaseDir / "game-assets" / "foo.txt"))
    assert(os.exists(targetBaseDir / "game-assets" / "data" / "stats.csv"))
    assert(os.exists(targetBaseDir / "game-assets" / "mixed"))
    assert(os.exists(targetBaseDir / "game-assets" / "mixed" / "taken.txt"))
    assert(os.exists(targetBaseDir / "game-assets" / "mixed" / "also-taken.txt"))
  }

}
