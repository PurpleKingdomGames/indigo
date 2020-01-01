package indigojs.delegates

import indigo.shared.datatypes.Flip
import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Flip")
final class FlipDelegate(_horizontal: Boolean, _vertical: Boolean) {

  @JSExport
  val horizontal = _horizontal
  @JSExport
  val vertical = _vertical

  @JSExport
  def flipH: FlipDelegate =
    new FlipDelegate(!horizontal, vertical)

  @JSExport
  def flipV: FlipDelegate =
    new FlipDelegate(horizontal, !vertical)

  @JSExport
  def withFlipH(value: Boolean) =
    new FlipDelegate(value, vertical)

  @JSExport
  def withFlipV(value: Boolean) =
    new FlipDelegate(horizontal, value)

  def toInternal: Flip =
    Flip(horizontal, vertical)

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("FlipHelper")
@JSExportAll
object FlipDelegate {

  def None: FlipDelegate =
    new FlipDelegate(false, false)

}
