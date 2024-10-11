package indigo.shared

enum ImageType derives CanEqual:
  case JPEG, PNG, WEBP

  override def toString(): String = this match {
    case ImageType.JPEG => "image/jpeg"
    case ImageType.PNG  => "image/png"
    case ImageType.WEBP => "image/webp"
  }

final case class ImageData(size: Int, `type`: ImageType, data: Array[Byte])
