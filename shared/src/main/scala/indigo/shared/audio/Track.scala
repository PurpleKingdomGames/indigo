package indigo.shared.audio

final case class Track(assetRef: String, volume: Volume)
object Track {
  def apply(assetRef: String): Track =
    Track(assetRef, Volume.Max)
}