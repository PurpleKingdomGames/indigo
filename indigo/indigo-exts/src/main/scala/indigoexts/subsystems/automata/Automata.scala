package indigoexts.subsystems.automata

import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.Outcome
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.scenegraph._
import indigoexts.subsystems.SubSystem
import indigoexts.subsystems.automata.AutomataEvent._
import indigo.shared.dice.Dice
import indigoexts.subsystems.automata.Automata.Layer
import indigo.shared.EqualTo._
import indigo.shared.datatypes.Tint
import scala.collection.mutable

final class Automata(val poolKey: AutomataPoolKey, val automaton: Automaton, val layer: Layer, maxPoolSize: Option[Int], val pool: List[SpawnedAutomaton]) extends SubSystem {
  type EventType = AutomataEvent

  def liveAutomataCount: Int =
    pool.size

  def withMaxPoolSize(limit: Int): Automata =
    new Automata(poolKey, automaton, layer, Option(limit), pool)

  val eventFilter: GlobalEvent => Option[AutomataEvent] = {
    case e: AutomataEvent =>
      Some(e)

    case FrameTick =>
      Some(AutomataEvent.Cull)

    case _ =>
      None
  }

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def update(gameTime: GameTime, dice: Dice): AutomataEvent => Outcome[Automata] = {
    case Spawn(key, position, lifeSpan, payload) if key === poolKey =>
      val spawned =
        SpawnedAutomaton(
          automaton,
          AutomatonSeedValues(
            position,
            gameTime.running,
            lifeSpan.getOrElse(automaton.lifespan),
            Millis.zero,
            dice.roll,
            payload
          )
        )

      val newPool: List[SpawnedAutomaton] =
        maxPoolSize match {
          case None =>
            pool :+ spawned

          case Some(limit) if pool.length < limit =>
            pool :+ spawned

          case Some(limit) if pool.length === limit =>
            pool.drop(1) :+ spawned

          case Some(limit) =>
            pool.drop(limit - pool.length + 1) :+ spawned

        }

      Outcome(new Automata(poolKey, automaton, layer, maxPoolSize, newPool))

    case KillAllInPool(key) if key === poolKey =>
      Outcome(Automata(poolKey, automaton, layer))

    case KillAll =>
      Outcome(Automata(poolKey, automaton, layer))

    case Cull =>
      val (l, r) =
        pool.partition(_.isAlive(gameTime.running))

      Outcome(new Automata(poolKey, automaton, layer, maxPoolSize, l.map(_.updateDelta(gameTime.delta))))
        .addGlobalEvents(r.flatMap(sa => sa.automaton.onCull(sa.seedValues)))

    case _ =>
      Outcome(this)
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    Automata.render(this, gameTime)

  def report: String =
    "Automata farm"

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
            Tint.None,
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
            Tint.None,
            automatonUpdate.events,
            SceneAudio.None,
            ScreenEffects.None,
            Nil
          )

        case Layer.UI =>
          SceneUpdateFragment(
            Nil,
            Nil,
            automatonUpdate.nodes,
            Tint.None,
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
    new Automata(poolKey, automaton, layer, None, Nil)

  def render(farm: Automata, gameTime: GameTime): SceneUpdateFragment =
    farm.layer.emptyScene(renderNoLayer(farm, gameTime))

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
