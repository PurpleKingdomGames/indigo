package indigojs.delegates

import indigo.shared.audio.Track

import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Track")
final class TrackDelegate(_assetRef: String, _volume: VolumeDelegate) {

  @JSExport
  val assetRef = _assetRef
  @JSExport
  val volume = _volume

  def toInternal: Track =
    Track(assetRef, volume.toInternal)
}
