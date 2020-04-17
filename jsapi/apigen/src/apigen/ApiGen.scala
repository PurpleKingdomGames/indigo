package apigen

import os.Path
import scala.annotation.tailrec

@SuppressWarnings(Array("org.wartremover.warts.Equals"))
object ApiGen {

  val srcDirectory = os.pwd / "indigojs" / "src"

  @tailrec
  def chunk(remaining: List[String], active: List[String], acc: List[List[String]]): List[List[String]] =
    remaining match {
      case Nil =>
        acc :+ active

      case x :: xs if x.contains("indigodoc") =>
        chunk(xs, List(x), acc :+ active)

      case x :: xs =>
        chunk(xs, active :+ x, acc)

    }

  def processFile(outputDir: Path, path: Path): Unit = {
    val name: String = path.baseName

    val chunks =
      chunk(
        os.read.lines(path).toList.map(_.trim).filter(_.startsWith("//")),
        Nil,
        Nil
      ).filterNot(_.isEmpty)

    if (chunks.nonEmpty) {
      println("Found indigodocs to process in: " + name)
      LineParser.processChunks(outputDir, name, chunks)
      println("...done")
    }

  }

  def walkSourceTree(outputDir: Path): Unit = {
    val paths: List[Path] =
      os.walk(srcDirectory)
        .toList
        .filter(_.last.endsWith(".scala"))

    println("Source files found to process:")
    println(
      paths
        .map(_.relativeTo(os.pwd).segments.mkString("/"))
        .mkString("\n")
    )

    paths.foreach(p => processFile(outputDir, p))
  }

  def main(args: Array[String]): Unit = {
    val outputDir = os.pwd / "out" / "jsdocs"

    os.remove.all(outputDir)

    walkSourceTree(outputDir)
  }

}
