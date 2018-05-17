package com.purplekingdomgames.indigoexts.automata

import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, Sprite, Text}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey

sealed trait Automaton {
  val bindingKey: BindingKey = BindingKey.generate
  val key: AutomataPoolKey
  val modifiers: List[AutomataModifier]
  val lifespan: AutomataLifeSpan
}
case class GraphicAutomaton(key: AutomataPoolKey, graphic: Graphic, lifespan: AutomataLifeSpan, modifiers: List[AutomataModifier])
    extends Automaton
case class SpriteAutomaton(key: AutomataPoolKey,
                           sprite: Sprite,
                           autoPlay: Boolean,
                           animationCycleLabel: Option[String],
                           lifespan: AutomataLifeSpan,
                           modifiers: List[AutomataModifier])
    extends Automaton
case class TextAutomaton(key: AutomataPoolKey, text: Text, lifespan: AutomataLifeSpan, modifiers: List[AutomataModifier])
    extends Automaton

case class AutomataPoolKey(key: String) extends AnyVal {
  def ===(other: AutomataPoolKey): Boolean =
    AutomataPoolKey.equality(this, other)
}
object AutomataPoolKey {
  def generate: AutomataPoolKey =
    AutomataPoolKey(BindingKey.generate.value)

  def equality(a: AutomataPoolKey, b: AutomataPoolKey): Boolean =
    a.key == b.key
}

case class AutomataLifeSpan(millis: Double)
