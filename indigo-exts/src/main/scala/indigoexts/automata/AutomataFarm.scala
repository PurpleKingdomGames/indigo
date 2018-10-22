package indigoexts.automata

import indigo.gameengine.GameTime
import indigo.gameengine.events.FrameEvent
import indigo.gameengine.scenegraph.datatypes.Point
import indigo.gameengine.scenegraph._
import indigoexts.automata.AutomataEvent.{KillAll, KillAllInPool, KillByKey, Spawn}
import indigoexts.automata.AutomataModifier._

import scala.collection.mutable
import scala.util.Random

/*
Properties of an automaton:
They have a fixed lifespan
They have a thing to render
They have procedural modifiers based on time and previous value
They can emit events
 */

/*
This is full of mutable nasty, but I can't think of a less awful way of doing it
at the moment. It has an odd lifecycle. If we integrated it as a thing into the
full engine I could clean it up, as a bolt on... I'm awaiting inspiration.
 */
object AutomataFarm {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val inventory: mutable.HashMap[AutomataPoolKey, Automaton] = mutable.HashMap()
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var paddock: List[SpawnedAutomaton] = Nil

  def register(automaton: Automaton): Unit = {
    inventory.put(automaton.key, automaton)
    ()
  }

  def update(gameTime: GameTime, automataEvent: AutomataEvent): Unit =
    automataEvent match {
      case Spawn(key, pt) =>
        inventory.get(key).foreach { k =>
          paddock = paddock :+ SpawnedAutomaton(k, AutomatonSeedValues(pt, gameTime.running, k.lifespan.millis, 0, Random.nextInt()))
        }

      case KillAllInPool(key) =>
        paddock = paddock.filterNot(p => p.automata.key === key)
        ()

      case KillByKey(bindingKey) =>
        paddock = paddock.filterNot(p => p.automata.bindingKey === bindingKey)
        ()

      case KillAll =>
        paddock = Nil
        ()
    }

  def renderToGameLayer(gameTime: GameTime): SceneUpdateFragment = {
    val f =
      (p: List[(SceneGraphNode, List[FrameEvent])]) =>
        p.map(q => SceneUpdateFragment.empty.addGameLayerNodes(q._1).addViewEvents(q._2))
          .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

    f(render(gameTime))
  }

  def renderToLightingLayer(gameTime: GameTime): SceneUpdateFragment = {
    val f =
      (p: List[(SceneGraphNode, List[FrameEvent])]) =>
        p.map(q => SceneUpdateFragment.empty.addGameLayerNodes(q._1).addViewEvents(q._2))
          .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

    f(render(gameTime))
  }

  def renderToUiLayer(gameTime: GameTime): SceneUpdateFragment = {
    val f =
      (p: List[(SceneGraphNode, List[FrameEvent])]) =>
        p.map(q => SceneUpdateFragment.empty.addGameLayerNodes(q._1).addViewEvents(q._2))
          .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

    f(render(gameTime))
  }

  private def render(gameTime: GameTime): List[(SceneGraphNode, List[FrameEvent])] = {
    paddock = paddock.filter(_.isAlive(gameTime.running)).map(_.updateDelta(gameTime.delta))

    paddock.map { sa =>
      sa.automata match {
        case GraphicAutomaton(_, graphic, _, modifiers) =>
          modifiers.foldLeft[(Graphic, List[FrameEvent])]((graphic, Nil)) { (p, m) =>
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
          def applySpriteModifiers(sp: Sprite): (Sprite, List[FrameEvent]) =
            modifiers.foldLeft[(Sprite, List[FrameEvent])]((sp, Nil)) { (p, m) =>
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
          modifiers.foldLeft[(Text, List[FrameEvent])]((text, Nil)) { (p, m) =>
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

}

case class SpawnedAutomaton(automata: Automaton, seedValues: AutomatonSeedValues) {
  def isAlive(currentTime: Double): Boolean =
    seedValues.createdAt + automata.lifespan.millis > currentTime

  def updateDelta(frameDelta: Double): SpawnedAutomaton =
    this.copy(seedValues = seedValues.copy(timeAliveDelta = seedValues.timeAliveDelta + frameDelta.toInt))
}

case class AutomatonSeedValues(spawnedAt: Point, createdAt: Double, lifeSpan: Double, timeAliveDelta: Double, randomSeed: Int)
