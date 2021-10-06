package indigoextras.subsystems

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.events.{FrameTick, GlobalEvent, SubSystemEvent}
import indigo.shared.scenegraph._
import indigo.shared.subsystems.SubSystem
import indigoextras.subsystems.AutomataEvent._

import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds
import indigo.shared.dice.Dice
import indigo.shared.scenegraph.SceneNode
import indigo.shared.temporal.{Signal, SignalReader}
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.BindingKey

final case class Automata(
    poolKey: AutomataPoolKey,
    automaton: Automaton,
    layerKey: Option[BindingKey],
    maxPoolSize: Option[Int]
) extends SubSystem {
  type EventType      = AutomataEvent
  type SubSystemModel = AutomataState

  def withMaxPoolSize(limit: Int): Automata =
    Automata(poolKey, automaton, layerKey, Option(limit))

  val eventFilter: GlobalEvent => Option[AutomataEvent] = {
    case e: AutomataEvent =>
      Some(e)

    case FrameTick =>
      Some(AutomataEvent.Update(poolKey))

    case _ =>
      None
  }

  val initialModel: Outcome[AutomataState] =
    Outcome(AutomataState(0, Nil))

  private given CanEqual[Option[Int], Option[Int]] = CanEqual.derived

  def update(frameContext: SubSystemFrameContext, state: AutomataState): AutomataEvent => Outcome[AutomataState] = {
    case Spawn(key, position, lifeSpan, payload) if key == poolKey =>
      val spawned =
        SpawnedAutomaton(
          automaton.node.giveNode(state.totalSpawned, frameContext.dice),
          automaton.modifier,
          automaton.onCull,
          new AutomatonSeedValues(
            position,
            frameContext.gameTime.running,
            lifeSpan.getOrElse(automaton.lifespan),
            frameContext.dice.roll,
            payload
          )
        )

      Outcome(
        maxPoolSize match {
          case None =>
            state.copy(
              totalSpawned = state.totalSpawned + 1,
              pool = spawned :: state.pool
            )

          case Some(limit) if state.pool.length < limit =>
            state.copy(
              totalSpawned = state.totalSpawned + 1,
              pool = spawned :: state.pool
            )

          case Some(limit) if state.pool.length == limit =>
            state.copy(
              totalSpawned = state.totalSpawned + 1,
              pool = spawned :: state.pool.dropRight(1)
            )

          case Some(limit) =>
            state.copy(
              totalSpawned = state.totalSpawned + 1,
              pool = spawned :: state.pool.dropRight(limit - state.pool.length + 1)
            )
        }
      )

    case KillAll(key) if key == poolKey =>
      Outcome(state.copy(pool = Nil))

    case Update(key) if key == poolKey =>
      val cullEvents = state.pool
        .filterNot(_.isAlive(frameContext.gameTime.running))
        .toList
        .flatMap(sa => sa.onCull(sa.seedValues))

      Outcome(
        state.copy(
          pool = state.pool.filter(_.isAlive(frameContext.gameTime.running))
        )
      ).addGlobalEvents(cullEvents)

    case _ =>
      Outcome(state)
  }

  def present(frameContext: SubSystemFrameContext, state: AutomataState): Outcome[SceneUpdateFragment] = {
    val updated = Automata.renderNoLayer(state.pool, frameContext.gameTime)

    Outcome(
      SceneUpdateFragment(
        layerKey match {
          case None      => Layer(updated.nodes)
          case Some(key) => Layer(key, updated.nodes)
        }
      ),
      updated.events
    )

  }
}
object Automata {

  def apply(poolKey: AutomataPoolKey, automaton: Automaton): Automata =
    Automata(poolKey, automaton, None, None)

  def apply(poolKey: AutomataPoolKey, automaton: Automaton, layerKey: BindingKey): Automata =
    Automata(poolKey, automaton, Some(layerKey), None)

  def renderNoLayer(pool: List[SpawnedAutomaton], gameTime: GameTime): AutomatonUpdate =
    AutomatonUpdate.sequence(
      pool.map { sa =>
        sa.modifier.run((sa.seedValues, sa.sceneGraphNode)).at(gameTime.running - sa.seedValues.createdAt)
      }
    )

}

final case class AutomataState(totalSpawned: Long, pool: List[SpawnedAutomaton])

sealed trait AutomataEvent extends SubSystemEvent derives CanEqual
object AutomataEvent {
  final case class Spawn(key: AutomataPoolKey, at: Point, lifeSpan: Option[Seconds], payload: Option[AutomatonPayload])
      extends AutomataEvent
  object Spawn {
    def apply(key: AutomataPoolKey, at: Point): Spawn =
      Spawn(key, at, None, None)
  }
  final case class KillAll(key: AutomataPoolKey) extends AutomataEvent
  final case class Update(key: AutomataPoolKey)  extends AutomataEvent
}

trait AutomatonPayload

