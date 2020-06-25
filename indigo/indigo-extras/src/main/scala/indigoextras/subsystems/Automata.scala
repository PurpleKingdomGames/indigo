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
import indigo.shared.FrameContext
import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds
import indigo.shared.dice.Dice
import indigo.shared.scenegraph.{SceneGraphNode, Renderable}
import indigo.shared.temporal.Signal
import indigo.shared.scenegraph.Clone

final class Automata(val poolKey: AutomataPoolKey, val automaton: Automaton, val layer: Layer, maxPoolSize: Option[Int]) extends SubSystem {
  type EventType      = AutomataEvent
  type SubSystemModel = List[SpawnedAutomaton]

  def withMaxPoolSize(limit: Int): Automata =
    new Automata(poolKey, automaton, layer, Option(limit))

  val eventFilter: GlobalEvent => Option[AutomataEvent] = {
    case e: AutomataEvent =>
      Some(e)

    case FrameTick =>
      Some(AutomataEvent.Update(poolKey))

    case _ =>
      None
  }

  val initialModel: List[SpawnedAutomaton] =
    Nil

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def update(frameContext: FrameContext, pool: List[SpawnedAutomaton]): AutomataEvent => Outcome[List[SpawnedAutomaton]] = {
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

      Outcome(
        maxPoolSize match {
          case None =>
            spawned :: pool

          case Some(limit) if pool.length < limit =>
            spawned :: pool

          case Some(limit) if pool.length === limit =>
            spawned :: pool.dropRight(1)

          case Some(limit) =>
            spawned :: pool.dropRight(limit - pool.length + 1)
        }
      )

    case KillAll(key) if key === poolKey =>
      Outcome(Nil)

    case Update(key) if key === poolKey =>
      val cullEvents = pool
        .filterNot(_.isAlive(frameContext.gameTime.running))
        .toList
        .flatMap(sa => sa.automaton.onCull(sa.seedValues))

      Outcome(pool.filter(_.isAlive(frameContext.gameTime.running))).addGlobalEvents(cullEvents)

    case _ =>
      Outcome(pool)
  }

  def render(frameContext: FrameContext, pool: List[SpawnedAutomaton]): SceneUpdateFragment =
    layer.emptyScene(Automata.renderNoLayer(pool, frameContext.gameTime))
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

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.While", "org.wartremover.warts.NonUnitStatements"))
  def renderNoLayer(pool: List[SpawnedAutomaton], gameTime: GameTime): AutomatonUpdate =
    AutomatonUpdate.sequence(
      pool.map { sa =>
        sa.automaton.modifier(sa.seedValues, sa.automaton.sceneGraphNode).at(gameTime.running - sa.seedValues.createdAt)
      }
    )

}

sealed trait AutomataEvent extends GlobalEvent
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

final case class Automaton(
    sceneGraphNode: SceneGraphNode,
    lifespan: Seconds,
    modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate],
    onCull: AutomatonSeedValues => List[GlobalEvent]
) {

  def withModifier(newModifier: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate]): Automaton =
    this.copy(modifier = newModifier)

  def withOnCullEvent(onCullEvent: AutomatonSeedValues => List[GlobalEvent]): Automaton =
    this.copy(onCull = onCullEvent)
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
    Automaton(SceneGraphNode, lifespan, NoModifySignal, NoCullEvent)

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

final case class SpawnedAutomaton(automaton: Automaton, seedValues: AutomatonSeedValues) {
  def isAlive(currentTime: Seconds): Boolean =
    seedValues.createdAt + seedValues.lifeSpan > currentTime
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

  def sequence(l: List[AutomatonUpdate]): AutomatonUpdate =
    new AutomatonUpdate(
      nodes = l.flatMap(_.nodes),
      events = l.flatMap(_.events)
    )

}
