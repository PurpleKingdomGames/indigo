package indigo.shared.events

import indigo.shared.constants.Key
import indigo.shared.audio.Volume
import indigo.shared.datatypes.Point
import indigo.shared.assets.AssetName

// Events that are passed to the GlobalEventStream
trait GlobalEvent

case object FrameTick extends GlobalEvent

sealed trait InputEvent extends GlobalEvent with Product with Serializable

sealed trait MouseEvent extends InputEvent {
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

sealed trait KeyboardEvent extends InputEvent {
  val keyCode: Key
}
object KeyboardEvent {
  final case class KeyUp(keyCode: Key)   extends KeyboardEvent
  final case class KeyDown(keyCode: Key) extends KeyboardEvent
}

final case class PlaySound(assetName: AssetName, volume: Volume) extends GlobalEvent

trait NetworkSendEvent    extends GlobalEvent
trait NetworkReceiveEvent extends GlobalEvent

sealed trait StorageEvent extends GlobalEvent

object StorageEvent {
  final case class Save(key: String, data: String) extends StorageEvent
  final case class Load(key: String)               extends StorageEvent
  final case class Delete(key: String)             extends StorageEvent
  final case object DeleteAll                      extends StorageEvent
  final case class Loaded(data: String)            extends StorageEvent
}
