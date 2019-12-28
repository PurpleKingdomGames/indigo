package indigojs.delegates

import indigo.shared.audio.Volume

import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Volume")
final class VolumeDelegate(_amount: Double) {

  @JSExport
  val amount = _amount

  def toInternal: Volume =
    Volume(amount)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("VolumeHelper")
@JSExportAll
object VolumeDelegate {
  val Min: VolumeDelegate = new VolumeDelegate(0)
  val Max: VolumeDelegate = new VolumeDelegate(1)
}
