package indigo.shared.audio

import indigo.shared.assets.AssetName

final case class Track(assetName: AssetName, volume: Volume)
object Track {
  def apply(assetName: AssetName): Track =
    Track(assetName, Volume.Max)
}
