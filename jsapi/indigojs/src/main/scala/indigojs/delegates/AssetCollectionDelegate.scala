package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js

import indigo.platform.assets.AssetCollection
import indigo.shared.assets.AssetName

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportAll
final class AssetCollectionDelegate(assetCollection: AssetCollection) {
  val images: js.Array[LoadedImageAssetDelegate] =
    assetCollection.images.map(p => new LoadedImageAssetDelegate(p.name.value, p.data)).toJSArray

  val texts: js.Array[LoadedTextAssetDelegate] =
    assetCollection.texts.map(p => new LoadedTextAssetDelegate(p.name.value, p.data)).toJSArray

  val sounds: js.Array[LoadedAudioAssetDelegate] =
    assetCollection.sounds.map(p => new LoadedAudioAssetDelegate(p.name.value, p.data)).toJSArray

  def findImageDataByName(name: String): js.UndefOr[js.Object] =
    assetCollection.findImageDataByName(AssetName(name)).orUndefined

  def findTextDataByName(name: String): js.UndefOr[String] =
    assetCollection.findTextDataByName(AssetName(name)).orUndefined

  def findAudioDataByName(name: String): js.UndefOr[js.Object] =
    assetCollection.findAudioDataByName(AssetName(name)).orUndefined
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class LoadedImageAssetDelegate(_name: String, _data: js.Object) {
  @JSExport
  val name = _name
  @JSExport
  val data = _data
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class LoadedTextAssetDelegate(_name: String, _data: String) {
  @JSExport
  val name = _name
  @JSExport
  val data = _data
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class LoadedAudioAssetDelegate(_name: String, _data: js.Object) {
  @JSExport
  val name = _name
  @JSExport
  val data = _data
}
