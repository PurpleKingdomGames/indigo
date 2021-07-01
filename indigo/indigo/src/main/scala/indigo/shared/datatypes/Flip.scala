package indigo.shared.datatypes

final case class Flip(horizontal: Boolean, vertical: Boolean) derives CanEqual:

  def flipHorizontally: Flip =
    this.copy(horizontal = !horizontal)

  def flipVertically: Flip =
    this.copy(vertical = !vertical)

  def withHorizontalFlip(isFlipped: Boolean): Flip =
    this.copy(horizontal = isFlipped)

  def withVerticalFlip(isFlipped: Boolean): Flip =
    this.copy(vertical = isFlipped)

object Flip:
  val default: Flip =
    Flip(false, false)
