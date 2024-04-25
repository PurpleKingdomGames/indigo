package indigoplugin.generators

import indigoplugin.DataType

object EmbedData {

  sealed trait Mode
  object Mode {
    final case class AsEnum(extendsFrom: Option[String])               extends Mode
    case object AsMap                                                  extends Mode
    final case class AsCustom(present: List[List[DataType]] => String) extends Mode
  }

  // Has a standard format, first row is headers, first column is keys.
  // Strings delimited with single or double quotes preserve the delimited
  // value, the quotes are dropped, but the other kind of quote within that
  // string is kept.
  // Cells cannot be empty.
  def generate(
      moduleName: String,
      fullyQualifiedPackage: String,
      filePath: os.Path,
      delimiter: String,
      rowFilter: String => Boolean,
      embedMode: Mode
  ): os.Path => Seq[os.Path] = outDir => {

    val lines =
      if (!os.exists(filePath)) throw new Exception("Path to data file not found: " + filePath.toString())
      else {
        os.read.lines(filePath).filter(rowFilter)
      }

    val rows =
      lines.map(row => extractRowData(row, delimiter)).toList

    val dataFrame =
      DataFrame.fromRows(rows)

    val wd = outDir / Generators.OutputDirName

    os.makeDir.all(wd)

    val file = wd / s"$moduleName.scala"

    val contents =
      embedMode match {
        case Mode.AsEnum(extendsFrom) =>
          s"""package $fullyQualifiedPackage
          |
          |// DO NOT EDIT: Generated by Indigo.
          |${dataFrame.renderEnum(moduleName, extendsFrom)}
          |""".stripMargin

        case Mode.AsMap =>
          s"""package $fullyQualifiedPackage
          |
          |// DO NOT EDIT: Generated by Indigo.
          |${dataFrame.renderMap(moduleName)}
          |""".stripMargin

        case Mode.AsCustom(present) =>
          s"""package $fullyQualifiedPackage
          |
          |// DO NOT EDIT: Generated by Indigo.
          |${dataFrame.renderCustom(present)}
          |""".stripMargin

      }

    os.write.over(file, contents)

    Seq(file)
  }

  def extractRowData(row: String, delimiter: String): List[DataType] = {

    val cleanDelimiter: String =
      if (delimiter == "\\|") "|" else delimiter

    val cleanRow: String =
      row.trim match {
        case r if r.startsWith(cleanDelimiter) && r.endsWith(cleanDelimiter) =>
          r.drop(cleanDelimiter.length()).dropRight(cleanDelimiter.length())

        case r if r.startsWith(cleanDelimiter) =>
          r.drop(cleanDelimiter.length())

        case r if r.endsWith(cleanDelimiter) =>
          r.dropRight(cleanDelimiter.length())

        case r =>
          r
      }

    parse(delimiter)(cleanRow).map(_._1).collect {
      case d @ DataType.StringData(s, _) if s.nonEmpty => d
      case DataType.StringData(_, _)                   => DataType.NullData
      case d: DataType.BooleanData                     => d
      case d: DataType.DoubleData                      => d
      case d: DataType.IntData                         => d
      case DataType.NullData                           => DataType.NullData
    }
  }

  // A parser of things,
  // is a function from strings,
  // to a list of pairs
  // of things and strings.
  def parse(delimiter: String): String => List[(DataType, String)] = {
    val takeUpToDelimiter         = s"^(.*?)${delimiter}(.*)".r
    val takeMatchingSingleQuotes  = s"^'(.*?)'${delimiter}(.*)".r
    val takeMatchingDoubleQuotes  = s"""^\"(.*?)\"${delimiter}(.*)""".r
    val takeRemainingSingleQuotes = s"^'(.*?)'".r
    val takeRemainingDoubleQuotes = s"""^\"(.*?)\"""".r

    (in: String) =>
      in match {
        case takeMatchingDoubleQuotes(take, left) =>
          List(DataType.decideType(take.trim) -> left) ++ parse(delimiter)(left.trim)

        case takeMatchingSingleQuotes(take, left) =>
          List(DataType.decideType(take.trim) -> left) ++ parse(delimiter)(left.trim)

        case takeUpToDelimiter(take, left) =>
          List(DataType.decideType(take.trim) -> left) ++ parse(delimiter)(left.trim)

        case takeRemainingSingleQuotes(take) =>
          List(DataType.decideType(take.trim) -> "")

        case takeRemainingDoubleQuotes(take) =>
          List(DataType.decideType(take.trim) -> "")

        case take =>
          List(DataType.decideType(take.trim) -> "")
      }
  }
}

