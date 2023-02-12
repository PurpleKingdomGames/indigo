package indigo.shared.audio

import indigo.shared.assets.AssetName

/** Represents a playable audio asset and it's current volume.
  */
final case class Track(assetName: AssetName, volume: Volume) derives CanEqual
object Track:
  def apply(assetName: AssetName): Track =
    Track(assetName, Volume.Max)
