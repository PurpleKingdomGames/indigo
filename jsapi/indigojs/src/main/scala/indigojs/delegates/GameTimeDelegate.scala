package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.time.GameTime

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class GameTimeDelegate(gameTime: GameTime) {

  @JSExport
  val running: Double = gameTime.running.toDouble

  @JSExport
  val delta: Double = gameTime.delta.toDouble

  @JSExport
  val targetFPS: Int = gameTime.targetFPS.value
}
