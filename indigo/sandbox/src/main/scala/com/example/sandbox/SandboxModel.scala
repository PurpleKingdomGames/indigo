package com.example.sandbox

import com.example.sandbox.scenes.ChangeValue
import com.example.sandbox.scenes.ConfettiModel
import com.example.sandbox.scenes.PathFindingModel
import com.example.sandbox.scenes.PointersModel
import indigo.*
import indigo.syntax.*
import indigoextras.ui.*
import indigoextras.ui.simple.InputFieldChange

object SandboxModel {

  private given CanEqual[Option[String], Option[String]] = CanEqual.derived

  def initialModel(startupData: SandboxStartupData): SandboxGameModel =
    SandboxGameModel(
      DudeModel(startupData.dude, DudeIdle),
      SaveLoadPhases.NotStarted,
      None,
      ConfettiModel.empty,
      PointersModel.empty,
      PathFindingModel.empty,
      Radians.zero,
      0,
      components
    )

  def components: ComponentGroup[Int] =
    ComponentGroup(BoundsMode.fixed(200, 300))
      .add(
        ComponentList(Dimensions(200, 40)) { (_: Int) =>
          (1 to 3).toBatch.map { i =>
            ComponentId("lbl" + i) -> Label[Int](
              "Custom rendered label " + i,
              (_, label) => Bounds(0, 0, 150, 10)
            ) { case (offset, label, dimensions) =>
              Outcome(
                Layer(
                  TextBox(label)
                    .withColor(RGBA.Red)
                    .moveTo(offset.unsafeToPoint)
                    .withSize(dimensions.unsafeToSize)
                )
              )
            }
          }
        }
      )
      .add(
        Label[Int](
          "Another label",
          (_, label) => Bounds(0, 0, 150, 10)
        ) { case (offset, label, dimensions) =>
          Outcome(
            Layer(
              TextBox(label)
                .withColor(RGBA.White)
                .moveTo(offset.unsafeToPoint)
                .withSize(dimensions.unsafeToSize)
            )
          )
        }
      )
      .add(
        Switch[Int, Int](BoundsType.fixed(40, 40))(
          (coords, bounds, _) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Green.mix(RGBA.Black)),
                    Stroke(1, RGBA.Green)
                  )
                  .moveTo(coords.unsafeToPoint)
              )
            ),
          (coords, bounds, _) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Red.mix(RGBA.Black)),
                    Stroke(1, RGBA.Red)
                  )
                  .moveTo(coords.unsafeToPoint)
              )
            )
        )
          .onSwitch(value => Batch(Log("Switched to: " + value)))
          .switchOn
      )
      .add(
        Button[Int](Bounds(32, 32)) { (coords, bounds, _) =>
          Outcome(
            Layer(
              Shape
                .Box(
                  bounds.unsafeToRectangle,
                  Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
                  Stroke(1, RGBA.Magenta)
                )
                .moveTo(coords.unsafeToPoint)
            )
          )
        }
          .presentDown { (coords, bounds, _) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                    Stroke(1, RGBA.Cyan)
                  )
                  .moveTo(coords.unsafeToPoint)
              )
            )
          }
          .presentOver((coords, bounds, _) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                    Stroke(1, RGBA.Yellow)
                  )
                  .moveTo(coords.unsafeToPoint)
              )
            )
          )
          .onClick(Log("Button clicked"))
          .onPress(Log("Button pressed"))
          .onRelease(Log("Button released"))
      )
      .add(
        ComponentList(Dimensions(200, 150)) { (_: Int) =>
          (1 to 3).toBatch.map { i =>
            ComponentId("radio-" + i) ->
              ComponentGroup(BoundsMode.fixed(200, 30))
                .withLayout(ComponentLayout.Horizontal(Padding.right(10)))
                .add(
                  Switch[Int, Int](BoundsType.fixed(20, 20))(
                    (coords, bounds, _) =>
                      Outcome(
                        Layer(
                          Shape
                            .Circle(
                              bounds.unsafeToRectangle.toIncircle,
                              Fill.Color(RGBA.Green.mix(RGBA.Black)),
                              Stroke(1, RGBA.Green)
                            )
                            .moveTo(coords.unsafeToPoint + Point(10))
                        )
                      ),
                    (coords, bounds, _) =>
                      Outcome(
                        Layer(
                          Shape
                            .Circle(
                              bounds.unsafeToRectangle.toIncircle,
                              Fill.Color(RGBA.Red.mix(RGBA.Black)),
                              Stroke(1, RGBA.Red)
                            )
                            .moveTo(coords.unsafeToPoint + Point(10))
                        )
                      )
                  )
                    .onSwitch { value =>
                      Batch(
                        Log("Selected: " + i),
                        ChangeValue(i)
                      )
                    }
                    .withAutoToggle { (_, ref) =>
                      if ref == i then Option(SwitchState.On) else Option(SwitchState.Off)
                    }
                )
                .add(
                  Label[Int](
                    "Radio " + i,
                    (_, label) => Bounds(0, 0, 150, 10)
                  ) { case (offset, label, dimensions) =>
                    Outcome(
                      Layer(
                        TextBox(label)
                          .withColor(RGBA.Red)
                          .moveTo(offset.unsafeToPoint)
                          .withSize(dimensions.unsafeToSize)
                      )
                    )
                  }
                )
          }
        }
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
            .addGlobalEvents(
              Save("my-save-game", "Important save data."),
              FetchKeys(0, 3)
            )

        case SaveLoadPhases.SaveIt =>
          // Then we load it back (see the loaded event capture below!)
          Outcome(state.copy(saveLoadPhase = SaveLoadPhases.LoadIt))
            .addGlobalEvents(
              Load("my-save-game"),
              Load("missing")
            )

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

    case KeyboardEvent.KeyDown(Key.ARROW_LEFT) =>
      println("left")
      Outcome(
        state.copy(
          dude = state.dude.walkLeft
        )
      )

    case KeyboardEvent.KeyDown(Key.ARROW_RIGHT) =>
      Outcome(
        state.copy(
          dude = state.dude.walkRight
        )
      )

    case KeyboardEvent.KeyDown(Key.ARROW_UP) =>
      Outcome(
        state.copy(
          dude = state.dude.walkUp
        )
      )

    case KeyboardEvent.KeyDown(Key.ARROW_DOWN) =>
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

    case Loaded("my-save-game", loadedData) =>
      Outcome(state.copy(data = loadedData))

    case Loaded(key, loadedData) =>
      println(s"Other data load attempted: $key, $loadedData")
      Outcome(state)

    case KeysFound(found) =>
      println("Keys found: " + found)
      Outcome(state)

    case _ =>
      Outcome(state)
  }

}

final case class SandboxGameModel(
    dude: DudeModel,
    saveLoadPhase: SaveLoadPhases,
    data: Option[String],
    confetti: ConfettiModel,
    pointers: PointersModel,
    pathfinding: PathFindingModel,
    rotation: Radians,
    num: Int,
    components: ComponentGroup[Int]
)

final case class DudeModel(dude: Dude, walkDirection: DudeDirection) {
  def idle: DudeModel      = this.copy(walkDirection = DudeIdle)
  def walkLeft: DudeModel  = this.copy(walkDirection = DudeLeft)
  def walkRight: DudeModel = this.copy(walkDirection = DudeRight)
  def walkUp: DudeModel    = this.copy(walkDirection = DudeUp)
  def walkDown: DudeModel  = this.copy(walkDirection = DudeDown)
}

sealed trait DudeDirection derives CanEqual {
  val cycleName: CycleLabel
}
case object DudeIdle  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("blink")      }
case object DudeLeft  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk left")  }
case object DudeRight extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk right") }
case object DudeUp    extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk up")    }
case object DudeDown  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk down")  }

// States of a state machine - could use Phantom types to force order but...
enum SaveLoadPhases derives CanEqual:
  case NotStarted, InitialClear, SaveIt, LoadIt, Complete
