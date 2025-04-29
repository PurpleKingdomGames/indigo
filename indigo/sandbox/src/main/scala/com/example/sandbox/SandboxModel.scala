package com.example.sandbox

import com.example.sandbox.scenes.ActorPhysicsSceneModel
import com.example.sandbox.scenes.ActorSceneModel
import com.example.sandbox.scenes.ChangeValue
import com.example.sandbox.scenes.ComponentUIScene2
import com.example.sandbox.scenes.ConfettiModel
import com.example.sandbox.scenes.PathFindingModel
import com.example.sandbox.scenes.PerformerPhysicsSceneModel
import com.example.sandbox.scenes.PerformerSceneModel
import com.example.sandbox.scenes.PointersModel
import com.example.sandbox.scenes.SfxComponents
import example.TestFont
import indigo.*
import indigo.syntax.*
import indigoextras.mesh.*
import indigoextras.ui.*

final case class SandboxGameModel(
    dude: DudeModel,
    saveLoadPhase: SaveLoadPhases,
    data: Option[String],
    confetti: ConfettiModel,
    pointers: PointersModel,
    pathfinding: PathFindingModel,
    rotation: Radians,
    num: Int,
    sfxComponents: ComponentGroup[Unit],
    components: ComponentGroup[Int],
    scrollPane: ScrollPane[ComponentList[Int], Int],
    button: Button[Int],
    meshData: MeshData,
    actorScene: ActorSceneModel,
    actorPhysicsScene: ActorPhysicsSceneModel,
    performerSceneModel: PerformerSceneModel,
    performerPhysicsSceneModel: PerformerPhysicsSceneModel
)

object SandboxModel {

  private given CanEqual[Option[String], Option[String]] = CanEqual.derived

  def randomPoint(dice: Dice, offset: Point): Point =
    Point(dice.rollFromZero(100), dice.rollFromZero(100)).moveBy(offset)

  def initialModel(startupData: SandboxStartupData): SandboxGameModel =
    val dice          = Dice.fromSeed(1)
    val offset        = Point(75, 75)
    val points        = List.fill(10)(randomPoint(dice, offset)).toBatch
    val superTriangle = Triangle.encompassing(points.map(_.toVertex), 10)
    val mesh          = Mesh.fromVertices(points.map(_.toVertex), superTriangle)

    SandboxGameModel(
      DudeModel(startupData.dude, DudeIdle),
      SaveLoadPhases.NotStarted,
      None,
      ConfettiModel.empty,
      PointersModel.empty,
      PathFindingModel.empty,
      Radians.zero,
      0,
      SfxComponents.components,
      components,
      ComponentUIScene2.CustomComponents.pane,
      customButton,
      MeshData(
        points,
        superTriangle,
        mesh
      ),
      ActorSceneModel.initial,
      ActorPhysicsSceneModel.initial,
      PerformerSceneModel.initial,
      PerformerPhysicsSceneModel.initial
    )

  val customButton: Button[Int] =
    Button[Int](Bounds(32, 32)) { (ctx, btn) =>
      Outcome(
        Layer(
          Shape
            .Box(
              btn.bounds.unsafeToRectangle,
              Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
              Stroke(1, RGBA.Magenta)
            )
            .moveTo(ctx.parent.coords.unsafeToPoint)
        )
      )
    }
      .presentDown { (ctx, btn) =>
        Outcome(
          Layer(
            Shape
              .Box(
                btn.bounds.unsafeToRectangle,
                Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                Stroke(1, RGBA.Cyan)
              )
              .moveTo(ctx.parent.coords.unsafeToPoint)
          )
        )
      }
      .presentOver((ctx, btn) =>
        Outcome(
          Layer(
            Shape
              .Box(
                btn.bounds.unsafeToRectangle,
                Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                Stroke(1, RGBA.Yellow)
              )
              .moveTo(ctx.parent.coords.unsafeToPoint)
          )
        )
      )
      .onClick(Log("Button clicked"))
      .onPress(Log("Button pressed"))
      .onRelease(Log("Button released"))

  private val text =
    Text("", TestFont.fontKey, SandboxAssets.testFontMaterial)
  private val textRed =
    Text("", TestFont.fontKey, SandboxAssets.testFontMaterial.withTint(RGBA.Red))

