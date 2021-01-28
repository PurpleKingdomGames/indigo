package com.example.sandbox

import indigo._
import indigoextras.ui.InputFieldChange

object SandboxModel {

  def initialModel(startupData: SandboxStartupData): SandboxGameModel =
    SandboxGameModel(
      DudeModel(startupData.dude, DudeIdle),
      SaveLoadPhases.NotStarted,
      None
    )

  def updateModel(state: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] = {
    case rd @ RendererDetails(_, _, _) =>
      println(rd)
      Outcome(state)

    case InputFieldChange(key, value) =>
      println(s"Input field '${key.toString()}' changed: " + value)
      Outcome(state)

    case FrameTick =>
      state.saveLoadPhase match {
        case SaveLoadPhases.NotStarted =>
          // First we emit a delete all event
          Outcome(state.copy(saveLoadPhase = SaveLoadPhases.InitialClear))
            .addGlobalEvents(DeleteAll)

        case SaveLoadPhases.InitialClear =>
          // Then we save some data
          println("Saving data")
          Outcome(state.copy(saveLoadPhase = SaveLoadPhases.SaveIt))
            .addGlobalEvents(Save("my-save-game", "Important save data."))

        case SaveLoadPhases.SaveIt =>
          // Then we load it back (see the loaded event capture below!)
          Outcome(state.copy(saveLoadPhase = SaveLoadPhases.LoadIt))
            .addGlobalEvents(Load("my-save-game"))

        case SaveLoadPhases.LoadIt =>
          state.data match {
            case None =>
              println("...waiting for data to load")
              Outcome(state)

            case Some(loadedData) =>
              println("Data loaded: " + loadedData)
              Outcome(state.copy(saveLoadPhase = SaveLoadPhases.Complete))
          }

        case SaveLoadPhases.Complete =>
          Outcome(state)
      }

    case KeyboardEvent.KeyDown(Key.LEFT_ARROW) =>
      println("left")
      Outcome(
        state.copy(
          dude = state.dude.walkLeft
        )
      )

    case KeyboardEvent.KeyDown(Key.RIGHT_ARROW) =>
      Outcome(
        state.copy(
          dude = state.dude.walkRight
        )
      )

    case KeyboardEvent.KeyDown(Key.UP_ARROW) =>
      Outcome(
        state.copy(
          dude = state.dude.walkUp
        )
      )

    case KeyboardEvent.KeyDown(Key.DOWN_ARROW) =>
      Outcome(
        state.copy(
          dude = state.dude.walkDown
        )
      )

    // case KeyboardEvent.KeyUp(Key.KEY_F) =>
    //   println("Toggle full screen mode...")
    //   Outcome(state, List(ToggleFullScreen))

    // case KeyboardEvent.KeyUp(Key.KEY_E) =>
    //   println("Enter full screen mode...")
    //   Outcome(state, List(EnterFullScreen))

    // case KeyboardEvent.KeyUp(Key.KEY_X) =>
    //   println("Exit full screen mode...")
    //   Outcome(state, List(ExitFullScreen))

    case KeyboardEvent.KeyUp(_) =>
      Outcome(
        state.copy(
          dude = state.dude.idle
        )
      )

    case Loaded(_, loadedData) =>
      Outcome(state.copy(data = Some(loadedData)))

    case _ =>
      Outcome(state)
  }

}

final case class SandboxGameModel(dude: DudeModel, saveLoadPhase: SaveLoadPhase, data: Option[String])

final case class DudeModel(dude: Dude, walkDirection: DudeDirection) {
  def idle: DudeModel      = this.copy(walkDirection = DudeIdle)
  def walkLeft: DudeModel  = this.copy(walkDirection = DudeLeft)
  def walkRight: DudeModel = this.copy(walkDirection = DudeRight)
  def walkUp: DudeModel    = this.copy(walkDirection = DudeUp)
  def walkDown: DudeModel  = this.copy(walkDirection = DudeDown)
}

sealed trait DudeDirection {
  val cycleName: CycleLabel
}
case object DudeIdle  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("blink")      }
case object DudeLeft  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk left")  }
case object DudeRight extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk right") }
case object DudeUp    extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk up")    }
case object DudeDown  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk down")  }

// States of a state machine - could use Phantom types to force order but...
sealed trait SaveLoadPhase
object SaveLoadPhases {
  case object NotStarted   extends SaveLoadPhase
  case object InitialClear extends SaveLoadPhase
  case object SaveIt       extends SaveLoadPhase
  case object LoadIt       extends SaveLoadPhase
  case object Complete     extends SaveLoadPhase
}
