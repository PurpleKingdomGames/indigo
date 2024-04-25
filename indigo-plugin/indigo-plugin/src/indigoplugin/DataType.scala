package indigoplugin

import scala.util.matching.Regex

sealed trait DataType {

  def nullable: Boolean
  def makeOptional: DataType

  def isString: Boolean =
    this match {
      case _: DataType.StringData => true
      case _                      => false
    }

  def isDouble: Boolean =
    this match {
      case _: DataType.DoubleData => true
      case _                      => false
    }

  def isInt: Boolean =
    this match {
      case _: DataType.IntData => true
      case _                   => false
    }

  def isBoolean: Boolean =
    this match {
      case _: DataType.BooleanData => true
      case _                       => false
    }

  def isNull: Boolean =
    this match {
      case DataType.NullData => true
      case _                 => false
    }

  def toStringData: DataType.StringData =
    this match {
      case s: DataType.StringData if s.nullable              => DataType.StringData(s"""Some("${s.value}")""", true)
      case s: DataType.StringData                            => s
      case DataType.BooleanData(value, nullable) if nullable => DataType.StringData(s"Some(${value.toString})", true)
      case DataType.BooleanData(value, _)                    => DataType.StringData(value.toString, false)
      case DataType.DoubleData(value, nullable) if nullable  => DataType.StringData(s"Some(${value.toString})", true)
      case DataType.DoubleData(value, _)                     => DataType.StringData(value.toString, false)
      case DataType.IntData(value, nullable) if nullable     => DataType.StringData(s"Some(${value.toString})", true)
      case DataType.IntData(value, _)                        => DataType.StringData(value.toString, false)
      case DataType.NullData                                 => DataType.StringData("None", true)
    }

  def asString: String =
    this match {
      case s: DataType.StringData if s.nullable              => s"""Some("${s.value}")"""
      case s: DataType.StringData                            => s""""${s.value}""""
      case DataType.BooleanData(value, nullable) if nullable => s"Some(${value.toString})"
      case DataType.BooleanData(value, _)                    => value.toString
      case DataType.DoubleData(value, nullable) if nullable  => s"Some(${value.toString})"
      case DataType.DoubleData(value, _)                     => value.toString
      case DataType.IntData(value, nullable) if nullable     => s"Some(${value.toString})"
      case DataType.IntData(value, _)                        => value.toString
      case DataType.NullData                                 => "None"
    }

  def giveTypeName: String =
    this match {
      case d: DataType.StringData if d.nullable  => "Option[String]"
      case _: DataType.StringData                => "String"
      case d: DataType.BooleanData if d.nullable => "Option[Boolean]"
      case _: DataType.BooleanData               => "Boolean"
      case d: DataType.DoubleData if d.nullable  => "Option[Double]"
      case _: DataType.DoubleData                => "Double"
      case d: DataType.IntData if d.nullable     => "Option[Int]"
      case _: DataType.IntData                   => "Int"
      case DataType.NullData                     => "Option[Any]"
    }

}
object DataType {

  // Most to least specific: Boolean, Int, Double, String
  final case class BooleanData(value: Boolean, nullable: Boolean) extends DataType {
    def makeOptional: BooleanData = this.copy(nullable = true)
  }
  object BooleanData {
    def apply(value: Boolean): BooleanData = BooleanData(value, false)
  }

  final case class IntData(value: Int, nullable: Boolean) extends DataType {
    def toDoubleData: DoubleData = DoubleData(value.toDouble, nullable)
    def makeOptional: IntData    = this.copy(nullable = true)
  }
  object IntData {
    def apply(value: Int): IntData = IntData(value, false)
  }

  final case class DoubleData(value: Double, nullable: Boolean) extends DataType {
    def makeOptional: DoubleData = this.copy(nullable = true)
  }
  object DoubleData {
    def apply(value: Double): DoubleData = DoubleData(value, false)
  }

  final case class StringData(value: String, nullable: Boolean) extends DataType {
    def makeOptional: StringData = this.copy(nullable = true)
  }
  object StringData {
    def apply(value: String): StringData = StringData(value, false)
  }

  case object NullData extends DataType {
    val nullable: Boolean      = true
    def makeOptional: DataType = this
  }

  private val isBoolean: Regex = """^(true|false)$""".r
  private val isInt: Regex     = """^(\-?[0-9]+)$""".r
  private val isDouble: Regex  = """^(\-?[0-9]+?)\.([0-9]+)$""".r
  private val isNull: Regex    = """^$""".r

  def decideType: String => DataType = {
    case isBoolean(v)     => BooleanData(v.toBoolean, false)
    case isInt(v)         => IntData(v.toInt, false)
    case isDouble(v1, v2) => DoubleData(s"$v1.$v2".toDouble, false)
    case isNull(_)        => NullData
    case v                => StringData(v, false)
  }

  def sameType(a: DataType, b: DataType): Boolean =
    (a, b) match {
      case (_: DataType.StringData, _: DataType.StringData)   => true
      case (DataType.NullData, _: DataType.StringData)        => true
      case (_: DataType.StringData, DataType.NullData)        => true
      case (_: DataType.BooleanData, _: DataType.BooleanData) => true
      case (DataType.NullData, _: DataType.BooleanData)       => true
      case (_: DataType.BooleanData, DataType.NullData)       => true
      case (_: DataType.DoubleData, _: DataType.DoubleData)   => true
      case (DataType.NullData, _: DataType.DoubleData)        => true
      case (_: DataType.DoubleData, DataType.NullData)        => true
      case (_: DataType.IntData, _: DataType.IntData)         => true
      case (DataType.NullData, _: DataType.IntData)           => true
      case (_: DataType.IntData, DataType.NullData)           => true
      case _                                                  => false
    }

  def allSameType(l: List[DataType]): Boolean =
    l match {
      case Nil    => true
      case h :: t => t.forall(d => sameType(h, d))
    }

  def allNumericTypes(l: List[DataType]): Boolean =
    l.forall(d => d.isDouble || d.isInt || d.isNull)

  def hasOptionalValues(l: List[DataType]): Boolean =
    l.contains(DataType.NullData)

  def convertToBestType(l: List[DataType]): List[DataType] =
    // Cases we can manage:
    // - They're all the same! Maybe optional...
    // - Doubles and Ints, convert Ints to Doubles
    // - Fallback is that everything is a string.
    if (allSameType(l)) {
      // All the same! Great!
      l
    } else if (allNumericTypes(l)) {
      l.map {
        case v @ DataType.DoubleData(_, _) => v
        case v @ DataType.IntData(_, _)    => v.toDoubleData
        case DataType.NullData             => DataType.NullData
        case s => throw new Exception(s"Unexpected non-numeric type '$s'") // Shouldn't get here.
      }
    } else {
      // Nothing else to do, but make everything a string that isn't null.
      l.map {
        case DataType.NullData => DataType.NullData
        case d                 => d.toStringData
      }
    }

  def matchHeaderRowLength(rows: Array[Array[DataType]]): Array[Array[DataType]] =
    rows.toList match {
      case Nil =>
        rows

      case headers :: data =>
        val l = headers.length
        val res =
          headers :: data.map { r =>
            val diff = l - r.length
            if (diff > 0) {
              r ++ List.fill(diff)(DataType.NullData)
            } else {
              r
            }
          }

        res.toArray
    }

}
