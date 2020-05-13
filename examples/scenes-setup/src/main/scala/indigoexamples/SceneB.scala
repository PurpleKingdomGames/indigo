package indigoexamples

import indigo._
import indigoexts.lenses._
import indigoexts.scenes._
import indigoexts.subsystems.SubSystem

// There is no relevant entry in the ViewModel for either scene, so we've just left it as Unit.
object SceneB extends Scene[GameModel, Unit] {
  type SceneModel     = MessageB
  type SceneViewModel = Unit

  val name: SceneName = SceneName("B")

  val sceneModelLens: Lens[GameModel, MessageB] =
    Lens(
      model => model.sceneB,
      (model, newMessage) => model.copy(sceneB = newMessage)
    )

  // Nothing to do
  val sceneViewModelLens: Lens[Unit, Unit] =
    Lens.fixed(())

  val sceneSubSystems: Set[SubSystem] =
    Set(
      HelloSubSystem("Scene SubSystem B", FontStuff.fontKey)
    )

  // Nothing to do
  def updateSceneModel(gameTime: GameTime, sceneModel: MessageB, inputState: InputState, dice: Dice): GlobalEvent => Outcome[MessageB] =
    _ => Outcome(sceneModel)

  // Nothing to do
  def updateSceneViewModel(gameTime: GameTime, sceneModel: MessageB, sceneViewModel: Unit, inputState: InputState, dice: Dice): Outcome[Unit] = Outcome(())

  // Show some text
  // When the user clicks anywhere in the screen, trigger an event to jump to the other scene.
  def updateSceneView(gameTime: GameTime, sceneModel: MessageB, sceneViewModel: Unit, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment = {
    val events: List[GlobalEvent] =
      if (inputState.mouse.wasMouseClickedWithin(Rectangle(0, 0, 550, 400))) List(SceneEvent.JumpTo(SceneA.name))
      else Nil

    val text: Text = Text(sceneModel.value, 20, 20, 1, FontStuff.fontKey)

    SceneUpdateFragment.empty
      .addGameLayerNodes(text)
      .addGlobalEvents(events)
  }
}

final case class MessageB(value: String)
