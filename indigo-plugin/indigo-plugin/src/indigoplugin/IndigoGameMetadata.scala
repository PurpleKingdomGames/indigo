package indigoplugin

/** Represents the various options that go into an Indigo game build.
  *
  * @param title
  *   Title of your game. Default 'Made with Indigo'.
  * @param showCursor
  *   Show the cursor? Default 'true'.
  * @param backgroundColor
  *   HTML page background color Default 'white'.
  * @param width
  *   Initial window width. Default '550'.
  * @param height
  *   Initial window height. Default '400'.
  * @param antiAliasing
  *   Smooth the rendered view? Defaults to false for pixel art.
  */
final case class IndigoGameMetadata(
    title: String,
    showCursor: Boolean,
    backgroundColor: String,
    width: Int,
    height: Int,
    antiAliasing: Boolean
) {

  /** Sets a new title for your game's window / title bar / tab */
  def withTitle(newTitle: String): IndigoGameMetadata =
    this.copy(title = newTitle)

  /** Make the cursor visible */
  def cursorVisible: IndigoGameMetadata =
    this.copy(showCursor = true)

  /** Hide the cursor */
  def cursorHidden: IndigoGameMetadata =
    this.copy(showCursor = false)

  /** Sets the background color, any valid CSS color representation acceptable, e.g. 'black' or '#000000' */
  def withBackgroundColor(cssColorValue: String): IndigoGameMetadata =
    this.copy(backgroundColor = cssColorValue)

  /** Set the background color from RGBA values */
  def withBackgroundColor(r: Double, g: Double, b: Double, a: Double): IndigoGameMetadata = {
    val convert: Double => String = d => {
      val hex = Integer.toHexString((Math.min(1, Math.max(0, d)) * 255).toInt)
      if (hex.length == 1) "0" + hex else hex
    }
    withBackgroundColor("#" + convert(r) + convert(g) + convert(b) + convert(a))
  }

  /** Set the background color from RGB values */
  def withBackgroundColor(r: Double, g: Double, b: Double): IndigoGameMetadata = {
    val convert: Double => String = d => {
      val hex = Integer.toHexString((Math.min(1, Math.max(0, d)) * 255).toInt)
      if (hex.length == 1) "0" + hex else hex
    }
    withBackgroundColor("#" + convert(r) + convert(g) + convert(b))
  }

  /** Set the window start width */
  def withWindowWidth(value: Int): IndigoGameMetadata =
    this.copy(width = value)

  /** Set the window start height */
  def withWindowHeight(value: Int): IndigoGameMetadata =
    this.copy(height = value)

  /** Set the window start width */
  def withWindowSize(w: Int, h: Int): IndigoGameMetadata =
    this.copy(width = w, height = h)

  /** Smooths the image for normal graphics (i.e. not pixel art) */
  def useAntiAliasing: IndigoGameMetadata =
    this.copy(antiAliasing = true)

  /** Does not smooth the image for crisp pixel-art. */
  def noAntiAliasing: IndigoGameMetadata =
    this.copy(antiAliasing = false)

}

object IndigoGameMetadata {

  /** The default metadata for an Indigo game. */
  val defaults: IndigoGameMetadata =
    IndigoGameMetadata(
      title = "Made with Indigo",
      showCursor = true,
      backgroundColor = "white",
      width = 550,
      height = 400,
      antiAliasing = false
    )
}
