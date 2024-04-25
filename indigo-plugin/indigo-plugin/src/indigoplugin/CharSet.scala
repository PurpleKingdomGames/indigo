package indigoplugin

/** Represents a set of characters used for generating fonts.
  *
  * @param characters
  *   The string containing the characters in the set.
  */
final case class CharSet(characters: String, default: Char) {

  def toCharacterCodes: Array[Int] =
    characters.toCharArray.map(_.toInt)

}

object CharSet {

  val DefaultCharacter: Char = ' '

  def fromString(characters: String, default: Char): CharSet =
    CharSet(characters, default)
  def fromString(characters: String): CharSet =
    fromString(characters + DefaultCharacter, DefaultCharacter)

  def fromSeq(chars: Seq[Char], default: Char): CharSet =
    CharSet(chars.mkString, default)
  def fromSeq(chars: Seq[Char]): CharSet =
    fromSeq(chars :+ DefaultCharacter, DefaultCharacter)

  def fromCharCodeRange(from: Int, to: Int, default: Char): CharSet =
    CharSet((from to to).map(_.toChar).mkString, default)
  def fromCharCodeRange(from: Int, to: Int): CharSet =
    CharSet(((from to to).map(_.toChar) :+ DefaultCharacter).mkString, DefaultCharacter)

  def fromCharRange(start: Char, end: Char, default: Char): CharSet =
    fromCharCodeRange(start.toInt, end.toInt, default)
  def fromCharRange(start: Char, end: Char): CharSet =
    fromCharCodeRange(start.toInt, end.toInt)

  def fromUniqueString(characters: String, default: Char): CharSet =
    CharSet(characters.distinct, default)
  def fromUniqueString(characters: String): CharSet =
    CharSet((characters + DefaultCharacter).distinct, DefaultCharacter)

  val ASCII: CharSet           = fromCharCodeRange(0, 127)
  val ExtendedASCII: CharSet   = fromCharCodeRange(0, 255)
  val AlphabeticLower: CharSet = fromCharCodeRange('a'.toInt, 'z'.toInt)
  val AlphabeticUpper: CharSet = fromCharCodeRange('A'.toInt, 'Z'.toInt)
  val Alphabetic: CharSet      = fromSeq(('a' to 'z') ++ ('A' to 'Z'))
  val Numeric: CharSet         = fromCharCodeRange('0'.toInt, '9'.toInt)
  val Alphanumeric: CharSet    = fromSeq(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))
  val Whitespace: CharSet      = fromString(" \t\n\r\f")
}
