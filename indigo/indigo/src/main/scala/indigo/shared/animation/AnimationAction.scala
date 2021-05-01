package indigo.shared.animation

enum AnimationAction derives CanEqual:
  case Play extends AnimationAction
  case ChangeCycle(label: CycleLabel) extends AnimationAction
  case JumpToFirstFrame extends AnimationAction
  case JumpToLastFrame extends AnimationAction
  case JumpToFrame(number: Int) extends AnimationAction

object AnimationAction:
  extension (aa: AnimationAction)
    def hash: String =
      aa match
        case Play => "Play"
        case ChangeCycle(label) => s"ChangeCycle(${label})"
        case JumpToFirstFrame => "JumpToFirstFrame"
        case JumpToLastFrame => "JumpToLastFrame"
        case JumpToFrame(number) => s"JumpToFrame(${number.toString()})"
