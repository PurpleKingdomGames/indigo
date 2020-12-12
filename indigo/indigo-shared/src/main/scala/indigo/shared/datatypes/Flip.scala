package indigo.shared.datatypes

final case class Flip(horizontal: Boolean, vertical: Boolean) {

  def flipHorizontally: Flip =
    this.copy(horizontal = !horizontal)

  def flipVertically: Flip =
    this.copy(vertical = !vertical)

  def withHorizontalFlip(isFlipped: Boolean): Flip =
    this.copy(horizontal = isFlipped)

  def withVerticalFlip(isFlipped: Boolean): Flip =
    this.copy(vertical = isFlipped)

  def hash: String =
    (horizontal, vertical) match {
      case (false, false) => "00"
      case (true, false)  => "10"
      case (false, true)  => "01"
      case (true, true)   => "11"
    }
}
object Flip {
  val default: Flip =
    Flip(false, false)
}
