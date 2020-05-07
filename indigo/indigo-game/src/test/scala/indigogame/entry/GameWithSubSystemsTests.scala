package indigogame.entry

import utest._
import indigo.shared.assets.AssetType.Text
import indigoexts.subsystems.SubSystem
import indigo.shared.events.GlobalEvent
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigoexts.subsystems.SubSystemsRegister
import indigo.shared.events.InputState

object GameWithSubSystemsTests extends TestSuite {

  import GameTestFixtures._

  val tests: Tests =
    Tests {

      "should be able to run model update across a game and it's subsytems and produce the right outcome" - {
        val updated = GameWithSubSystems.update(modelUpdate)(GameTime.zero, gameWithSubSystems, InputState.default, Dice.loaded(0))

        val outcome = updated(EventsOnlyEvent.Increment)

        outcome.state.model.text ==> ""
        outcome.state.subSystemsRegister.size ==> 1
        outcome.globalEvents.length ==> 3
        outcome.globalEvents.contains(EventsOnlyEvent.OnPerCount) ==> true
        outcome.globalEvents.contains(EventsOnlyEvent.Total(1)) ==> true
        outcome.globalEvents.contains(EventsOnlyEvent.Decrement) ==> true
      }

      "should be able to update the view model" - {
        val outcome = GameWithSubSystems.updateViewModel(viewModelUpdate)(GameTime.zero, gameWithSubSystems, 0, InputState.default, Dice.loaded(0))
        outcome.state ==> 10
        outcome.globalEvents.length ==> 1
        outcome.globalEvents.contains(EventsOnlyEvent.Increment) ==> true
      }

    }

}

object GameTestFixtures {

  val subSystem =
    EventsOnlySubSystem(0)

  val register =
    new SubSystemsRegister(List(subSystem))

  val model =
    GameModel("")

  val gameWithSubSystems =
    new GameWithSubSystems[GameModel](model, register)

  val modelUpdate: (GameTime, GameModel, InputState, Dice) => GlobalEvent => Outcome[GameModel] =
    (_, m, _, _) => _ => Outcome(m).addGlobalEvents(EventsOnlyEvent.Decrement)

  val viewModelUpdate: (GameTime, GameModel, Int, InputState, Dice) => Outcome[Int] =
    (_, _, viewModel, _, _) => Outcome(viewModel + 10).addGlobalEvents(EventsOnlyEvent.Increment)

  final case class GameModel(text: String)

  final case class EventsOnlySubSystem(count: Int) extends SubSystem {

    type EventType = EventsOnlyEvent

    val eventFilter: GlobalEvent => Option[EventsOnlyEvent] = {
      case e: EventsOnlyEvent => Some(e)
      case _                  => None
    }

    def update(gameTime: GameTime, inputState: InputState, dice: Dice): EventsOnlyEvent => Outcome[SubSystem] = {
      case EventsOnlyEvent.Increment =>
        val newCount = count + 1

        Outcome(this.copy(newCount))
          .addGlobalEvents(EventsOnlyEvent.Total(newCount))
          .addGlobalEvents(List.fill(newCount)(EventsOnlyEvent.OnPerCount))

      case EventsOnlyEvent.Decrement =>
        Outcome(this.copy(count + 1))

      case _ =>
        Outcome(this)
    }

    def render(gameTime: GameTime): SceneUpdateFragment =
      SceneUpdateFragment.empty

  }

  sealed trait EventsOnlyEvent extends GlobalEvent
  object EventsOnlyEvent {
    case object Increment              extends EventsOnlyEvent
    case object Decrement              extends EventsOnlyEvent
    final case class Total(count: Int) extends EventsOnlyEvent
    case object OnPerCount             extends EventsOnlyEvent
  }
}
