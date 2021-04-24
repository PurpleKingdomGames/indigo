package indigo.shared.scenegraph

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point

/**
  * Represents a single line of text.
  *
  * @param text
  * @param lineBounds
  */
final case class TextLine(text: String, lineBounds: Rectangle) {
  def moveTo(x: Int, y: Int): TextLine =
    moveTo(Point(x, y))
  def moveTo(newPosition: Point): TextLine =
    this.copy(lineBounds = lineBounds.moveTo(newPosition))

  def hash: String = text + lineBounds.hash
}
