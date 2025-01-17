package indigoplugin.utils

object Utils {

  def ensureDirectoryAt(path: os.Path): os.Path = {
    os.remove.all(path)
    os.makeDir.all(path)

    path
  }

  def findWorkspace: os.Path =
    sys.env
      .get("MILL_WORKSPACE_ROOT")
      .map(os.Path(_))
      .getOrElse(os.pwd)

}