final case class DataFrame(data: Array[Array[DataType]], columnCount: Int) {
  def headers: Array[DataType.StringData] =
    data.head.map(_.toStringData)

  def rows: Array[Array[DataType]] =
    data.tail

  def alignColumnTypes: DataFrame = {
    val columns =
      DataType.matchHeaderRowLength(rows).transpose

    val stringKeys: Array[DataType] =
      columns.head.map(_.toStringData)

    val typedColumns: Array[Array[DataType]] = columns.tail
      .map(d => DataType.convertToBestType(d.toList).toArray)

    val optionalColumns: Array[Array[DataType]] =
      typedColumns.map { col =>
        if (DataType.hasOptionalValues(col.toList)) {
          col.map(_.makeOptional)
        } else {
          col
        }
      }

    val cleanedRows: Array[Array[DataType]] =
      (stringKeys +: optionalColumns).transpose

    this.copy(
      data = headers.asInstanceOf[Array[DataType]] +: cleanedRows
    )
  }

  def toSafeName: String => String = { name: String =>
    name.replaceAll("[^a-zA-Z0-9]", "-").split("-").toList.filterNot(_.isEmpty) match {
      case h :: t if h.take(1).matches("[0-9]") => ("_" :: h :: t.map(_.capitalize)).mkString
      case l                                    => l.map(_.capitalize).mkString
    }
  }

  def toSafeNameCamel: String => String = { name: String =>
    name.replaceAll("[^a-zA-Z0-9]", "-").split("-").toList.filterNot(_.isEmpty) match {
      case h :: t if h.take(1).matches("[0-9]") => ("_" :: h :: t.map(_.capitalize)).mkString
      case h :: t                               => (h.toLowerCase :: t.map(_.capitalize)).mkString
      case l                                    => l.map(_.capitalize).mkString
    }
  }

  def renderVars(omitVal: Boolean): String = {
    val names = headers.drop(1).map(_.value)
    val types = rows.head.drop(1).map(_.giveTypeName)
    names
      .zip(types)
      .map { case (n, t) =>
        (if (omitVal) "" else "val ") + s"${toSafeNameCamel(n)}: $t"
      }
      .mkString(", ")
  }

  def renderEnum(moduleName: String, extendsFrom: Option[String]): String = {
    val renderedRows =
      rows
        .map { r =>
          s"""  case ${toSafeName(r.head.asString)} extends $moduleName(${r.tail
            .map(_.asString)
            .mkString(", ")})"""
        }
        .mkString("\n")

    val extFrom = extendsFrom
      .map { module =>
        s""" extends $module"""
      }
      .getOrElse("")

    s"""
    |enum $moduleName(${renderVars(false)})$extFrom:
    |${renderedRows}
    |""".stripMargin
  }

  def renderMap(moduleName: String): String = {
    val renderedRows =
      rows
        .map { r =>
          s"""      ${r.head.asString} -> $moduleName(${r.tail.map(_.asString).mkString(", ")})"""
        }
        .mkString(",\n")

    s"""
    |final case class $moduleName(${renderVars(true)})
    |object $moduleName:
    |  val data: Map[String, $moduleName] =
    |    Map(
    |${renderedRows}
    |    )
    |""".stripMargin
  }

  def renderCustom(present: List[List[DataType]] => String): String =
    present(headers.toList :: rows.map(_.toList).toList)
}
object DataFrame {

  private val standardMessage: String =
    "Embedded data must have two rows (minimum) of the same length (two columns minimum). The first row is the headers / field names. The first column are the keys."

  def fromRows(rows: List[List[DataType]]): DataFrame =
    rows match {
      case Nil =>
        throw new Exception("No data to create. " + standardMessage)

      case _ :: Nil =>
        throw new Exception("Only one row of data found. " + standardMessage)

      case h :: _ =>
        val len = h.length

        if (len == 0) {
          throw new Exception("No data to create. " + standardMessage)
        } else if (len == 1) {
          throw new Exception("Only one column of data. " + standardMessage)
        } else {
          DataFrame(rows.map(_.toArray).toArray, len).alignColumnTypes
        }
    }

}