opaque type AutomataPoolKey = String
object AutomataPoolKey:
  inline def apply(key: String): AutomataPoolKey   = key
  inline def fromDice(dice: Dice): AutomataPoolKey = dice.rollAlphaNumeric

  given CanEqual[AutomataPoolKey, AutomataPoolKey]                 = CanEqual.derived
  given CanEqual[Option[AutomataPoolKey], Option[AutomataPoolKey]] = CanEqual.derived

final case class Automaton(
    node: AutomatonNode,
    lifespan: Seconds,
    modifier: SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate],
    onCull: AutomatonSeedValues => List[GlobalEvent]
) {

  def withModifier(newModifier: SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate]): Automaton =
    this.copy(modifier = newModifier)

  def withOnCullEvent(onCullEvent: AutomatonSeedValues => List[GlobalEvent]): Automaton =
    this.copy(onCull = onCullEvent)
}

object Automaton {

  def NoOpModifier: SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate] =
    SignalReader { case (_, n) =>
      Signal.fixed(AutomatonUpdate(n))
    }

  def FixedModifier(
      transform: (AutomatonSeedValues, SceneNode) => SceneNode
  ): SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate] =
    SignalReader { case (sa, n) =>
      Signal.fixed(AutomatonUpdate(transform(sa, n)))
    }

  val NoCullEvent: AutomatonSeedValues => List[GlobalEvent] =
    _ => Nil

  def apply(node: AutomatonNode, lifespan: Seconds): Automaton =
    Automaton(node, lifespan, NoOpModifier, NoCullEvent)

  def apply(node: AutomatonNode, lifespan: Seconds, placeModifier: (Point, SceneNode) => SceneNode): Automaton =
    Automaton(node, lifespan, FixedModifier((sa, n) => placeModifier(sa.spawnedAt, n)), NoCullEvent)

}

sealed trait AutomatonNode derives CanEqual {
  def giveNode(totalSpawned: Long, dice: Dice): SceneNode
}
object AutomatonNode {

  final case class Fixed(node: SceneNode) extends AutomatonNode {
    def giveNode(totalSpawned: Long, dice: Dice): SceneNode =
      node
  }

  final case class OneOf(nodes: NonEmptyList[SceneNode]) extends AutomatonNode {
    def giveNode(totalSpawned: Long, dice: Dice): SceneNode = {
      val nodeList = nodes.toList

      nodeList(dice.rollFromZero(nodeList.length - 1))
    }
  }
  object OneOf {
    def apply(node: SceneNode, nodes: SceneNode*): OneOf =
      OneOf(NonEmptyList(node, nodes.toList))
  }

  final case class Cycle(nodes: NonEmptyList[SceneNode]) extends AutomatonNode {
    private def correctMod(dividend: Double, divisor: Double): Int =
      (((dividend % divisor) + divisor) % divisor).toInt

    def giveNode(totalSpawned: Long, dice: Dice): SceneNode = {
      val nodeList = nodes.toList

      nodeList(correctMod(totalSpawned.toDouble, nodeList.length.toDouble))
    }
  }
  object Cycle {
    def apply(node: SceneNode, nodes: SceneNode*): Cycle =
      Cycle(NonEmptyList(node, nodes.toList))
  }

}

final case class AutomatonSeedValues(
    spawnedAt: Point,
    createdAt: Seconds,
    lifeSpan: Seconds,
    randomSeed: Int,
    payload: Option[AutomatonPayload]
) derives CanEqual {

  /** A value progressing from 0 to 1 as the automaton reaches its end.
    */
  def progression(timeAlive: Seconds): Double =
    timeAlive.toDouble / lifeSpan.toDouble

}

final case class SpawnedAutomaton(
    sceneGraphNode: SceneNode,
    modifier: SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate],
    onCull: AutomatonSeedValues => List[GlobalEvent],
    seedValues: AutomatonSeedValues
) derives CanEqual {
  def isAlive(currentTime: Seconds): Boolean =
    seedValues.createdAt + seedValues.lifeSpan > currentTime
}

final case class AutomatonUpdate(nodes: List[SceneNode], events: List[GlobalEvent]) derives CanEqual {

  def |+|(other: AutomatonUpdate): AutomatonUpdate =
    AutomatonUpdate(nodes ++ other.nodes, events ++ other.events)

  def addGlobalEvents(newEvents: GlobalEvent*): AutomatonUpdate =
    addGlobalEvents(newEvents.toList)

  def addGlobalEvents(newEvents: List[GlobalEvent]): AutomatonUpdate =
    AutomatonUpdate(nodes, events ++ newEvents)

}

object AutomatonUpdate {

  def empty: AutomatonUpdate =
    new AutomatonUpdate(Nil, Nil)

  def apply(nodes: SceneNode*): AutomatonUpdate =
    new AutomatonUpdate(nodes.toList, Nil)

  def apply(nodes: List[SceneNode]): AutomatonUpdate =
    new AutomatonUpdate(nodes, Nil)

  def sequence(l: List[AutomatonUpdate]): AutomatonUpdate =
    new AutomatonUpdate(
      nodes = l.flatMap(_.nodes),
      events = l.flatMap(_.events)
    )

}
