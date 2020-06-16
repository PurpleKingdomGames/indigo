package indigoextras.subsystems

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.scenegraph._
import indigo.shared.subsystems.SubSystem
import indigoextras.subsystems.AutomataEvent._
import indigoextras.subsystems.Automata.Layer
import indigo.shared.EqualTo._
import indigo.shared.EqualTo
import indigo.shared.datatypes.RGBA
import scala.collection.mutable
import indigo.shared.FrameContext
import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds
import indigo.shared.dice.Dice
import indigo.shared.scenegraph.{SceneGraphNode, Renderable}
import indigo.shared.temporal.Signal
import indigo.shared.scenegraph.Clone

final class Automata(val poolKey: AutomataPoolKey, val automaton: Automaton, val layer: Layer, maxPoolSize: Option[Int]) extends SubSystem {
  type EventType = AutomataEvent

  val pool: mutable.ListBuffer[SpawnedAutomaton] =
    new mutable.ListBuffer()

  def liveAutomataCount: Int =
    pool.size

  def withMaxPoolSize(limit: Int): Automata =
    new Automata(poolKey, automaton, layer, Option(limit))

  val eventFilter: GlobalEvent => Option[AutomataEvent] = {
    case e: AutomataEvent =>
      Some(e)

    case FrameTick =>
      Some(AutomataEvent.Cull(poolKey))

    case _ =>
      None
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def update(frameContext: FrameContext): AutomataEvent => Outcome[Automata] = {
    case Spawn(key, position, lifeSpan, payload) if key === poolKey =>
      val spawned =
        SpawnedAutomaton(
          automaton,
          new AutomatonSeedValues(
            position,
            frameContext.gameTime.running,
            lifeSpan.getOrElse(automaton.lifespan),
            frameContext.dice.roll,
            payload
          )
        )

      maxPoolSize match {
        case None =>
          pool.append(spawned)

        case Some(limit) if pool.length < limit =>
          pool.append(spawned)

        case Some(limit) if pool.length === limit =>
          pool.drop(1).append(spawned)

        case Some(limit) =>
          pool.drop(limit - pool.length + 1).append(spawned)
      }

      Outcome(this)

    case KillAll(key) if key === poolKey =>
      pool.clear()
      Outcome(this)

    case Cull(key) if key === poolKey => // AKA: Update.
      val cullEvents = pool
        .filterNot(_.isAlive(frameContext.gameTime.running))
        .toList
        .flatMap(sa => sa.automaton.onCull(sa.seedValues))

      pool.filterInPlace(_.isAlive(frameContext.gameTime.running))

      Outcome(this).addGlobalEvents(cullEvents)

    case _ =>
      Outcome(this)
  }

  def render(frameContext: FrameContext): SceneUpdateFragment =
    layer.emptyScene(Automata.renderNoLayer(this, frameContext.gameTime))
}
object Automata {

  sealed trait Layer {
    def emptyScene(automatonUpdate: AutomatonUpdate): SceneUpdateFragment =
      this match {
        case Layer.Game =>
          SceneUpdateFragment(
            automatonUpdate.nodes,
            Nil,
            Nil,
            Nil,
            RGBA.None,
            Nil,
            automatonUpdate.events,
            SceneAudio.None,
            ScreenEffects.None,
            Nil
          )

        case Layer.Lighting =>
          SceneUpdateFragment(
            Nil,
            automatonUpdate.nodes,
            Nil,
            Nil,
            RGBA.None,
            Nil,
            automatonUpdate.events,
            SceneAudio.None,
            ScreenEffects.None,
            Nil
          )

        case Layer.UI =>
          SceneUpdateFragment(
            Nil,
            Nil,
            Nil,
            automatonUpdate.nodes,
            RGBA.None,
            Nil,
            automatonUpdate.events,
            SceneAudio.None,
            ScreenEffects.None,
            Nil
          )
      }

  }
  object Layer {
    case object Game     extends Layer
    case object Lighting extends Layer
    case object UI       extends Layer
  }

  def apply(poolKey: AutomataPoolKey, automaton: Automaton, layer: Layer): Automata =
    new Automata(poolKey, automaton, layer, None)

  private val nodes: mutable.ListBuffer[SceneGraphNode] = new mutable.ListBuffer
  private val events: mutable.ListBuffer[GlobalEvent]   = new mutable.ListBuffer

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.While", "org.wartremover.warts.NonUnitStatements"))
  def renderNoLayer(farm: Automata, gameTime: GameTime): AutomatonUpdate = {
    nodes.clear()
    events.clear()

    var i: Int     = 0
    val count: Int = farm.pool.length

    while (i < count) {
      val sa  = farm.pool(i)
      val res = sa.automaton.modifier(sa.seedValues, sa.automaton.sceneGraphNode).at(gameTime.running - sa.seedValues.createdAt)

      nodes ++= res.nodes
      events ++= res.events

      i = i + 1
    }

    AutomatonUpdate(nodes.toList, events.toList)
  }

}

sealed trait AutomataEvent extends GlobalEvent
object AutomataEvent {
  final case class Spawn(key: AutomataPoolKey, at: Point, lifeSpan: Option[Seconds], payload: Option[AutomatonPayload]) extends AutomataEvent
  object Spawn {
    def apply(key: AutomataPoolKey, at: Point): Spawn =
      Spawn(key, at, None, None)
  }
  final case class KillAll(key: AutomataPoolKey) extends AutomataEvent
  final case class Cull(key: AutomataPoolKey)    extends AutomataEvent
}

trait AutomatonPayload

final class AutomataPoolKey(val key: String) extends AnyVal {
  override def toString: String =
    s"AutomataPoolKey(key = $key)"
}
object AutomataPoolKey {

