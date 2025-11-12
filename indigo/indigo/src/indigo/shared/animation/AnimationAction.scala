package indigo.shared.animation

enum AnimationAction derives CanEqual:
  case Play
  case ChangeCycle(label: CycleLabel)
  case JumpToFirstFrame
  case JumpToLastFrame
  case JumpToFrame(number: Int)
  case ScrubTo(position: Double)
