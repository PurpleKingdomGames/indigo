package indigo.shared.datatypes

final class Flip(val horizontal: Boolean, val vertical: Boolean) {
  
  def flipH: Flip =
    new Flip(!horizontal, vertical)

  def flipV: Flip =
    new Flip(horizontal, !vertical)

  def withFlipH(value: Boolean) =
    new Flip(value, vertical)

  def withFlipV(value: Boolean) =
    new Flip(horizontal, value)

  def hash: String =
    (horizontal, vertical) match {
      case (false, false) => "00"
      case (true, false)  => "10"
      case (false, true)  => "01"
      case (true, true)   => "11"
    }
}
object Flip {
  def apply(horizontal: Boolean, vertical: Boolean): Flip =
    new Flip(horizontal, vertical)
}
