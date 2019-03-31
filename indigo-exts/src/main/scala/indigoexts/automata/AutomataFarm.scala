package indigoexts.automata

import indigo.time.GameTime
import indigo.time.Millis
import indigo.gameengine.Outcome
import indigo.gameengine.events.{FrameTick, GlobalEvent}
import indigo.gameengine.scenegraph._
import indigo.gameengine.subsystems.SubSystem
import indigoexts.automata.AutomataFarmEvent._
import indigo.dice.Dice

/*
Properties of an automaton:
They have a fixed lifespan
They have a thing to render
They have procedural modifiers based on time and previous value
They can emit events
 */
final case class AutomataFarm(inventory: Map[AutomatonPoolKey, Automaton], paddock: List[SpawnedAutomaton]) extends SubSystem {
  type Model     = AutomataFarm
  type EventType = AutomataFarmEvent

  val eventFilter: GlobalEvent => Option[AutomataFarmEvent] = {
    case e: AutomataFarmEvent =>
      Some(e)

    case FrameTick =>
      Some(AutomataFarmEvent.Cull)

    case _ =>
      None
  }

  def update(gameTime: GameTime, dice: Dice): AutomataFarmEvent => Outcome[SubSystem] =
    AutomataFarm.update(this, gameTime, dice)

  def render(gameTime: GameTime): SceneUpdateFragment =
    AutomataFarm.render(this, gameTime)

  def report: String =
    "Automata farm"

  def add(automaton: Automaton): AutomataFarm =
    this.copy(
      inventory = inventory + (automaton.key -> automaton)
    )
}
object AutomataFarm {

  def empty: AutomataFarm =
    AutomataFarm(Map.empty[AutomatonPoolKey, Automaton], Nil)

  def update(farm: AutomataFarm, gameTime: GameTime, dice: Dice): AutomataFarmEvent => Outcome[SubSystem] = {
    case Spawn(key, pt) =>
      Outcome(
        farm.copy(
          paddock =
            farm.paddock ++
              farm.inventory
                .get(key)
                .map { k =>
                  SpawnedAutomaton(k, AutomatonSeedValues(pt, gameTime.running, k.lifespan, Millis.zero, dice.roll))
                }
                .toList
        )
      )

    case KillAllInPool(key) =>
      Outcome(
        farm.copy(
          paddock = farm.paddock.filterNot(p => p.automaton.key === key)
        )
      )

    case KillByKey(bindingKey) =>
      Outcome(
        farm.copy(
          paddock = farm.paddock.filterNot(p => p.automaton.bindingKey === bindingKey)
        )
      )

    case KillAll =>
      Outcome(
        farm.copy(
          paddock = Nil
        )
      )

    case Cull =>
      Outcome(
        farm.copy(
          paddock = farm.paddock.filter(_.isAlive(gameTime.running)).map(_.updateDelta(gameTime.delta))
        )
      )
  }

  def render(farm: AutomataFarm, gameTime: GameTime): SceneUpdateFragment = {
    val f =
      (p: List[Outcome[SceneGraphNode]]) =>
        p.map(q => SceneUpdateFragment.empty.addGameLayerNodes(q.state).addViewEvents(q.globalEvents))
          .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

    f(renderNoLayer(farm, gameTime))
  }

  def renderNoLayer(farm: AutomataFarm, gameTime: GameTime): List[Outcome[SceneGraphNode]] =
    farm.paddock.map { sa =>
      sa.automaton.modifier(sa.seedValues, sa.automaton.renderable).at(gameTime.running)
    }
}
