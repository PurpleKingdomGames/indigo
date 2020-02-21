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

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AssetType")
object AssetTypeDelegateFactory {

  @JSExport("Text")
  def getText(name: String, path: String) : TextAsset =
    new TextAsset(name, path)

  @JSExport("Image")
  def getImage(name: String, path: String) : ImageAsset =
    new ImageAsset(name, path)

  @JSExport("Audio")
  def getAudio(name: String, path: String) : AudioAsset =
    new AudioAsset(name, path)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("TextAsset")
final class TextAsset(_name: String, _path: String) extends AssetTypeDelegate {

  @JSExport
  val name: String = _name
  @JSExport
  val path: String = _path

  def toInternal: AssetType =
    AssetType.Text(name, path)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("ImageAsset")
final class ImageAsset(_name: String, _path: String) extends AssetTypeDelegate {

  @JSExport
  val name: String = _name
  @JSExport
  val path: String = _path

  def toInternal: AssetType =
    AssetType.Image(name, path)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AudioAsset")
final class AudioAsset(_name: String, _path: String) extends AssetTypeDelegate {

  @JSExport
  val name: String = _name
  @JSExport
  val path: String = _path

  def toInternal: AssetType =
    AssetType.Audio(name, path)
}
