package indigo.platform.events

import com.github.ghik.silencer.silent

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
    JSEventReceiver.pokeBackWithFeeling(js.Dynamic.literal(name = "Bob", age = 32))
    ()
  }

}

@js.native
@JSGlobalScope
object JSEventReceiver extends js.Any {
  def pokeBack(): Int                                   = js.native
  @silent def pokeBackWithFeeling(foo: js.Dynamic): Int = js.native
}

// @js.native
// @JSGlobal("Foo")
// final class Foo(val name: String, val age: Int) extends js.Object
