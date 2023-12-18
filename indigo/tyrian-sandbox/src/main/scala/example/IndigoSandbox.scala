package example

import cats.effect.IO
import example.game.MyAwesomeGame
import org.scalajs.dom.document
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Logger
import tyrian.cmds.Random

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object IndigoSandbox extends TyrianApp[Msg, Model]:

  val gameDivId1: String    = "my-game-1"
  val gameDivId2: String    = "my-game-2"
  val gameId1: IndigoGameId = IndigoGameId("reverse")
  val gameId2: IndigoGameId = IndigoGameId("combine")

  def router: Location => Msg = Routing.externalOnly(Msg.NoOp, Msg.FollowLink(_))

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.Emit(Msg.StartIndigo))

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NoOp =>
      (model, Cmd.None)

    case Msg.FollowLink(href) =>
      (model, Nav.loadUrl(href))

    case Msg.NewRandomInt(i) =>
      (model.copy(randomNumber = i), Cmd.None)

    case Msg.NewContent(content) =>
      val cmds: Cmd[IO, Msg] =
        Logger.info[IO]("New content: " + content) |+|
          model.bridge.publish(gameId1, content) |+|
          model.bridge.publish(gameId2, content) |+|
          Random.int[IO].map(next => Msg.NewRandomInt(next.value))

      (model.copy(field = content), cmds)

    case Msg.Insert =>
      (model.copy(components = Counter.init :: model.components), Cmd.None)

    case Msg.Remove =>
      val cs = model.components match
        case Nil    => Nil
        case _ :: t => t

      (model.copy(components = cs), Cmd.None)

    case Msg.Modify(id, m) =>
      val cs = model.components.zipWithIndex.map { case (c, i) =>
        if i == id then Counter.update(m, c) else c
      }

      (model.copy(components = cs), Cmd.None)

    case Msg.StartIndigo =>
      val task: IO[Msg] =
        IO.delay {
          if gameDivsExist(gameDivId1, gameDivId2) then
            println("Indigo container divs ready, launching games.")
            MyAwesomeGame(model.bridge.subSystem(gameId1), true)
              .launch(
                gameDivId1,
                "width"  -> "200",
                "height" -> "200"
              )
            MyAwesomeGame(model.bridge.subSystem(gameId2), false)
              .launch(
                gameDivId2,
                "width"  -> "200",
                "height" -> "200"
              )

            Msg.NoOp
          else
            println("Indigo container divs not ready, retrying...")
            Msg.RetryIndigo
        }

      (model, Cmd.Run(task))

    case Msg.RetryIndigo =>
      (model, Cmd.emitAfterDelay(Msg.StartIndigo, 0.5.seconds))

    case Msg.IndigoReceive(msg) =>
      (model, Logger.consoleLog("(Tyrian) from indigo: " + msg))

  def view(model: Model): Html[Msg] =
    val counters = model.components.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    div(
      div(hidden(false))("Random number: " + model.randomNumber.toString),
      div(
        a(href := "/another-page")("Internal link (will be ignored)"),
        br,
        a(href := "http://tyrian.indigoengine.io/")("Tyrian website")
      ),
      div(id := gameDivId1)(),
      div(id := gameDivId2)(),
      div(
        input(placeholder := "Text to reverse", onInput(s => Msg.NewContent(s)), myStyle),
        div(myStyle)(text(model.field.reverse))
      ),
      div(elems)
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.Batch(
      model.bridge.subscribe { case msg =>
        Some(Msg.IndigoReceive(s"[Any game!] ${msg}"))
      },
      model.bridge.subscribe(gameId1) { case msg =>
        Some(Msg.IndigoReceive(s"[${gameId1.toString}] ${msg}"))
      },
      model.bridge.subscribe(gameId2) { case msg =>
        Some(Msg.IndigoReceive(s"[${gameId2.toString}] ${msg}"))
      }
    )

  private val myStyle =
    styles(
      CSS.width("100%"),
      CSS.height("40px"),
      CSS.padding("10px 0"),
      CSS.`font-size`("2em"),
      CSS.`text-align`("center")
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def gameDivsExist(id1: String, id2: String): Boolean =
    document.getElementById(id1) != null &&
      document.getElementById(id2) != null

enum Msg derives CanEqual:
  case NewContent(content: String)
  case Insert
  case Remove
  case Modify(i: Int, msg: Counter.Msg)
  case StartIndigo
  case RetryIndigo
  case IndigoReceive(msg: String)
  case NewRandomInt(i: Int)
  case FollowLink(href: String)
  case NoOp

object Counter:

  opaque type Model = Int

  def init: Model = 0

  enum Msg derives CanEqual:
    case Increment, Decrement

  def view(model: Model): Html[Msg] =
    div(
      button(onClick(Msg.Decrement))(text("-")),
      div(text(model.toString)),
      button(onClick(Msg.Increment))(text("+"))
    )

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Increment => model + 1
      case Msg.Decrement => model - 1

final case class Model(
    bridge: TyrianIndigoBridge[IO, String],
    field: String,
    components: List[Counter.Model],
    randomNumber: Int
)
object Model:
  val init: Model =
    Model(TyrianIndigoBridge(), "", Nil, 0)
