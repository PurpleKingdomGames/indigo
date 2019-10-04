import scala.scalajs.js.annotation._

@JSExportTopLevel("Mailbox")
object Mailbox extends ScalaToElmBridge {

  val process: ElmMessage => ScalaMessage = {
    case DoubleIt(i) =>
      Doubled(i * 2)
  }

}
