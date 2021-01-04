package indigo

import scala.scalajs.js.annotation._

trait GameLauncher {

  protected def ready(flags: Map[String, String]): Unit

  @JSExport
  def launch(): Unit =
    ready(Map[String, String]())

  @JSExport
  def launch(flags: scala.scalajs.js.Dictionary[String]): Unit =
    ready(flags.toMap)

}
