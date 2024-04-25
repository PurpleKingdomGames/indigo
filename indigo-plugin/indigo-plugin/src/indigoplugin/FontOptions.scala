package indigoplugin

final case class FontOptions(
    fontKey: String,
    fontSize: Int,
    charSet: CharSet,
    color: RGB,
    antiAlias: Boolean,
    maxCharactersPerLine: Int
) {

  def withFontKey(newFontKey: String): FontOptions =
    this.copy(fontKey = newFontKey)

  def withFontSize(newFontSize: Int): FontOptions =
    this.copy(fontSize = newFontSize)

  def withChars(newCharSet: CharSet): FontOptions =
    this.copy(charSet = newCharSet)

  def withColor(newColor: RGB): FontOptions =
    this.copy(color = newColor)

  def withAntiAlias(newAntiAlias: Boolean): FontOptions =
    this.copy(antiAlias = newAntiAlias)
  def useAntiAliasing: FontOptions =
    withAntiAlias(true)
  def noAntiAliasing: FontOptions =
    withAntiAlias(false)

  def withMaxCharactersPerLine(newMaxCharactersPerLine: Int): FontOptions =
    this.copy(maxCharactersPerLine = newMaxCharactersPerLine)

}

object FontOptions {

  def apply(fontKey: String, fontSize: Int): FontOptions =
    FontOptions(fontKey, fontSize, CharSet.ASCII, indigoplugin.RGB.White, false, 16)

  def apply(fontKey: String, fontSize: Int, charSet: CharSet): FontOptions =
    FontOptions(fontKey, fontSize, charSet, indigoplugin.RGB.White, false, 16)

}
