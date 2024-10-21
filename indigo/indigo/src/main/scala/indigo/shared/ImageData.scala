package indigo.shared

import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetPath
import indigo.shared.assets.AssetType
import indigo.shared.assets.AssetTypePrimitive

import java.util.Base64

enum ImageType derives CanEqual:
  case JPEG, PNG, WEBP

  override def toString(): String = this match {
    case ImageType.JPEG => "image/jpeg"
    case ImageType.PNG  => "image/png"
    case ImageType.WEBP => "image/webp"
  }

final case class ImageData(name: String, size: Int, `type`: ImageType, data: Array[Byte]) {
  def toAsset: AssetTypePrimitive = AssetType.Image(AssetName(name), AssetPath(toDataUrl))

  def toDataUrl: String =
    s"data:${`type`.toString};base64,${new String(Base64.getEncoder.encode(data))}"
}
