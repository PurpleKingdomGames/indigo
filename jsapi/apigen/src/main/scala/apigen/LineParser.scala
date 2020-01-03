package apigen

import scala.annotation.tailrec
import os.Path

@SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.ToString", "org.wartremover.warts.Throw"))
object LineParser {

  def processChunks(outputDir: Path, fileName: String, chunks: List[List[String]]): Unit = {
    val entities: List[Option[EntityDefinition]] =
      chunks.map(processLines)

    if (!os.exists(outputDir))
      os.makeDir(outputDir)

    entities.collect { case Some(s) => s }.foreach { e =>
      os.write.append(outputDir / (fileName + ".xml"), e.toXml)
    }
  }

  def processLines(lines: List[String]): Option[EntityDefinition] = {
    @tailrec
    def rec(remaining: List[String], entity: Option[EntityDefinition]): Option[EntityDefinition] =
      remaining match {
        case Nil =>
          entity

        case x :: xs if x.contains("indigodoc") =>
          rec(xs, parseHeader(x).map(_.toEntity))

        case _ :: xs =>
          //Skip for now
          rec(xs, entity)
      }

    rec(lines, None)
  }

  def parseHeader(line: String): Option[Header] = {
    val parts = line.split(' ').toList.map(_.trim)

    val f: (String, List[String]) => Option[String] =
      (n, ps) =>
        ps.find(_.contains(n)).map { p =>
          p.substring(n.length())
        }

    for {
      entity <- f("entity:", parts)
      name   <- f("name:", parts)
    } yield Header(entity, name, f("type:", parts))
  }

}

@SuppressWarnings(Array("org.wartremover.warts.Throw"))
final case class Header(entity: String, name: String, returnType: Option[String]) {

  def toEntity: EntityDefinition =
    (entity, returnType) match {
      case ("static", _) =>
        StaticEntity(name)

      case ("class", _) =>
        ClassEntity(name)

      case ("value", Some(rt)) =>
        ValueEntity(name, rt)

      case ("value", None) =>
        throw new Exception("Couldn't make value type for " + name + ", missing return type")

      case ("method", _) =>
        MethodEntity(name)

      case ("function", _) =>
        FunctionEntity(name)

      case (l, _) =>
        throw new Exception("Oops.. what was this entity? " + l)
    }

}
