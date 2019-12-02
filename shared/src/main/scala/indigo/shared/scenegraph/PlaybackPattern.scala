package indigo.shared.scenegraph

import indigo.shared.audio.Track

sealed trait PlaybackPattern
object PlaybackPattern {
  case object Silent                             extends PlaybackPattern
  final case class SingleTrackLoop(track: Track) extends PlaybackPattern
}
