package indigo.gameengine.scenegraph.animation

sealed trait AnimationAction {
  val hash: String
}
object AnimationAction {
  case object Play extends AnimationAction {
    val hash: String = "Play"
  }

  final case class ChangeCycle(label: String) extends AnimationAction {
    val hash: String = s"ChangeCycle($label)"
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