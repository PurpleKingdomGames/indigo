package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.Startup
import indigo.shared.ToReportable

sealed trait StartUpDelegate {
  def toInternal: Startup[js.Array[String], js.Object]
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("StartUp")
object StartUpDelegate {

  @JSExport
  def succeedWith(model: js.Object): StartUpSuccessDelegate =
    new StartUpSuccessDelegate(model, new js.Array(), new js.Array())

  @JSExport
  def failWith(errors: js.Array[String]): StartUpFailureDelegate =
    new StartUpFailureDelegate(errors)

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("StartUpSuccess")
final class StartUpSuccessDelegate(val model: js.Object, val animations: js.Array[AnimationDelegate], val fonts: js.Array[FontInfoDelegate]) extends StartUpDelegate {

  @JSExport
  def addAnimations(value: js.Array[AnimationDelegate]): StartUpSuccessDelegate =
    new StartUpSuccessDelegate(model, animations ++ value, fonts)

  @JSExport
  def addFonts(value: js.Array[FontInfoDelegate]): StartUpSuccessDelegate =
    new StartUpSuccessDelegate(model, animations, fonts ++ value)

  def toInternal: Startup.Success[js.Object] =
    Startup
      .Success(model)
      .addAnimations(animations.map(_.toInternal).toList)
      .addFonts(fonts.map(_.toInternal).toList)

}

@JSExportTopLevel("StartUpFailure")
final class StartUpFailureDelegate(val errors: js.Array[String]) extends StartUpDelegate {
  import StartUpFailureDelegate._

  def toInternal: Startup.Failure[js.Array[String]] =
    Startup.Failure(errors)
}
object StartUpFailureDelegate {
  implicit val toReportable: ToReportable[js.Array[String]] =
    ToReportable.createToReportable(_.toList.mkString("\n"))
}
