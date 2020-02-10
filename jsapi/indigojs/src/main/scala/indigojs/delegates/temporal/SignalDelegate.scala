package indigojs.delegates.temporal

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.temporal.Signal
import indigo.shared.time.Millis

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.AsInstanceOf"))
@JSExportTopLevel("Signal")
final class SignalDelegate(val f: js.Function1[Double, _]) extends AnyVal {

  private def convert(s: Signal[Any]): SignalDelegate =
    new SignalDelegate(d => s.at(Millis(d.toLong)))

  @JSExport
  def at(t: Double): Any =
    f(t)

  @JSExport
  def merge(other: SignalDelegate)(f: (js.Any, js.Any) => js.Any): SignalDelegate =
    convert(Signal.merge(this.toInternal, other.toInternal)((a, b) => f(a.asInstanceOf[js.Any], b.asInstanceOf[js.Any])))

  @JSExport
  def pipe(sf: SignalFunctionDelegate): SignalDelegate =
    sf.run(this)

  @JSExport
  def combineWith(other: SignalDelegate): SignalDelegate =
    new SignalDelegate((d: Double) => Signal.product(this.toInternal, other.toInternal).at(Millis(d.toLong)))

  @JSExport
  def clampTime(from: Double, to: Double): SignalDelegate =
    convert(Signal.clampTime(this.toInternal, Millis(from.toLong), Millis(to.toLong)))

  @JSExport
  def wrapTime(at: Double): SignalDelegate =
    convert(Signal.wrapTime(this.toInternal, Millis(at.toLong)))

  @JSExport
  def affectTime(multiplyBy: Double): SignalDelegate =
    convert(Signal.affectTime(this.toInternal, multiplyBy))

  @JSExport
  def easeIn(target: Double, divisor: Int): SignalDelegate =
    convert(Signal.easeIn(this.toInternal, Millis(target.toLong), divisor))

  @JSExport
  def easeOut(target: Double, divisor: Int): SignalDelegate =
    convert(Signal.easeOut(this.toInternal, Millis(target.toLong), divisor))

  @JSExport
  def map(f: js.Any => js.Any): SignalDelegate =
    convert(Signal.monadSignal.map(this.toInternal)(a => f(a.asInstanceOf[js.Any])))

  @JSExport
  def flatMap(f: js.Any => SignalDelegate): SignalDelegate =
    convert(Signal.monadSignal.flatMap(this.toInternal)(a => f(a.asInstanceOf[js.Any]).toInternal))

  def toInternal: Signal[Any] =
    Signal(t => f(t.value.toDouble))
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SignalHelper")
object SignalDelegate {

  private def convert(s: Signal[Any]): SignalDelegate =
    new SignalDelegate(d => s.at(Millis(d.toLong)))

  @JSExport
  val Time: SignalDelegate =
    convert(Signal(t => t.value.toDouble))

  @JSExport
  val TimeInSeconds: SignalDelegate =
    convert(Signal(_.toSeconds))

  @JSExport
  def Pulse(interval: Double): SignalDelegate =
    new SignalDelegate(d => Signal.Pulse(Millis(interval.toLong)).at(Millis(d.toLong)))

  @JSExport
  def fixed[A](a: A): SignalDelegate =
    new SignalDelegate(_ => a)

}
