package indigoexts.subsystems.automata

import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.Outcome
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.scenegraph._
import indigoexts.subsystems.SubSystem
import indigoexts.subsystems.automata.AutomataEvent._
import indigo.shared.dice.Dice
import indigo.shared.datatypes.Point

import indigo.shared.EqualTo._
import indigo.shared.datatypes.BindingKey

/*
Properties of an automaton:
They have a fixed lifespan
They have a thing to render
They have procedural modifiers based on time and previous value
They can emit events
 */
final case class Automata(inventory: Map[AutomataPoolKey, Automaton], paddock: List[SpawnedAutomaton]) extends SubSystem {
  type EventType = AutomataEvent

  val eventFilter: GlobalEvent => Option[AutomataEvent] = {
    case e: AutomataEvent =>
      Some(e)

    case FrameTick =>
      Some(AutomataEvent.Cull)

    case _ =>
      None
  }

  def update(gameTime: GameTime, dice: Dice): AutomataEvent => Outcome[SubSystem] =
    Automata.update(this, gameTime, dice)

  def render(gameTime: GameTime): SceneUpdateFragment =
    Automata.render(this, gameTime)

  def report: String =
    "Automata farm"

  def add(automaton: Automaton): Automata =
    this.copy(
      inventory = inventory + (automaton.key -> automaton)
    )
}
object Automata {

  def empty: Automata =
    Automata(Map.empty[AutomataPoolKey, Automaton], Nil)

  def update(farm: Automata, gameTime: GameTime, dice: Dice): AutomataEvent => Outcome[SubSystem] = {
    case Spawn(key, pt, ls, pl) =>
      spawn(farm, gameTime, dice, key, pt, ls, pl)

    case KillAllInPool(key) =>
      killAllInPool(farm, key)

    case KillByKey(bindingKey) =>
      killByKey(farm, bindingKey)

    case KillAll =>
      killAll(farm)

    case Cull =>
      cullPaddock(farm, gameTime)
  }

  def spawn(farm: Automata, gameTime: GameTime, dice: Dice, poolKey: AutomataPoolKey, position: Point, lifeSpan: Option[Millis], payload: Option[AutomatonPayload]): Outcome[Automata] =
    Outcome(
      farm.copy(
        paddock =
          farm.paddock ++
            farm.inventory
              .get(poolKey)
              .map { k =>
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
              .toList
      )
    )

  def killAllInPool(farm: Automata, poolKey: AutomataPoolKey): Outcome[Automata] =
    Outcome(farm.copy(paddock = farm.paddock.filterNot(p => p.automaton.key === poolKey)))

  def killByKey(farm: Automata, bindingKey: BindingKey): Outcome[Automata] =
    Outcome(farm.copy(paddock = farm.paddock.filterNot(p => p.automaton.bindingKey === bindingKey)))

  def killAll(farm: Automata): Outcome[Automata] =
    Outcome(farm.copy(paddock = Nil))

  def cullPaddock(farm: Automata, gameTime: GameTime): Outcome[Automata] = {
    val (l, r) = farm.paddock
      .partition(_.isAlive(gameTime.running))

    Outcome(
      farm.copy(paddock = l.map(_.updateDelta(gameTime.delta)))
    ).addGlobalEvents(r.map(sa => sa.automaton.onCull(sa.seedValues)).collect { case Some(s) => s })
  }

  def render(farm: Automata, gameTime: GameTime): SceneUpdateFragment =
    renderNoLayer(farm, gameTime).foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def renderNoLayer(farm: Automata, gameTime: GameTime): List[SceneUpdateFragment] =
    farm.paddock.map { sa =>
      sa.automaton.modifier(sa.seedValues, sa.automaton.renderable).at(gameTime.running - sa.seedValues.createdAt)
    }
}
