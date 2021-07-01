package indigo.shared.animation

enum AnimationAction derives CanEqual:
  case Play                           extends AnimationAction
  case ChangeCycle(label: CycleLabel) extends AnimationAction
  case JumpToFirstFrame               extends AnimationAction
  case JumpToLastFrame                extends AnimationAction
  case JumpToFrame(number: Int)       extends AnimationAction
