package com.purplekingdomgames.indigoexts.automata

import com.purplekingdomgames.indigo.GameTime
import com.purplekingdomgames.indigo.gameengine.events.GlobalEventStream
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point
import com.purplekingdomgames.indigo.gameengine.scenegraph.{SceneGraphNodeBranch, Sprite}
import com.purplekingdomgames.indigoexts.automata.AutomataEvent.{KillAll, KillAllInPool, KillByKey, Spawn}
import com.purplekingdomgames.indigoexts.automata.AutomataModifier._

import scala.collection.mutable

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

  private val inventory: mutable.HashMap[AutomataPoolKey, Automaton] = mutable.HashMap()
  private var paddock: List[SpawnedAutomaton] = Nil

  def register(automaton: Automaton): Unit = {
    inventory.put(automaton.key, automaton)
    ()
  }

  def update(gameTime: GameTime, automataEvent: AutomataEvent): Unit = {
    automataEvent match {
      case Spawn(key, pt) =>
        inventory.get(key).foreach { k =>
          paddock = paddock :+ SpawnedAutomaton(k, gameTime.running, pt)
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
  }


  def render(gameTime: GameTime): SceneGraphNodeBranch = {
    paddock = paddock.filter(_.isAlive(gameTime.running))

    SceneGraphNodeBranch(
      paddock.map { sa =>
        sa.automata match {
          case GraphicAutomaton(_, graphic, _, modifiers) =>
            modifiers.foldLeft(graphic) { (g, m) =>
              m match {
                case ChangeAlpha(f) =>
                  g.withAlpha(f(gameTime, g.effects.alpha))

                case ChangeTint(f) =>
                  g.withTint(f(gameTime, g.effects.tint))

                case MoveTo(f) =>
                  g.moveTo(f(gameTime, g.bounds.position))

                case EmitEvents(f) =>
                  f(gameTime).foreach(GlobalEventStream.push)
                  g
              }
            }

          case SpriteAutomaton(_, sprite, autoPlay, maybeCycleLabel, _, modifiers) =>
            def applySpriteModifiers(sp: Sprite): Sprite = {
              modifiers.foldLeft[Sprite](sp) { (s, m) =>
                m match {
                  case ChangeAlpha(f) =>
                    s.withAlpha(f(gameTime, s.effects.alpha))

                  case ChangeTint(f) =>
                    s.withTint(f(gameTime, s.effects.tint))

                  case MoveTo(f) =>
                    s.moveTo(f(gameTime, s.bounds.position))

                  case EmitEvents(f) =>
                    f(gameTime).foreach(GlobalEventStream.push)
                    s
                }
              }
            }

            applySpriteModifiers(((s: Sprite) => if(autoPlay) s.play() else s)(maybeCycleLabel.map(l => sprite.changeCycle(l)).getOrElse(sprite)))

          case TextAutomaton(_, text, _, modifiers) =>
            modifiers.foldLeft(text) { (t, m) =>
              m match {
                case ChangeAlpha(f) =>
                  t.withAlpha(f(gameTime, t.effects.alpha))

                case ChangeTint(f) =>
                  t.withTint(f(gameTime, t.effects.tint))

                case MoveTo(f) =>
                  t.moveTo(f(gameTime, t.bounds.position))

                case EmitEvents(f) =>
                  f(gameTime).foreach(GlobalEventStream.push)
                  t
              }
            }
        }
      }
    )
  }

}

case class SpawnedAutomaton(automata: Automaton, createdAt: Double, spawnedAt: Point) {
  def isAlive(currentTime: Double): Boolean =
    createdAt + automata.lifespan.millis < currentTime
}
