package indigoextras.subsystems.automata

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.scenegraph._
import indigo.shared.subsystems.SubSystem
import indigoextras.subsystems.automata.AutomataEvent._
import indigoextras.subsystems.automata.Automata.Layer
import indigo.shared.EqualTo._
import indigo.shared.datatypes.RGBA
import scala.collection.mutable
import indigo.shared.FrameContext

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
      Some(AutomataEvent.Cull)

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

    case KillAll =>
      pool.clear()
      Outcome(this)

    case Cull => // AKA: Update.
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
