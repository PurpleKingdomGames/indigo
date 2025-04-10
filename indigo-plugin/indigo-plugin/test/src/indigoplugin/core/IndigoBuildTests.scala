package indigoplugin.core

import indigoplugin.IndigoAssets

class IndigoBuildTests extends munit.FunSuite {

  test("Asset filters (default)") {

    val base: os.RelPath               = os.RelPath("some")
    val toCopy: os.RelPath             = os.RelPath("some/path/foo.txt")
    val include: os.RelPath => Boolean = _ => false
    val exclude: os.RelPath => Boolean = _ => false
    val indigoAssets: IndigoAssets     = IndigoAssets(base, include, exclude, IndigoAssets.noRename)

    val actual = indigoAssets.isCopyAllowed(toCopy)

    assert(actual)
  }

  test("Asset filters (exclude specific file)") {

    val base: os.RelPath               = os.RelPath("some")
    val toCopy: os.RelPath             = os.RelPath("some/path/foo.txt")
    val include: os.RelPath => Boolean = _ => false
    val exclude: os.RelPath => Boolean = _ == os.RelPath("path/foo.txt")
    val indigoAssets: IndigoAssets     = IndigoAssets(base, include, exclude, IndigoAssets.noRename)

    val actual = indigoAssets.isCopyAllowed(toCopy)

    assert(actual)
  }

  test("Asset filters (include - would work anyway)") {

    val base: os.RelPath               = os.RelPath("some")
    val toCopy: os.RelPath             = os.RelPath("some/path/foo.txt")
    val include: os.RelPath => Boolean = _ == os.RelPath("path/foo.txt")
    val exclude: os.RelPath => Boolean = _ => false
    val indigoAssets: IndigoAssets     = IndigoAssets(base, include, exclude, IndigoAssets.noRename)

    val actual = indigoAssets.isCopyAllowed(toCopy)

    assert(actual)
  }

  test("Asset filters (include a file inside excluded folder)") {

    val base: os.RelPath               = os.RelPath("some")
    val toCopy: os.RelPath             = os.RelPath("some/path/foo.txt")
    val include: os.RelPath => Boolean = _ == os.RelPath("path/foo.txt")
    val exclude: os.RelPath => Boolean = _ == os.RelPath("path")
    val indigoAssets: IndigoAssets     = IndigoAssets(base, include, exclude, IndigoAssets.noRename)

    val actual = indigoAssets.isCopyAllowed(toCopy)

    assert(actual)
  }

}
