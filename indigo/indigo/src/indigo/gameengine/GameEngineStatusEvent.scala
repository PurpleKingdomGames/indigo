package indigo.gameengine

import org.scalajs.dom.CustomEvent
import org.scalajs.dom.CustomEventInit
import org.scalajs.dom.Element

import scala.annotation.nowarn
import scala.scalajs.js

enum GameEngineStatusEvent derives CanEqual:
  case Initiated
  case Loaded(firstLoad: Boolean)
  case Loading(steps: Int, complete: Int, label: String, firstLoad: Boolean)
  case Error(message: String, stackTrace: String)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def dispatch(e: Element): Unit =
    val namespace: String = "indigoengine"
    val (eventType, detail) = this match {
      case Initiated         => ("initiated", null)
      case Loaded(firstLoad) => (if firstLoad then "loaded" else "reloaded", null)
      case Loading(steps, complete, label, firstLoad) =>
        (
          if firstLoad then "loading" else "reloading",
          js.Dynamic.literal(
            progress = complete.toDouble / steps.toDouble,
            steps = steps,
            complete = complete,
            label = label
          )
        )
      case Error(msg, stackTrace) => ("error", js.Dynamic.literal(message = msg, stackTrace = stackTrace))
    }

    @nowarn("msg=unused")
    val event = e.dispatchEvent(
      new CustomEvent(
        s"${namespace}.${eventType}",
        js.Dynamic
          .literal(
            bubbles = true,
            cancelable = true,
            detail = detail
          )
          .asInstanceOf[CustomEventInit]
      )
    )

    ()
