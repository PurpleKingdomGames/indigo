package indigojs.delegates

import indigo.shared.audio.Volume

import scala.scalajs.js.annotation._

final class VolumeDelegate(val amount: Double) {
  def toInternal: Volume =
    Volume(amount)
}

object VolumeDelegate {
  val Min: VolumeDelegate = new VolumeDelegate(0)
  val Max: VolumeDelegate = new VolumeDelegate(1)
}
