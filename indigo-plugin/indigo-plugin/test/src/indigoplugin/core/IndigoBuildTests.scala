package indigoplugin.core

class IndigoBuildTests extends munit.FunSuite {

  test("Asset filters (default)") {

    val base: os.Path                  = os.Path("/some")
    val toCopy: os.Path                = os.Path("/some/path/foo.txt")
    val include: os.RelPath => Boolean = _ => false
    val exclude: os.RelPath => Boolean = _ => false

    val actual = IndigoBuild.isCopyAllowed(base, toCopy, include, exclude)

    assert(actual)
  }

  test("Asset filters (exclude specific file)") {

    val base: os.Path                  = os.Path("/some")
    val toCopy: os.Path                = os.Path("/some/path/foo.txt")
    val include: os.RelPath => Boolean = _ => false
    val exclude: os.RelPath => Boolean = _ == os.RelPath("path/foo.txt")

    val actual = !IndigoBuild.isCopyAllowed(base, toCopy, include, exclude)

    assert(actual)
  }

  test("Asset filters (include - would work anyway)") {

    val base: os.Path                  = os.Path("/some")
    val toCopy: os.Path                = os.Path("/some/path/foo.txt")
    val include: os.RelPath => Boolean = _ == os.RelPath("path/foo.txt")
    val exclude: os.RelPath => Boolean = _ => false

    val actual = IndigoBuild.isCopyAllowed(base, toCopy, include, exclude)

    assert(actual)
  }

  test("Asset filters (include a file inside excluded folder)") {

    val base: os.Path                  = os.Path("/some")
    val toCopy: os.Path                = os.Path("/some/path/foo.txt")
    val include: os.RelPath => Boolean = _ == os.RelPath("path/foo.txt")
    val exclude: os.RelPath => Boolean = _ == os.RelPath("path")

    val actual = IndigoBuild.isCopyAllowed(base, toCopy, include, exclude)

    assert(actual)
  }

}
