package sbtindigo

import sbt.File

object Utils {

  def ensureDirectoryAt(path: String): File = {
    val dirFile = new File(path)

    if (!dirFile.exists())
      dirFile.mkdir()

    dirFile
  }

}