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
import indigo.shared.IndigoLogger

import scala.collection.mutable.{ListBuffer}

@SuppressWarnings(Array("org.wartremover.warts.Var"))
final class Automata(poolKey: AutomataPoolKey, automaton: Automaton) extends SubSystem {
  type EventType = AutomataEvent

  private var paddock: ListBuffer[SpawnedAutomaton] = new ListBuffer()

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
      val x =
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

      paddock = paddock += x

      Outcome(this)

    case Spawn(key, _, _, _) =>
      IndigoLogger.errorOnce("Attempt to spawn automata with unregistered pool key: " + key.toString)
      Outcome(this)

    case KillAllInPool(key) if key === poolKey =>
      paddock = new ListBuffer()
      Outcome(this)

    case KillAllInPool(key) =>
      IndigoLogger.errorOnce("Attempt to kill all automata with unregistered pool key: " + key.toString)
      Outcome(this)

    case KillAll =>
      paddock = new ListBuffer()
      Outcome(this)

    case Cull =>
      val (l, r) = paddock
        .partition(_.isAlive(gameTime.running))

      paddock = l.map(_.updateDelta(gameTime.delta))

      Outcome(this)
        .addGlobalEvents(r.toList.flatMap(sa => sa.automaton.onCull(sa.seedValues)))

  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    Automata.render(this, gameTime)

  def report: String =
    "Automata farm"

}
object Automata {

  def apply(poolKey: AutomataPoolKey, automaton: Automaton): Automata =
    new Automata(poolKey, automaton)

  def render(farm: Automata, gameTime: GameTime): SceneUpdateFragment =
    renderNoLayer(farm, gameTime).foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def renderNoLayer(farm: Automata, gameTime: GameTime): List[SceneUpdateFragment] =
    farm.paddock.toList.map { sa =>
      sa.automaton.modifier(sa.seedValues, sa.automaton.sceneGraphNode).at(gameTime.running - sa.seedValues.createdAt)
    }
}
