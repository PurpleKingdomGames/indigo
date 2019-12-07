package indigojs.delegates

import indigo.shared.audio.Track

import scala.scalajs.js.annotation._

@JSExportTopLevel("Track")
final class TrackDelegate(val assetRef: String, val volume: VolumeDelegate) {
  def toInternal: Track =
    Track(assetRef, volume.toInternal)
}
