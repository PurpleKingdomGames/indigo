package indigojs.delegates

import indigo.shared.audio.Track

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.assets.AssetName

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Track")
final class TrackDelegate(_assetName: String, _volume: js.UndefOr[VolumeDelegate]) {

  @JSExport
  val assetName = _assetName
  @JSExport
  val volume = _volume.toOption match {
      case Some(v) => v
      case None => VolumeDelegate.Max
  }

  def toInternal: Track =
    Track(AssetName(assetName), volume.toInternal)
}
