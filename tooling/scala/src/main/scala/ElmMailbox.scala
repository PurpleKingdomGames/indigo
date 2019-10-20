import scala.scalajs.js.annotation._

@JSExportTopLevel("ElmMailbox")
object ElmMailbox extends ScalaToElmBridge {

  val process: ElmMessage => ScalaMessage = {
    case LogIt(message) =>
      // A boring job in Elm is just grabbing the current time...
      // No problem in Scala!
      println(s"[${System.currentTimeMillis()}] " + message)
      Noop
  }

}
