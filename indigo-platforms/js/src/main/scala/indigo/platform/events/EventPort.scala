package indigo.platform.events

import scala.scalajs.js
import scala.scalajs.js.annotation._
import indigo.shared.IndigoLogger

@JSExportTopLevel("IndigoEventPort")
object EventPort {

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.NonUnitStatements"))
  @JSExport
  def poke(): Unit = {
    IndigoLogger.info("Hey! Someone poked me!")
    JSEventReceiver.pokeBack()
    ()
  }

}

@js.native
@JSGlobalScope
object JSEventReceiver extends js.Any {
  def pokeBack(): Int = js.native
}
