package com.purplekingdomgames.indigoexts.automata

import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, Sprite, Text}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey

sealed trait Automata {
  val bindingKey: BindingKey = BindingKey.generate
  val key: AutomataPoolKey
  val modifiers: List[AutomataModifier]
  val lifespan: AutomataLifeSpan
}
case class GraphicAutomata(key: AutomataPoolKey, graphic: Graphic, lifespan: AutomataLifeSpan, modifiers: List[AutomataModifier]) extends Automata
case class SpriteAutomata(key: AutomataPoolKey, sprite: Sprite, autoPlay: Boolean, animationCycleLabel: Option[String], lifespan: AutomataLifeSpan, modifiers: List[AutomataModifier]) extends Automata
case class TextAutomata(key: AutomataPoolKey, text: Text, lifespan: AutomataLifeSpan, modifiers: List[AutomataModifier]) extends Automata

case class AutomataPoolKey(key: String)
object AutomataPoolKey {
  def generate: AutomataPoolKey =
    AutomataPoolKey(BindingKey.generate.value)
}

case class AutomataLifeSpan(millis: Int)