  implicit val eq: EqualTo[AutomataPoolKey] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[String]].equal(a.key, b.key)
    }

  def apply(key: String): AutomataPoolKey =
    new AutomataPoolKey(key)

  def fromDice(dice: Dice): AutomataPoolKey =
    AutomataPoolKey(dice.rollAlphaNumeric)

}

sealed trait Automaton {

  val sceneGraphNode: SceneGraphNode
  val lifespan: Seconds
  val modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate]
  val onCull: AutomatonSeedValues => List[GlobalEvent]

  def withModifier(modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate]): Automaton =
    Automaton.create(sceneGraphNode, lifespan, modifier, onCull)

  def withOnCullEvent(onCullEvent: AutomatonSeedValues => List[GlobalEvent]): Automaton =
    Automaton.create(sceneGraphNode, lifespan, modifier, onCullEvent)
}

object Automaton {

  val NoModifySignal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
    (sa, n) => {
      Signal.fixed(
        n match {
          case r: Renderable =>
            AutomatonUpdate(r.moveTo(sa.spawnedAt))

          case c: Clone =>
            AutomatonUpdate(c.withTransforms(sa.spawnedAt, c.rotation, c.scale, c.alpha, c.flipHorizontal, c.flipVertical))

          case _ =>
            AutomatonUpdate(n)
        }
      )
    }

  val NoCullEvent: AutomatonSeedValues => List[GlobalEvent] =
    _ => Nil

  def apply(SceneGraphNode: SceneGraphNode, lifespan: Seconds): Automaton =
    create(SceneGraphNode, lifespan, NoModifySignal, NoCullEvent)

  def create(
      sceneGraphNodeEntity: SceneGraphNode,
      lifeExpectancy: Seconds,
      modifierSignal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate],
      onCullEvent: AutomatonSeedValues => List[GlobalEvent]
  ): Automaton =
    new Automaton {
      val sceneGraphNode: SceneGraphNode                                             = sceneGraphNodeEntity
      val lifespan: Seconds                                                          = lifeExpectancy
      val modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] = modifierSignal
      val onCull: AutomatonSeedValues => List[GlobalEvent]                           = onCullEvent
    }

}

final class AutomatonSeedValues(
    val spawnedAt: Point,
    val createdAt: Seconds,
    val lifeSpan: Seconds,
    val randomSeed: Int,
    val payload: Option[AutomatonPayload]
) {

  /**
    * A value progressing from 0 to 1 as the automaton reaches its end.
    */
  def progression(timeAlive: Seconds): Double =
    timeAlive.toDouble / lifeSpan.toDouble

}

final class SpawnedAutomaton(val automaton: Automaton, val seedValues: AutomatonSeedValues) {
  def isAlive(currentTime: Seconds): Boolean =
    seedValues.createdAt + seedValues.lifeSpan > currentTime
}

object SpawnedAutomaton {

  def apply(automaton: Automaton, seedValues: AutomatonSeedValues): SpawnedAutomaton =
    new SpawnedAutomaton(automaton, seedValues)

}

final class AutomatonUpdate(val nodes: List[SceneGraphNode], val events: List[GlobalEvent]) {

  def |+|(other: AutomatonUpdate): AutomatonUpdate =
    AutomatonUpdate(nodes ++ other.nodes, events ++ other.events)

  def addGlobalEvents(newEvents: GlobalEvent*): AutomatonUpdate =
    addGlobalEvents(newEvents.toList)

  def addGlobalEvents(newEvents: List[GlobalEvent]): AutomatonUpdate =
    new AutomatonUpdate(nodes, events ++ newEvents)

}

object AutomatonUpdate {

  def empty: AutomatonUpdate =
    new AutomatonUpdate(Nil, Nil)

  def apply(nodes: List[SceneGraphNode], events: List[GlobalEvent]): AutomatonUpdate =
    new AutomatonUpdate(nodes, events)

  def apply(nodes: SceneGraphNode*): AutomatonUpdate =
    new AutomatonUpdate(nodes.toList, Nil)

  def apply(nodes: List[SceneGraphNode]): AutomatonUpdate =
    new AutomatonUpdate(nodes, Nil)

}
