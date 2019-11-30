package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.Outcome

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Outcome")
final class OutcomeDelegate(val state: js.Object, val globalEvents: js.Array[GlobalEventDelegate]) {

  @JSExport
  def addEvent(event: GlobalEventDelegate): OutcomeDelegate =
    addEvents(js.Array(event))

  @JSExport
  def addEvents(events: js.Array[GlobalEventDelegate]): OutcomeDelegate =
    new OutcomeDelegate(state, globalEvents.concat(events))

  def toInternal: Outcome[js.Object] =
    new Outcome(state, globalEvents.toList)

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("OutcomeOps")
object OutcomeDelegame {

  @JSExport
  def of(state: js.Object): OutcomeDelegate =
    new OutcomeDelegate(state, new js.Array())

}
