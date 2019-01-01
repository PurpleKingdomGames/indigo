package indigoexts.automata

import indigo.gameengine.GameTime
import indigo.gameengine.events.{FrameTick, GlobalEvent}
import indigo.gameengine.scenegraph._
import indigo.gameengine.scenegraph.datatypes.Point
import indigo.gameengine.subsystems.{SubSystem, UpdatedSubSystem}
import indigoexts.automata.AutomataEvent._
import indigoexts.automata.AutomataModifier._

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

  def update(gameTime: GameTime): AutomataEvent => UpdatedSubSystem =
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

  def update(farm: AutomataFarm, gameTime: GameTime): AutomataEvent => UpdatedSubSystem = {
    case Spawn(key, pt) =>
      UpdatedSubSystem(
        farm.copy(
          paddock =
            farm.paddock ++
              farm.inventory
                .get(key)
                .map { k =>
                  SpawnedAutomaton(k, AutomatonSeedValues(pt, gameTime.running, k.lifespan.millis, 0, Random.nextInt()))
                }
                .toList
        )
      )

    case ModifyAndSpawn(key, pt, f) =>
      UpdatedSubSystem(
        farm.copy(
          paddock =
            farm.paddock ++
              farm.inventory
                .get(key)
                .map(f orElse { case a => a })
                .map { k =>
                  SpawnedAutomaton(k, AutomatonSeedValues(pt, gameTime.running, k.lifespan.millis, 0, Random.nextInt()))
                }
                .toList
        )
      )

    case KillAllInPool(key) =>
      UpdatedSubSystem(
        farm.copy(
          paddock = farm.paddock.filterNot(p => p.automata.key === key)
        )
      )

    case KillByKey(bindingKey) =>
      UpdatedSubSystem(
        farm.copy(
          paddock = farm.paddock.filterNot(p => p.automata.bindingKey === bindingKey)
        )
      )

    case KillAll =>
      UpdatedSubSystem(
        farm.copy(
          paddock = Nil
        )
      )

    case Cull =>
      UpdatedSubSystem(
        farm.copy(
          paddock = farm.paddock.filter(_.isAlive(gameTime.running)).map(_.updateDelta(gameTime.delta))
        )
      )
  }

  def render(farm: AutomataFarm, gameTime: GameTime): SceneUpdateFragment = {
    val f =
      (p: List[(SceneGraphNode, List[GlobalEvent])]) =>
        p.map(q => SceneUpdateFragment.empty.addGameLayerNodes(q._1).addViewEvents(q._2))
          .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

    f(renderNoLayer(farm, gameTime))
  }

  def renderNoLayer(farm: AutomataFarm, gameTime: GameTime): List[(SceneGraphNode, List[GlobalEvent])] =
    farm.paddock.map { sa =>
      sa.automata match {
        case GraphicAutomaton(_, graphic, _, modifiers) =>
          modifiers.foldLeft[(Graphic, List[GlobalEvent])]((graphic, Nil)) { (p, m) =>
            m match {
              case ChangeAlpha(f) =>
                (p._1.withAlpha(f(gameTime, sa.seedValues, p._1.effects.alpha)), Nil)

              case ChangeTint(f) =>
                (p._1.withTint(f(gameTime, sa.seedValues, p._1.effects.tint)), Nil)

              case MoveTo(f) =>
                (p._1.moveTo(f(gameTime, sa.seedValues, p._1.bounds.position)), Nil)

              case EmitEvents(f) =>
                (p._1, p._2 ++ f(gameTime, sa.seedValues))
            }
          }

        case SpriteAutomaton(_, sprite, autoPlay, maybeCycleLabel, _, modifiers) =>
          def applySpriteModifiers(sp: Sprite): (Sprite, List[GlobalEvent]) =
            modifiers.foldLeft[(Sprite, List[GlobalEvent])]((sp, Nil)) { (p, m) =>
              m match {
                case ChangeAlpha(f) =>
                  (p._1.withAlpha(f(gameTime, sa.seedValues, p._1.effects.alpha)), Nil)

                case ChangeTint(f) =>
                  (p._1.withTint(f(gameTime, sa.seedValues, p._1.effects.tint)), Nil)

                case MoveTo(f) =>
                  (p._1.moveTo(f(gameTime, sa.seedValues, p._1.bounds.position)), Nil)

                case EmitEvents(f) =>
                  (p._1, p._2 ++ f(gameTime, sa.seedValues))
              }
            }

          applySpriteModifiers(
            ((s: Sprite) => if (autoPlay) s.play() else s)(maybeCycleLabel.map(l => sprite.changeCycle(l)).getOrElse(sprite))
          )

        case TextAutomaton(_, text, _, modifiers) =>
          modifiers.foldLeft[(Text, List[GlobalEvent])]((text, Nil)) { (p, m) =>
            m match {
              case ChangeAlpha(f) =>
                (p._1.withAlpha(f(gameTime, sa.seedValues, p._1.effects.alpha)), Nil)

              case ChangeTint(f) =>
                (p._1.withTint(f(gameTime, sa.seedValues, p._1.effects.tint)), Nil)

              case MoveTo(f) =>
                (p._1.moveTo(f(gameTime, sa.seedValues, p._1.bounds.position)), Nil)

              case EmitEvents(f) =>
                (p._1, p._2 ++ f(gameTime, sa.seedValues))
            }
          }
      }
    }

}

final case class SpawnedAutomaton(automata: Automaton, seedValues: AutomatonSeedValues) {
  def isAlive(currentTime: Double): Boolean =
    seedValues.createdAt + automata.lifespan.millis > currentTime

  def updateDelta(frameDelta: Double): SpawnedAutomaton =
    this.copy(seedValues = seedValues.copy(timeAliveDelta = seedValues.timeAliveDelta + frameDelta.toInt))
}

final case class AutomatonSeedValues(spawnedAt: Point, createdAt: Double, lifeSpan: Double, timeAliveDelta: Double, randomSeed: Int)
