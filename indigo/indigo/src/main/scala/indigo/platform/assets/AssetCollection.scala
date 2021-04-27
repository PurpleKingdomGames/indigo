package indigo.platform.assets

import indigo.shared.assets.AssetName

import indigo.shared.assets.AssetTag

import org.scalajs.dom
import org.scalajs.dom.html

final class AssetCollection(
    val images: List[LoadedImageAsset],
    val texts: List[LoadedTextAsset],
    val sounds: List[LoadedAudioAsset]
) {

  val count: Int =
    images.length + texts.length + sounds.length

  def |+|(other: AssetCollection): AssetCollection =
    new AssetCollection(
      images ++ other.images,
      texts ++ other.texts,
      sounds ++ other.sounds
    )

  def exists(name: AssetName): Boolean =
    images.exists(_.name == name) ||
      texts.exists(_.name == name) ||
      sounds.exists(_.name == name)

  def findImageDataByName(name: AssetName): Option[html.Image] =
    images.find(_.name == name).map(_.data)

  def findTextDataByName(name: AssetName): Option[String] =
    texts.find(_.name == name).map(_.data)

  def findAudioDataByName(name: AssetName): Option[dom.AudioBuffer] =
    sounds.find(_.name == name).map(_.data)

}

object AssetCollection {
  def empty: AssetCollection =
    new AssetCollection(Nil, Nil, Nil)
}

final case class LoadedAudioAsset(val name: AssetName, val data: dom.AudioBuffer)
final case class LoadedImageAsset(val name: AssetName, val data: html.Image, val tag: Option[AssetTag])
final case class LoadedTextAsset(val name: AssetName, val data: String)
