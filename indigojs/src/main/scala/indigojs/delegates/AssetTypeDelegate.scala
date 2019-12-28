package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.AssetType

@SuppressWarnings(Array("org.wartremover.warts.Any"))
sealed trait AssetTypeDelegate {

  @JSExport
  val name: String
  @JSExport
  val path: String

  def toInternal: AssetType
}

@JSExportTopLevel("TextAsset")
final class TextAsset(val name: String, val path: String) extends AssetTypeDelegate {
  def toInternal: AssetType =
    AssetType.Text(name, path)
}

@JSExportTopLevel("ImageAsset")
final class ImageAsset(val name: String, val path: String) extends AssetTypeDelegate {
  def toInternal: AssetType =
    AssetType.Image(name, path)
}

@JSExportTopLevel("AudioAsset")
final class AudioAsset(val name: String, val path: String) extends AssetTypeDelegate {
  def toInternal: AssetType =
    AssetType.Audio(name, path)
}
