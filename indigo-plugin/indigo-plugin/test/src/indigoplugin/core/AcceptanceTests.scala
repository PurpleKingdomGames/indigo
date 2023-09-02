package indigoplugin.core

import indigoplugin.IndigoAssets

class AcceptanceTests extends munit.FunSuite {

  val sourceDir = os.RelPath("test-assets")

  val targetDir = os.pwd / "out" / "indigo-plugin-acceptance-test-output"

  override def beforeAll(): Unit = {
    if (os.exists(targetDir)) {
      os.remove(target = targetDir, checkExists = true)
    }

    os.makeDir.all(targetDir)
  }

  val indigoAssets =
    IndigoAssets(
      gameAssetsDirectory = sourceDir,
      include = { case _ => false },
      exclude = { case _ => false }
    )

  test("Copy assets and assert expected output files") {

    IndigoBuild.copyAssets(indigoAssets, targetDir)

    assert(os.exists(targetDir))
    assert(os.exists(targetDir / "foo.txt"))
  }

}
