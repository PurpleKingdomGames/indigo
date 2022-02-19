package indigo

import indigo.gameengine.GameEngine

import scala.scalajs.js.annotation._

trait GameLauncher[StartUpData, Model, ViewModel]:

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  private var game: GameEngine[StartUpData, Model, ViewModel] = null

  protected def ready(parentElementId: String, flags: Map[String, String]): GameEngine[StartUpData, Model, ViewModel]

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  @JSExport
  def halt(): Unit =
    game.kill()
    game = null
    ()

  @JSExport
  def launch(): Unit =
    game = ready(GameLauncher.DefaultContainerId, Map[String, String]())
    ()

  @JSExport
  def launch(containerId: String): Unit =
    game = ready(containerId, Map[String, String]())
    ()

  // JS API
  @JSExport
  def launch(flags: scala.scalajs.js.Dictionary[String]): Unit =
    game = ready(GameLauncher.DefaultContainerId, flags.toMap)
    ()

  @JSExport
  def launch(containerId: String, flags: scala.scalajs.js.Dictionary[String]): Unit =
    game = ready(containerId, flags.toMap)
    ()

  // Scala API
  def launch(flags: Map[String, String]): Unit =
    game = ready(GameLauncher.DefaultContainerId, flags)
    ()

  def launch(flags: (String, String)*): Unit =
    game = ready(GameLauncher.DefaultContainerId, flags.toMap)
    ()

  def launch(containerId: String, flags: Map[String, String]): Unit =
    game = ready(containerId, flags)
    ()

  def launch(containerId: String, flags: (String, String)*): Unit =
    game = ready(containerId, flags.toMap)
    ()

object GameLauncher:
  val DefaultContainerId: String = "indigo-container"
