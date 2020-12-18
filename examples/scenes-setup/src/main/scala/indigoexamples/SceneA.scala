package indigoexamples

import indigo._
import indigo.scenes._

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

  val eventFilters: EventFilters = EventFilters.Default

  val subSystems: Set[SubSystem] =
    Set(
      HelloSubSystem("Scene SubSystem A", FontStuff.fontKey)
    )

  def updateModel(context: FrameContext[StartUpData], sceneModel: MessageA): GlobalEvent => Outcome[MessageA] = {
    case SceneEvent.SceneChange(from, to, at) =>
      println(s"A: Changed scene from '${from.name}' to '${to.name}' at running time: ${at.value}")
      Outcome(sceneModel)

    case _ =>
      Outcome(sceneModel)
  }

  // Nothing to do
  def updateViewModel(context: FrameContext[StartUpData], sceneModel: MessageA, sceneViewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  // Show some text
  // When the user clicks anywhere in the screen, trigger an event to jump to the other scene.
  def present(context: FrameContext[StartUpData], sceneModel: MessageA, sceneViewModel: Unit): Outcome[SceneUpdateFragment] = {
    val events: List[GlobalEvent] =
      if (context.inputState.mouse.wasMouseClickedWithin(Rectangle(0, 0, 550, 400))) List(SceneEvent.JumpTo(SceneB.name))
      else Nil

    val text: Text = Text(sceneModel.value, 20, 20, 1, FontStuff.fontKey)

    Outcome(
      SceneUpdateFragment.empty
        .addGameLayerNodes(text)
    ).addGlobalEvents(events)
  }

}

final case class MessageA(value: String)
