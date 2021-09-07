package indigo.shared.scenegraph

import indigo.shared.audio.Track

/** Scene audio can either be played on a loop, or be silenced.
  */
enum PlaybackPattern derives CanEqual:
  case Silent extends PlaybackPattern
  case SingleTrackLoop(track: Track) extends PlaybackPattern
