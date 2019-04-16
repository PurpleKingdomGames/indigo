package indigoexts.subsystems.automata

import indigo.time.GameTime
import indigo.time.Millis
import indigo.gameengine.Outcome
import indigo.gameengine.events.{FrameTick, GlobalEvent}
import indigo.gameengine.scenegraph._
import indigo.gameengine.scenegraph.datatypes.Point
import indigoexts.subsystems.SubSystem
import indigoexts.subsystems.automata.AutomataEvent._
import indigo.dice.Dice

import indigo.shared.EqualTo._

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
    case Spawn(key, pt) =>
      Outcome(
        farm.copy(
          paddock =
            farm.paddock ++
              farm.inventory
                .get(key)
                .map { k =>
                  SpawnedAutomaton(
                    k,
                    AutomatonSeedValues(
                      pt,
                      gameTime.running,
                      k.lifespan,
                      Millis.zero,
                      dice.roll
                    )
                  )
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

  def render(farm: Automata, gameTime: GameTime): SceneUpdateFragment = {
    val f =
      (p: List[Outcome[SceneGraphNode]]) =>
        p.map(q => SceneUpdateFragment.empty.addGameLayerNodes(q.state).addViewEvents(q.globalEvents))
          .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

    f(renderNoLayer(farm, gameTime))
  }

  def renderNoLayer(farm: Automata, gameTime: GameTime): List[Outcome[SceneGraphNode]] =
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
