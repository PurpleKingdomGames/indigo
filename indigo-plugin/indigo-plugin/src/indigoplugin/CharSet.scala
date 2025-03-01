package indigoplugin

/** Represents a set of characters used for generating fonts.
  *
  * @param characters
  *   The string containing the characters in the set.
  */
final case class CharSet(characters: String, default: Char) {

  def toCharacterCodes: Array[Int] =
    characters.toCharArray.map(_.toInt)

  def ++(other: CharSet): CharSet =
    CharSet(characters + other.characters, default)

  def withDefault(newDefault: Char): CharSet =
    this.copy(default = newDefault)

}

object CharSet {

  val DefaultCharacter: Char = ' '

  def fromUniqueString(characters: String, default: Char): CharSet =
    CharSet((characters + default.toString()).distinct, default)
  def fromUniqueString(characters: String): CharSet =
    CharSet((characters + DefaultCharacter).distinct, DefaultCharacter)

  def fromString(characters: String, default: Char): CharSet =
    fromUniqueString(characters, default)
  def fromString(characters: String): CharSet =
    fromUniqueString(characters, DefaultCharacter)

  def fromSeq(chars: Seq[Char], default: Char): CharSet =
    fromUniqueString(chars.mkString, default)
  def fromSeq(chars: Seq[Char]): CharSet =
    fromUniqueString(chars.mkString, DefaultCharacter)

  def fromCharCodeRange(from: Int, to: Int, default: Char): CharSet =
    fromUniqueString((from to to).map(_.toChar).mkString, default)
  def fromCharCodeRange(from: Int, to: Int): CharSet =
    fromCharCodeRange(from, to, DefaultCharacter)

  def fromCharRange(start: Char, end: Char, default: Char): CharSet =
    fromCharCodeRange(start.toInt, end.toInt, default)
  def fromCharRange(start: Char, end: Char): CharSet =
    fromCharCodeRange(start.toInt, end.toInt)

  val ASCII: CharSet           = fromCharCodeRange(0, 127)
  val ASCIIPrintable: CharSet  = fromCharCodeRange(32, 126)
  val ExtendedASCII: CharSet   = fromCharCodeRange(0, 255)
  val AlphabeticLower: CharSet = fromCharCodeRange('a'.toInt, 'z'.toInt)
  val AlphabeticUpper: CharSet = fromCharCodeRange('A'.toInt, 'Z'.toInt)
  val Alphabetic: CharSet      = fromSeq(('a' to 'z') ++ ('A' to 'Z'))
  val Numeric: CharSet         = fromCharCodeRange('0'.toInt, '9'.toInt)
  val Alphanumeric: CharSet    = fromSeq(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))
  val Whitespace: CharSet      = fromString(" \t\n\r\f")
}
