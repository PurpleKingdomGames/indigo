package indigoexts.subsystems.automata

import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.Outcome
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.scenegraph._
import indigoexts.subsystems.SubSystem
import indigoexts.subsystems.automata.AutomataEvent._
import indigo.shared.dice.Dice

import indigo.shared.EqualTo._

import indigoexts.primitives.UpdateList

final class Automata(poolKey: AutomataPoolKey, automaton: Automaton, val paddock: UpdateList[SpawnedAutomaton]) extends SubSystem {
  type EventType = AutomataEvent

  def liveAutomataCount: Int =
    paddock.size

  val eventFilter: GlobalEvent => Option[AutomataEvent] = {
    case e: AutomataEvent =>
      Some(e)

    case FrameTick =>
      Some(AutomataEvent.Cull)

    case _ =>
      None
  }

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def update(gameTime: GameTime, dice: Dice): AutomataEvent => Outcome[Automata] = {
    case Spawn(key, position, lifeSpan, payload) if key === poolKey =>
      val spawned =
        SpawnedAutomaton(
          automaton,
          AutomatonSeedValues(
            position,
            gameTime.running,
            lifeSpan.getOrElse(automaton.lifespan),
            Millis.zero,
            dice.roll,
            payload
          )
        )

      Outcome(new Automata(poolKey, automaton, paddock.append(spawned)))

    case KillAllInPool(key) if key === poolKey =>
      Outcome(Automata(poolKey, automaton))

    case KillAll =>
      Outcome(Automata(poolKey, automaton))

    case Cull =>
      val (l, r) =
        paddock.toList.partition(_.isAlive(gameTime.running))

      Outcome(new Automata(poolKey, automaton, paddock.replaceList(l.map(_.updateDelta(gameTime.delta)))))
        .addGlobalEvents(r.toList.flatMap(sa => sa.automaton.onCull(sa.seedValues)))

    case _ =>
      Outcome(this)
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    Automata.render(this, gameTime)

  def report: String =
    "Automata farm"

}
object Automata {

  def apply(poolKey: AutomataPoolKey, automaton: Automaton): Automata =
    new Automata(poolKey, automaton, UpdateList.empty)

  def render(farm: Automata, gameTime: GameTime): SceneUpdateFragment =
    renderNoLayer(farm, gameTime).foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def renderNoLayer(farm: Automata, gameTime: GameTime): List[SceneUpdateFragment] =
    farm.paddock.toList.map { sa =>
      sa.automaton.modifier(sa.seedValues, sa.automaton.sceneGraphNode).at(gameTime.running - sa.seedValues.createdAt)
    }
}
