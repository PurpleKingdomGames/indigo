package indigo

import scala.scalajs.js.annotation._

trait GameLauncher:

  protected def ready(parentElementId: String, flags: Map[String, String]): Unit

  @JSExport
  def launch(): Unit =
    ready(GameLauncher.DefaultContainerId, Map[String, String]())

  @JSExport
  def launch(containerId: String): Unit =
    ready(containerId, Map[String, String]())

  // JS API
  @JSExport
  def launch(flags: scala.scalajs.js.Dictionary[String]): Unit =
    ready(GameLauncher.DefaultContainerId, flags.toMap)

  @JSExport
  def launch(containerId: String, flags: scala.scalajs.js.Dictionary[String]): Unit =
    ready(containerId, flags.toMap)

  // Scala API
  def launch(flags: Map[String, String]): Unit =
    ready(GameLauncher.DefaultContainerId, flags)

  def launch(flags: (String, String)*): Unit =
    ready(GameLauncher.DefaultContainerId, flags.toMap)

  def launch(containerId: String, flags: Map[String, String]): Unit =
    ready(containerId, flags)

  def launch(containerId: String, flags: (String, String)*): Unit =
    ready(containerId, flags.toMap)

object GameLauncher:
  val DefaultContainerId: String = "indigo-container"
