package indigo

import indigo.gameengine.GameEngine
import org.scalajs.dom.Element
import org.scalajs.dom.document

import scala.scalajs.js.annotation.*

trait GameLauncher[StartUpData, Model, ViewModel]:

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  private var game: GameEngine[StartUpData, Model, ViewModel] = null

  protected def ready(flags: Map[String, String]): Element => GameEngine[StartUpData, Model, ViewModel]

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private val findElement: String => Element = containerId =>
    Option(document.getElementById(containerId)) match
      case Some(e) => e

      case None =>
        throw new Exception(s"Missing Element! Could not find an element with id '$containerId' on the page.")

  @JSExport
  def halt(): Unit =
    game.kill()
    ()

  @JSExport
  def launch(containerId: String): Unit =
    game = (findElement andThen ready(Map[String, String]()))(containerId)
    ()

  @JSExport
  def launch(element: Element): Unit =
    game = ready(Map[String, String]())(element)
    ()

  // JS API
  @JSExport
  def launch(containerId: String, flags: scala.scalajs.js.Dictionary[String]): Unit =
    game = (findElement andThen ready(flags.toMap))(containerId)
    ()

  @JSExport
  def launch(element: Element, flags: scala.scalajs.js.Dictionary[String]): Unit =
    game = ready(flags.toMap)(element)
    ()

  // Scala API
  def launch(containerId: String, flags: Map[String, String]): Unit =
    game = (findElement andThen ready(flags))(containerId)
    ()

  def launch(element: Element, flags: Map[String, String]): Unit =
    game = ready(flags)(element)
    ()

  def launch(containerId: String, flags: (String, String)*): Unit =
    game = (findElement andThen ready(flags.toMap))(containerId)
    ()

  def launch(element: Element, flags: (String, String)*): Unit =
    game = ready(flags.toMap)(element)
    ()
