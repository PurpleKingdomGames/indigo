package indigo.shared.animation

import indigo.shared.{AsString, EqualTo}

sealed trait AnimationAction {
  val hash: String
}
object AnimationAction {

  implicit def animationActionEqualTo(implicit clEq: EqualTo[CycleLabel], iEq: EqualTo[Int]): EqualTo[AnimationAction] =
    EqualTo.create {
      case (Play, Play)                                         => true
      case (ChangeCycle(a), ChangeCycle(b)) if clEq.equal(a, b) => true
      case (JumpToFirstFrame, JumpToFirstFrame)                 => true
      case (JumpToLastFrame, JumpToLastFrame)                   => true
      case (JumpToFrame(a), JumpToFrame(b)) if iEq.equal(a, b)  => true
      case _                                                    => false
    }

  implicit val animationActionAsString: AsString[AnimationAction] =
    AsString.create { action =>
      action.hash
    }

  case object Play extends AnimationAction {
    val hash: String = "Play"
  }

  final case class ChangeCycle(label: CycleLabel) extends AnimationAction {
    val hash: String = s"ChangeCycle(${label.value})"
  }

  case object JumpToFirstFrame extends AnimationAction {
    val hash: String = "JumpToFirstFrame"
  }

  case object JumpToLastFrame extends AnimationAction {
    val hash: String = "JumpToLastFrame"
  }

  final case class JumpToFrame(number: Int) extends AnimationAction {
    val hash: String = s"JumpToFrame($number)"
  }
}
