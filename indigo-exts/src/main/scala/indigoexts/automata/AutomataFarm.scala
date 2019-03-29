package indigoexts.automata

import indigo.gameengine.GameTime
import indigo.gameengine.GameTime.Millis
import indigo.gameengine.Outcome
import indigo.gameengine.events.{FrameTick, GlobalEvent}
import indigo.gameengine.scenegraph._
import indigo.gameengine.scenegraph.datatypes.Point
import indigo.gameengine.subsystems.SubSystem
import indigoexts.automata.AutomataEvent._

import scala.util.Random

/*
Properties of an automaton:
They have a fixed lifespan
They have a thing to render
They have procedural modifiers based on time and previous value
They can emit events
 */
final case class AutomataFarm(inventory: Map[AutomataPoolKey, Automaton], paddock: List[SpawnedAutomaton]) extends SubSystem {
  type Model     = AutomataFarm
  type EventType = AutomataEvent

  val eventFilter: GlobalEvent => Option[AutomataEvent] = {
    case e: AutomataEvent =>
      Some(e)

    case FrameTick =>
      Some(AutomataEvent.Cull)

    case _ =>
      None
  }

  def update(gameTime: GameTime): AutomataEvent => Outcome[SubSystem] =
    AutomataFarm.update(this, gameTime)

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
    AutomataFarm(Map.empty[AutomataPoolKey, Automaton], Nil)

  def update(farm: AutomataFarm, gameTime: GameTime): AutomataEvent => Outcome[SubSystem] = {
    case Spawn(key, pt) =>
      Outcome(
        farm.copy(
          paddock =
            farm.paddock ++
              farm.inventory
                .get(key)
                .map { k =>
                  SpawnedAutomaton(k, AutomatonSeedValues(pt, gameTime.running, k.lifespan, Millis.zero, Random.nextInt()))
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

final case class SpawnedAutomaton(automaton: Automaton, seedValues: AutomatonSeedValues) {
  def isAlive(currentTime: Millis): Boolean =
    seedValues.createdAt + automaton.lifespan > currentTime

  def updateDelta(frameDelta: Millis): SpawnedAutomaton =
    this.copy(seedValues = seedValues.copy(timeAliveDelta = seedValues.timeAliveDelta + frameDelta))
}

final case class AutomatonSeedValues(spawnedAt: Point, createdAt: Millis, lifeSpan: Millis, timeAliveDelta: Millis, randomSeed: Int)
