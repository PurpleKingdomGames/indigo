package indigo.shared.scenegraph

import indigo.shared.audio.Track

enum PlaybackPattern derives CanEqual:
  case Silent                             extends PlaybackPattern
  case SingleTrackLoop(track: Track) extends PlaybackPattern

