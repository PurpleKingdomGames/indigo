package indigo.platform.audio

enum SoundSwitch derives CanEqual:
  /** Stop all sounds, not only the previous same sound
    */
  case StopAll

  /** Stop only the previous same sound.
    */
  case StopPreviousSame

  /** Continue all previous sounds
    */
  case Continue
