package indigoplugin

import os._

// This is lifted from os-lib FileOps because os.copy has some sort of
// binary compat problem between version 0.7.1/4 and 0.7.6
object CustomOSLibCopy {

  import java.nio.file
  import java.nio.file.{Path => _, _}
  import java.nio.file.attribute.{FileAttribute, PosixFilePermission, PosixFilePermissions}

  def copy(
      from: Path,
      to: Path,
      followLinks: Boolean,
      replaceExisting: Boolean,
      copyAttributes: Boolean,
      createFolders: Boolean,
      mergeFolders: Boolean
  ): Unit = {
    if (createFolders) makeDir.all(to / up)
    val opts1 =
      if (followLinks) Array[CopyOption]()
      else Array[CopyOption](LinkOption.NOFOLLOW_LINKS)
    val opts2 =
      if (replaceExisting) Array[CopyOption](StandardCopyOption.REPLACE_EXISTING)
      else Array[CopyOption]()
    val opts3 =
      if (copyAttributes) Array[CopyOption](StandardCopyOption.COPY_ATTRIBUTES)
      else Array[CopyOption]()
    require(
      !to.startsWith(from),
      s"Can't copy a directory into itself: $to is inside $from"
    )

    def copyOne(p: Path): file.Path = {
      val target = to / p.relativeTo(from)
      if (mergeFolders && isDir(p, followLinks) && isDir(target, followLinks)) {
        // nothing to do
        target.wrapped
      } else {
        Files.copy(p.wrapped, target.wrapped, opts1 ++ opts2 ++ opts3: _*)
      }
    }

    copyOne(from)
    if (stat(from, followLinks = followLinks).isDir) walk(from).map(copyOne)
  }

  def copyInto(from: Path,
            to: Path,
            followLinks: Boolean,
            replaceExisting: Boolean,
            copyAttributes: Boolean,
            createFolders: Boolean,
            mergeFolders: Boolean): Unit = {
    copy(
      from, to/from.last,
      followLinks, replaceExisting, copyAttributes, createFolders, mergeFolders
    )
  }

}
