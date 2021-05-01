package com.example.sandbox

import indigo._
import scala.scalajs.js.annotation._

import scala.util.control.NoStackTrace

/*
This is a bit of a strange example. It's an indigo game designed to crash.

At every stage, the game will immediately crash and recover. When it
recovers it will emit an event with the function name it's currently in.
When a full frame has been completed the game will finally crash by catching
the trace event called "present" (from the present method on the previous
frame), and throwing an exception that will not be handled.

You can alter where the game crashes by commenting out the `handleError`
method calls.

To see the output, open the developer tools / console in your browser, or
in electron if you used indigoRun.
*/

@JSExportTopLevel("IndigoGame")
object ErrorsExample extends IndigoDemo[BootData, StartUpData, Model, ViewModel] {

  given CanEqual[Throwable, Throwable] = CanEqual.derived

  // We're going to let all events through everywhere for logging purposes.
  def eventFilters: EventFilters =
    EventFilters.AllowAll

  def boot(flags: Map[String, String]): Outcome[BootResult[BootData]] =
    Outcome
      .raiseError(BootUpCrash)
      .logCrash {
        case BootUpCrash =>
          "[Crash] Boot Error"
      }
      .handleError {
        case BootUpCrash =>
          Outcome(BootResult(GameConfig.default, BootData()))
            .addGlobalEvents(TraceEvent("boot"))
      }

  def setup(bootData: BootData, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[StartUpData]] =
    Outcome
      .raiseError(StartUpCrash)
      .logCrash {
        case StartUpCrash =>
          "[Crash] Startup Error"
      }
      .handleError {
        case StartUpCrash =>
          Outcome(Startup.Success(StartUpData()))
            .addGlobalEvents(TraceEvent("setup"))
      }

  def initialModel(startupData: StartUpData): Outcome[Model] =
    Outcome
      .raiseError(InitialiseModelCrash)
      .logCrash {
        case InitialiseModelCrash =>
          "[Crash] Initial Model Error"
      }
      .handleError {
        case InitialiseModelCrash =>
          Outcome(Model())
            .addGlobalEvents(TraceEvent("initialModel"))
      }

  def initialViewModel(startupData: StartUpData, model: Model): Outcome[ViewModel] =
    Outcome
      .raiseError(InitialiseViewModelCrash)
      .logCrash {
        case InitialiseViewModelCrash =>
          "[Crash] Initial ViewModel Error"
      }
      .handleError {
        case InitialiseViewModelCrash =>
          Outcome(ViewModel())
            .addGlobalEvents(TraceEvent("initialViewModel"))
      }

  def updateModel(context: FrameContext[StartUpData], model: Model): GlobalEvent => Outcome[Model] = {
    case FrameTick =>
      Outcome
        .raiseError(UpdateModelCrash)
        .logCrash {
          case UpdateModelCrash =>
            "[Crash] Update Model Error"
        }
        .handleError {
          case UpdateModelCrash =>
            Outcome(model)
              .addGlobalEvents(TraceEvent("updateModel"))
        }

    case TraceEvent("present") =>
        Outcome.raiseError(new Exception("One full frame completed, time to stop."))

    case TraceEvent(origin) =>
      println("Model recieved trace event from: " + origin)
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[StartUpData], model: Model, viewModel: ViewModel): GlobalEvent => Outcome[ViewModel] = {
    case FrameTick =>
      Outcome
        .raiseError(UpdateViewModelCrash)
        .logCrash {
          case UpdateViewModelCrash =>
            "[Crash] Update ViewModel Error"
        }
        .handleError {
          case UpdateViewModelCrash =>
            Outcome(viewModel)
              .addGlobalEvents(TraceEvent("updateViewModel"))
        }

    case TraceEvent(origin) =>
      println("ViewModel recieved trace event from: " + origin)
      Outcome(viewModel)

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[StartUpData], model: Model, viewModel: ViewModel): Outcome[SceneUpdateFragment] =
    Outcome
      .raiseError(PresentViewCrash)
      .logCrash {
        case PresentViewCrash =>
          "[Crash] Presentation Error"
      }
      .handleError {
        case PresentViewCrash =>
          Outcome(SceneUpdateFragment.empty)
            .addGlobalEvents(TraceEvent("present"))
      }

}

final case class BootData()
final case class StartUpData()
final case class Model()
final case class ViewModel()

sealed trait GameErrors extends Exception with NoStackTrace derives CanEqual

case object BootUpCrash              extends GameErrors
case object StartUpCrash             extends GameErrors
case object InitialiseModelCrash     extends GameErrors
case object InitialiseViewModelCrash extends GameErrors
case object UpdateModelCrash         extends GameErrors
case object UpdateViewModelCrash     extends GameErrors
case object PresentViewCrash         extends GameErrors

final case class TraceEvent(origin: String) extends GlobalEvent
