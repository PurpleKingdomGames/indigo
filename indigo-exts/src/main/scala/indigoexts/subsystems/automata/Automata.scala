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

import scala.collection.mutable.{HashMap, ListBuffer}

/*
Properties of an automaton:
They have a fixed lifespan
They have a thing to render
They have procedural modifiers based on time and previous value
They can emit events
 */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
final class Automata() extends SubSystem {
  type EventType = AutomataEvent

  private val inventory: HashMap[AutomataPoolKey, Automaton] = new HashMap()
  private var paddock: ListBuffer[SpawnedAutomaton]          = new ListBuffer()

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

  def isRegistered(poolKey: AutomataPoolKey): Boolean =
    inventory.contains(poolKey)

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def update(gameTime: GameTime, dice: Dice): AutomataEvent => Outcome[Automata] = {
    case Spawn(poolKey, position, lifeSpan, payload) if isRegistered(poolKey) =>
      val maybeA =
        inventory.get(poolKey).map { k =>
          SpawnedAutomaton(
            k,
            AutomatonSeedValues(
              position,
              gameTime.running,
              lifeSpan.getOrElse(k.lifespan),
              Millis.zero,
              dice.roll,
              payload
            )
          )
        }

      if (maybeA.isDefined) {
        paddock = paddock += maybeA.get
      }

      Outcome(this)

    case Spawn(key, _, _, _) =>
      IndigoLogger.errorOnce("Attempt to spawn automata with unregistered pool key: " + key.toString)
      Outcome(this)

    case KillAllInPool(key) if isRegistered(key) =>
      paddock = paddock.filterNot(p => p.automaton.key === key)
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

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def add(automaton: Automaton): Automata = {
    inventory += (automaton.key -> automaton)
    this
  }

}
object Automata {

  def apply(): Automata =
    new Automata()

  def empty: Automata =
    Automata()

  // def spawn(farm: Automata, gameTime: GameTime, dice: Dice, poolKey: AutomataPoolKey, position: Point, lifeSpan: Option[Millis], payload: Option[AutomatonPayload]): Outcome[Automata] =
  //   Outcome(
  //     farm.copy(
  //       paddock =
  //         farm.paddock ++
  //           farm.inventory
  //             .get(poolKey)
  //             .map { k =>
  //               SpawnedAutomaton(
  //                 k,
  //                 AutomatonSeedValues(
  //                   position,
  //                   gameTime.running,
  //                   lifeSpan.getOrElse(k.lifespan),
  //                   Millis.zero,
  //                   dice.roll,
  //                   payload
  //                 )
  //               )
  //             }
  //             .toList
  //     )
  //   )

  // def killAllInPool(farm: Automata, poolKey: AutomataPoolKey): Outcome[Automata] =
  //   Outcome(Automata(farm.inventory, farm.paddock.filterNot(p => p.automaton.key === poolKey)))

  // def killByKey(farm: Automata, bindingKey: BindingKey): Outcome[Automata] =
  //   Outcome(Automata(farm.inventory, farm.paddock.filterNot(p => p.automaton.bindingKey === bindingKey)))

  // def killAll(farm: Automata): Outcome[Automata] =
  //   Outcome(Automata(farm.inventory, Nil))

  // def cullPaddock(farm: Automata, gameTime: GameTime): Outcome[Automata] = {
  //   val (l, r) = farm.paddock
  //     .partition(_.isAlive(gameTime.running))

  //   Outcome(
  //     Automata(farm.inventory, l.map(_.updateDelta(gameTime.delta)))
  //   ).addGlobalEvents(r.flatMap(sa => sa.automaton.onCull(sa.seedValues)))
  // }

  def render(farm: Automata, gameTime: GameTime): SceneUpdateFragment =
    renderNoLayer(farm, gameTime).foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def renderNoLayer(farm: Automata, gameTime: GameTime): List[SceneUpdateFragment] =
    farm.paddock.toList.map { sa =>
      sa.automaton.modifier(sa.seedValues, sa.automaton.renderable).at(gameTime.running - sa.seedValues.createdAt)
    }
}
