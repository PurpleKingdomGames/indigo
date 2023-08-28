package indigoplugin.utils

import os._

object Utils {

  def ensureDirectoryAt(path: Path): Path = {
    os.remove.all(path)
    os.makeDir.all(path)

    path
  }

}
