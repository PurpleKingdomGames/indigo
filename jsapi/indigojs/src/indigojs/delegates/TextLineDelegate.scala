package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.TextLine

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("TextLine")
final class TextLineDelegate(val text: String, val lineBounds: RectangleDelegate) {
  def toInternal: TextLine =
    new TextLine(text, lineBounds.toInternal)
}
object TextLineDelegate {

  def fromInternal(textLine: TextLine): TextLineDelegate =
    new TextLineDelegate(textLine.text, RectangleDelegate.fromRectangle(textLine.lineBounds))

}