  def components: ComponentGroup[Int] =
    ComponentGroup(BoundsMode.fixed(200, 300))
      .withLayout(ComponentLayout.Horizontal(Padding(4), Overflow.Wrap))
      .add(
        ComponentList[Int, Label[Int]](Dimensions(200, 64)) { _ =>
          (1 to 3).toBatch.map { i =>
            ComponentId("lbl" + i) -> Label[Int](
              "Custom rendered label " + i,
              (ctx, label) => Bounds(ctx.services.bounds.get(textRed.withText(label)))
            ) { case (ctx, label) =>
              Outcome(
                Layer(
                  textRed
                    .withText(label.text(ctx))
                    .moveTo(ctx.parent.coords.unsafeToPoint)
                )
              )
            }
          }
        }
      )
      .add(
        Label[Int](
          "Another label",
          (ctx, label) => Bounds(ctx.services.bounds.get(text.withText(label)))
        ) { case (ctx, label) =>
          Outcome(
            Layer(
              text
                .withText(label.text(ctx))
                .moveTo(ctx.parent.coords.unsafeToPoint)
            )
          )
        }
      )
      .add(
        Switch[Int](BoundsType.fixed[Int](40, 40))(
          (context, switch) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    switch.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Green.mix(RGBA.Black)),
                    Stroke(1, RGBA.Green)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            ),
          (context, switch) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    switch.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Red.mix(RGBA.Black)),
                    Stroke(1, RGBA.Red)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
        )
          .onSwitch((ctx, switch) => Batch(Log("Switched to: " + ctx.reference)))
          .switchOn
      )
      .add(
        Button[Int](Bounds(32, 32)) { (context, button) =>
          Outcome(
            Layer(
              Shape
                .Box(
                  button.bounds.unsafeToRectangle,
                  Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
                  Stroke(1, RGBA.Magenta)
                )
                .moveTo(context.parent.coords.unsafeToPoint)
            )
          )
        }
          .presentDown { (context, button) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    button.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                    Stroke(1, RGBA.Cyan)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
          }
          .presentOver((context, button) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    button.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                    Stroke(1, RGBA.Yellow)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
          )
          .onClick(Log("Button clicked"))
          .onPress(Log("Button pressed"))
          .onRelease(Log("Button released"))
      )
      .add(
        ComponentList[Int, ComponentGroup[Int]](Dimensions(200, 64)) { _ =>
          (1 to 3).toBatch.map { i =>
            ComponentId("radio-" + i) ->
              ComponentGroup(BoundsMode.fixed(200, 30))
                .withLayout(ComponentLayout.Horizontal(Padding.right(10)))
                .add(
                  Switch[Int](BoundsType.fixed[Int](20, 20))(
                    (context, switch) =>
                      Outcome(
                        Layer(
                          Shape
                            .Circle(
                              switch.bounds.unsafeToRectangle.toIncircle,
                              Fill.Color(RGBA.Green.mix(RGBA.Black)),
                              Stroke(1, RGBA.Green)
                            )
                            .moveTo(context.parent.coords.unsafeToPoint + Point(10))
                        )
                      ),
                    (context, switch) =>
                      Outcome(
                        Layer(
                          Shape
                            .Circle(
                              switch.bounds.unsafeToRectangle.toIncircle,
                              Fill.Color(RGBA.Red.mix(RGBA.Black)),
                              Stroke(1, RGBA.Red)
                            )
                            .moveTo(context.parent.coords.unsafeToPoint + Point(10))
                        )
                      )
                  )
                    .onSwitch { (ctx, switch) =>
                      Batch(
                        Log("Selected: " + i),
                        ChangeValue(i)
                      )
                    }
                    .withAutoToggle { (ctx, _) =>
                      if ctx.reference == i then Option(SwitchState.On) else Option(SwitchState.Off)
                    }
                )
                .add(
                  Label[Int](
                    "Radio " + i,
                    (ctx, label) => Bounds(ctx.services.bounds.get(textRed.withText(label)))
                  ) { case (ctx, label) =>
                    Outcome(
                      Layer(
                        textRed
                          .withText(label.text(ctx))
                          .moveTo(ctx.parent.bounds.coords.unsafeToPoint)
                      )
                    )
                  }
                )
          }
        }
      )
      .add(
        Button[Int](Bounds(16, 16)) { (context, button) =>
          Outcome(
            Layer(
              Shape
                .Box(
                  button.bounds.unsafeToRectangle,
                  Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
                  Stroke(1, RGBA.Magenta)
                )
                .moveTo(context.parent.coords.unsafeToPoint)
            )
          )
        }
          .presentDown { (context, button) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    button.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                    Stroke(1, RGBA.Cyan)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
          }
          .presentOver((context, button) =>
            Outcome(
              Layer(
                Shape
                  .Box(
                    button.bounds.unsafeToRectangle,
                    Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                    Stroke(1, RGBA.Yellow)
                  )
                  .moveTo(context.parent.coords.unsafeToPoint)
              )
            )
          )
          .onClick(Log("Button clicked!"))
          .onPress(Log("Button pressed!"))
          .onRelease(Log("Button released!"))
          .makeDraggable
          .onDrag(Log("Dragging!"))
      )

  def updateModel(state: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] = {
    case rd @ RendererDetails(_, _, _) =>
      println(rd)
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

final case class MeshData(
    points: Batch[Point],
    superTriangle: Triangle,
    mesh: Mesh
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
