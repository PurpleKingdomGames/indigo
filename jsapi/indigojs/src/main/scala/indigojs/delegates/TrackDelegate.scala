package indigojs.delegates

import indigo.shared.audio.Track

import scala.scalajs.js.annotation._
import scala.scalajs.js

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Track")
final class TrackDelegate(_assetRef: String, _volume: js.UndefOr[VolumeDelegate]) {

  @JSExport
  val assetRef = _assetRef
  @JSExport
  val volume = _volume.toOption match {
      case Some(v) => v
      case None => VolumeDelegate.Max
  }

  def toInternal: Track =
    Track(assetRef, volume.toInternal)
}
