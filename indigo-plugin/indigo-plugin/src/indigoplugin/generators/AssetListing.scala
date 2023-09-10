package indigoplugin.generators

object AssetListing {

  def pathsToTree(paths: List[os.RelPath]): PathTree =
    PathTree.combineAll(
      paths.map { p =>
        PathTree.pathToPathTree(p) match {
          case None        => throw new Exception(s"Could not parse given path: $p")
          case Some(value) => value
        }
      }
    ).sorted

}

sealed trait PathTree {
  def name: String

  def combine(other: PathTree): PathTree =
    PathTree.combine(this, other)
  def |+|(other: PathTree): PathTree =
    combine(other)

  def sorted: PathTree =
    this match {
      case PathTree.Root(children) =>
        PathTree.Root(children.map(_.sorted).sortBy(_.name))

      case PathTree.Folder(n, children) =>
        PathTree.Folder(n, children.map(_.sorted).sortBy(_.name))

      case f @ PathTree.File(_, _, _) =>
        f
    }
}

object PathTree {

  def empty: PathTree =
    PathTree.Root(Nil)

  private def combineChildren(a: List[PathTree], b: List[PathTree]): List[PathTree] =
    b.flatMap {
      case PathTree.Root(children) =>
        combineChildren(a, children)

      case f @ PathTree.Folder(name, children) =>
        a.find {
          case PathTree.Folder(n, _) => n == name
          case _                     => false
        } match {
          case None =>
            a ++ List(f)

          case Some(fdlr @ PathTree.Folder(n, c)) =>
            a.filterNot(_ == fdlr) ++ List(PathTree.Folder(n, combineChildren(c, children)))

          case Some(_) =>
            a ++ List(f) // Shouldn't happen
        }

      case f: PathTree.File =>
        if (a.contains(f)) a else a ++ List(f)
    }

  def combine(a: PathTree, b: PathTree): PathTree =
    (a, b) match {
      case (PathTree.Root(Nil), PathTree.Root(Nil)) =>
        a

      case (PathTree.Root(csA), PathTree.Root(csB)) =>
        PathTree.Root(
          combineChildren(csA, csB)
        )

      case (PathTree.Root(csA), f @ PathTree.Folder(_, _)) =>
        PathTree.Root(combineChildren(csA, List(f)))

      case (PathTree.Folder(n, csA), PathTree.Root(csB)) =>
        PathTree.Folder(n, combineChildren(csA, csB))

      case (PathTree.Root(cs), f @ PathTree.File(_, _, _)) =>
        PathTree.Root(combineChildren(cs, List(f)))

      case (f @ PathTree.File(_, _, _), PathTree.Root(cs)) =>
        PathTree.Root(combineChildren(cs, List(f)))

      case (PathTree.Folder(nA, _), PathTree.Folder(nB, _)) if nA != nB =>
        throw new Exception(
          s"Something has gone wrong merging asset trees. Found two folders with different names: $nA and $nB"
        )

      case (PathTree.Folder(nA, csA), PathTree.Folder(_, csB)) =>
        PathTree.Folder(nA, combineChildren(csA, csB))

      case (PathTree.File(nA, _, _), PathTree.File(nB, _, _)) if nA != nB =>
        throw new Exception(
          s"Something has gone wrong merging asset trees. Found two file with different names: $nA and $nB"
        )

      case (f @ PathTree.File(_, _, _), PathTree.File(_, _, _)) =>
        f

      case (PathTree.Folder(nA, csA), f @ PathTree.File(_, _, _)) =>
        PathTree.Folder(nA, combineChildren(csA, List(f)))

      case (f @ PathTree.File(_, _, _), PathTree.Folder(nB, csB)) =>
        PathTree.Folder(nB, combineChildren(csB, List(f)))
    }

  def combineAll(pathTrees: List[PathTree]): PathTree =
    pathTrees.foldLeft(empty)(_ |+| _)

  def pathToPathTree(remaining: os.RelPath, original: os.RelPath): Option[PathTree] = {
    val result =
      remaining.segments.toList match {
        case Nil =>
          Option(
            PathTree.Root(Nil)
          )

        case p :: _ if p.isEmpty =>
          None

        case p :: Nil =>
          p.split('.').toList match {
            case Nil =>
              None

            case n :: ext :: Nil =>
              Option(
                PathTree.File(n, ext, original)
              )

            case n :: exts =>
              Option(
                PathTree.File(n, exts.mkString("."), original)
              )
          }

        case p :: ps =>
          Option(
            PathTree.Folder(
              p,
              pathToPathTree(os.RelPath(ps.toIndexedSeq, 0), original).toList
            )
          )
      }

    if (remaining == original) {
      Option(PathTree.Root(result.toList))
    } else {
      result
    }
  }
  def pathToPathTree(path: os.RelPath): Option[PathTree] =
    pathToPathTree(path, path)

  final case class Root(children: List[PathTree]) extends PathTree {
    val name: String = ""
  }
  final case class Folder(name: String, children: List[PathTree]) extends PathTree
  final case class File(name: String, extension: String, path: os.RelPath) extends PathTree {
    val fullName: String = name + "." + extension
  }

}
