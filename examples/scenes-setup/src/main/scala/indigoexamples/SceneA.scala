package indigoexamples

import indigo.*
import indigo.scenes.*

// There is no relevant entry in the ViewModel for either scene, so we've just left it as Unit.
object SceneA extends Scene[StartUpData, GameModel, Unit] {
  type SceneModel     = MessageA
  type SceneViewModel = Unit

  val name: SceneName = SceneName("A")

  val modelLens: Lens[GameModel, MessageA] =
    Lens(
      model => model.sceneA,
      (model, newMessage) => model.copy(sceneA = newMessage)
    )

  // Nothing to do
  val viewModelLens: Lens[Unit, Unit] =
    Lens.fixed(())

  val eventFilters: EventFilters = EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set(
      HelloSubSystem("Scene SubSystem A", FontStuff.fontKey)
    )

  def updateModel(context: SceneContext[StartUpData], sceneModel: MessageA): GlobalEvent => Outcome[MessageA] = {
    case SceneEvent.SceneChange(from, to, at) =>
      println(s"A: Changed scene from '${from}' to '${to}' at running time: ${at}")
      Outcome(sceneModel)

    case _ =>
      Outcome(sceneModel)
  }

  // Nothing to do
  def updateViewModel(context: SceneContext[StartUpData], sceneModel: MessageA, sceneViewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  // Show some text
  // When the user clicks anywhere in the screen, trigger an event to jump to the other scene.
  def present(context: SceneContext[StartUpData], sceneModel: MessageA, sceneViewModel: Unit): Outcome[SceneUpdateFragment] = {
    val events: Batch[GlobalEvent] =
      if (context.inputState.mouse.wasMouseClickedWithin(Rectangle(0, 0, 550, 400))) Batch(SceneEvent.JumpTo(SceneB.name))
      else Batch.empty

    val text: Text[_] = Text(sceneModel.value, 20, 20, 1, FontStuff.fontKey, FontStuff.fontMaterial)

    Outcome(
      SceneUpdateFragment(text)
    ).addGlobalEvents(events)
  }

}

final case class MessageA(value: String)
