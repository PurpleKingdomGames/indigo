package indigo.shared.events

import indigo.shared.constants.KeyCode
import indigo.shared.audio.Volume
import indigo.shared.datatypes.Point

// Events that are passed to the GlobalEventStream
trait GlobalEvent

case object FrameTick extends GlobalEvent

sealed trait MouseEvent extends GlobalEvent {
  val x: Int
  val y: Int
  def position: Point = Point(x, y)
}
object MouseEvent {
  final case class Click(x: Int, y: Int)     extends MouseEvent
  final case class MouseUp(x: Int, y: Int)   extends MouseEvent
  final case class MouseDown(x: Int, y: Int) extends MouseEvent
  final case class Move(x: Int, y: Int)      extends MouseEvent
}

sealed trait KeyboardEvent extends GlobalEvent {
  val keyCode: KeyCode
}
object KeyboardEvent {
  final case class KeyUp(keyCode: KeyCode)    extends KeyboardEvent
  final case class KeyDown(keyCode: KeyCode)  extends KeyboardEvent
  final case class KeyPress(keyCode: KeyCode) extends KeyboardEvent
}

final case class PlaySound(assetRef: String, volume: Volume) extends GlobalEvent

trait NetworkSendEvent    extends GlobalEvent
trait NetworkReceiveEvent extends GlobalEvent
