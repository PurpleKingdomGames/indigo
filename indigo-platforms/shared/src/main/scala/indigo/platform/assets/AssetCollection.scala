package indigo.platform.assets

import indigo.shared.EqualTo._

final class AssetCollection(
    val images: List[LoadedImageAsset],
    val texts: List[LoadedTextAsset],
    val sounds: List[LoadedAudioAsset]
) {

  def findImageDataByName(name: AssetName): Option[AssetDataFormats.ImageDataFormat] =
    images.find(_.name === name).map(_.data)

  def findTextDataByName(name: AssetName): Option[AssetDataFormats.TextDataFormat] =
    texts.find(_.name === name).map(_.data)

  def findAudioDataByName(name: AssetName): Option[AssetDataFormats.AudioDataFormat] =
    sounds.find(_.name === name).map(_.data)

}