package indigoextras.subsystems

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.events.{FrameTick, GlobalEvent, SubSystemEvent}
import indigo.shared.scenegraph._
import indigo.shared.subsystems.SubSystem
import indigoextras.subsystems.AutomataEvent._
// import indigoextras.subsystems.Automata.Layer

import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds
import indigo.shared.dice.Dice
import indigo.shared.scenegraph.{SceneGraphNode, Renderable}
import indigo.shared.temporal.{Signal, SignalReader}
import indigo.shared.scenegraph.Clone
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.BindingKey

final case class Automata(poolKey: AutomataPoolKey, automaton: Automaton, layerKey: BindingKey, maxPoolSize: Option[Int]) extends SubSystem {
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

    // TODO: How to control what layer you end up on? Pass in layer key?
    Outcome(
      SceneUpdateFragment(Layer(layerKey, updated.nodes)),
      updated.events
    )

  }
}
object Automata {

  def apply(poolKey: AutomataPoolKey, automaton: Automaton, layerKey: BindingKey): Automata =
    Automata(poolKey, automaton, layerKey, None)

  def renderNoLayer(pool: List[SpawnedAutomaton], gameTime: GameTime): AutomatonUpdate =
    AutomatonUpdate.sequence(
      pool.map { sa =>
        sa.modifier.run((sa.seedValues, sa.sceneGraphNode)).at(gameTime.running - sa.seedValues.createdAt)
      }
    )

}

final case class AutomataState(totalSpawned: Long, pool: List[SpawnedAutomaton])

sealed trait AutomataEvent extends SubSystemEvent
object AutomataEvent {
  final case class Spawn(key: AutomataPoolKey, at: Point, lifeSpan: Option[Seconds], payload: Option[AutomatonPayload]) extends AutomataEvent
  object Spawn {
    def apply(key: AutomataPoolKey, at: Point): Spawn =
      Spawn(key, at, None, None)
  }
  final case class KillAll(key: AutomataPoolKey) extends AutomataEvent
  final case class Update(key: AutomataPoolKey)  extends AutomataEvent
}

trait AutomatonPayload

final case class AutomataPoolKey(key: String) extends AnyVal {
  override def toString: String =
    s"AutomataPoolKey(key = $key)"
}
object AutomataPoolKey {

  def fromDice(dice: Dice): AutomataPoolKey =
    AutomataPoolKey(dice.rollAlphaNumeric)

}

final case class Automaton(
    node: AutomatonNode,
    lifespan: Seconds,
    modifier: SignalReader[(AutomatonSeedValues, SceneGraphNode), AutomatonUpdate],
    onCull: AutomatonSeedValues => List[GlobalEvent]
) {

  def withModifier(newModifier: SignalReader[(AutomatonSeedValues, SceneGraphNode), AutomatonUpdate]): Automaton =
    this.copy(modifier = newModifier)

  def withOnCullEvent(onCullEvent: AutomatonSeedValues => List[GlobalEvent]): Automaton =
    this.copy(onCull = onCullEvent)
}

object Automaton {

  val NoModifySignal: SignalReader[(AutomatonSeedValues, SceneGraphNode), AutomatonUpdate] =
    SignalReader {
      case (sa, n) =>
        Signal.fixed(
          n match {
            case r: Renderable =>
              AutomatonUpdate(r.moveTo(sa.spawnedAt))

            case c: Clone =>
              AutomatonUpdate(c.withTransforms(sa.spawnedAt, c.rotation, c.scale /*, c.alpha*/, c.flipHorizontal, c.flipVertical))

            case _ =>
              AutomatonUpdate(n)
          }
        )
    }

  val NoCullEvent: AutomatonSeedValues => List[GlobalEvent] =
    _ => Nil

  def apply(node: AutomatonNode, lifespan: Seconds): Automaton =
    Automaton(node, lifespan, NoModifySignal, NoCullEvent)

}

sealed trait AutomatonNode {
  def giveNode(totalSpawned: Long, dice: Dice): SceneGraphNode
}
object AutomatonNode {

  final case class Fixed(node: SceneGraphNode) extends AutomatonNode {
    def giveNode(totalSpawned: Long, dice: Dice): SceneGraphNode =
      node
  }

  final case class OneOf(nodes: NonEmptyList[SceneGraphNode]) extends AutomatonNode {
    def giveNode(totalSpawned: Long, dice: Dice): SceneGraphNode = {
      val nodeList = nodes.toList

      nodeList(dice.rollFromZero(nodeList.length - 1))
    }
  }
  object OneOf {
    def apply(node: SceneGraphNode, nodes: SceneGraphNode*): OneOf =
      OneOf(NonEmptyList(node, nodes.toList))
  }

  final case class Cycle(nodes: NonEmptyList[SceneGraphNode]) extends AutomatonNode {
    private def correctMod(dividend: Double, divisor: Double): Int =
      (((dividend % divisor) + divisor) % divisor).toInt

    def giveNode(totalSpawned: Long, dice: Dice): SceneGraphNode = {
      val nodeList = nodes.toList

      nodeList(correctMod(totalSpawned.toDouble, nodeList.length.toDouble))
    }
  }
  object Cycle {
    def apply(node: SceneGraphNode, nodes: SceneGraphNode*): Cycle =
      Cycle(NonEmptyList(node, nodes.toList))
  }

}

final case class AutomatonSeedValues(
    spawnedAt: Point,
    createdAt: Seconds,
    lifeSpan: Seconds,
    randomSeed: Int,
    payload: Option[AutomatonPayload]
) {

  /**
    * A value progressing from 0 to 1 as the automaton reaches its end.
    */
  def progression(timeAlive: Seconds): Double =
    timeAlive.toDouble / lifeSpan.toDouble

}

final case class SpawnedAutomaton(
    sceneGraphNode: SceneGraphNode,
    modifier: SignalReader[(AutomatonSeedValues, SceneGraphNode), AutomatonUpdate],
    onCull: AutomatonSeedValues => List[GlobalEvent],
    seedValues: AutomatonSeedValues
) {
  def isAlive(currentTime: Seconds): Boolean =
    seedValues.createdAt + seedValues.lifeSpan > currentTime
}

final case class AutomatonUpdate(nodes: List[SceneGraphNode], events: List[GlobalEvent]) {

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

  def apply(nodes: SceneGraphNode*): AutomatonUpdate =
    new AutomatonUpdate(nodes.toList, Nil)

  def apply(nodes: List[SceneGraphNode]): AutomatonUpdate =
    new AutomatonUpdate(nodes, Nil)

  def sequence(l: List[AutomatonUpdate]): AutomatonUpdate =
    new AutomatonUpdate(
      nodes = l.flatMap(_.nodes),
      events = l.flatMap(_.events)
    )

}
