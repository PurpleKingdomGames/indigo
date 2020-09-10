package indigo.shared.datatypes

final case class Flip(horizontal: Boolean, vertical: Boolean) {
  
  def flipH: Flip =
    this.copy(horizontal = !horizontal)

  def flipV: Flip =
    this.copy(vertical = !vertical)

  def withFlipH(value: Boolean): Flip =
    this.copy(horizontal = value)

  def withFlipV(value: Boolean): Flip =
    this.copy(vertical = value)

  def hash: String =
    (horizontal, vertical) match {
      case (false, false) => "00"
      case (true, false)  => "10"
      case (false, true)  => "01"
      case (true, true)   => "11"
    }
}
