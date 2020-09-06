package indigo.shared.animation

import indigo.shared.EqualTo

sealed trait AnimationAction {
  val hash: String

  override def toString(): String =
    hash
}
object AnimationAction {

  implicit val animationActionEqualTo: EqualTo[AnimationAction] = {
    val clEq = implicitly[EqualTo[CycleLabel]]
    val iEq  = implicitly[EqualTo[Int]]
    EqualTo.create {
      case (Play, Play)                                         => true
      case (ChangeCycle(a), ChangeCycle(b)) if clEq.equal(a, b) => true
      case (JumpToFirstFrame, JumpToFirstFrame)                 => true
      case (JumpToLastFrame, JumpToLastFrame)                   => true
      case (JumpToFrame(a), JumpToFrame(b)) if iEq.equal(a, b)  => true
      case _                                                    => false
    }
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
    val hash: String = s"JumpToFrame(${number.toString()})"
  }
}
