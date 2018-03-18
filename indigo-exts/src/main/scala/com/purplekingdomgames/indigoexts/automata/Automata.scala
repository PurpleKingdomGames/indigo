package com.purplekingdomgames.indigoexts.automata

import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, Sprite, Text}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey

sealed trait Automata {
  val key: AutomataKey
  val modifiers: List[AutomataModifier]
  val lifespan: AutomataLifeSpan
}
case class GraphicAutomata(key: AutomataKey, graphic: Graphic, lifespan: AutomataLifeSpan, modifiers: List[AutomataModifier]) extends Automata
case class SpriteAutomata(key: AutomataKey, sprite: Sprite, autoPlay: Boolean, animationCycleLabel: Option[String], lifespan: AutomataLifeSpan, modifiers: List[AutomataModifier]) extends Automata
case class TextAutomata(key: AutomataKey, text: Text, lifespan: AutomataLifeSpan, modifiers: List[AutomataModifier]) extends Automata

case class AutomataKey(key: String)
object AutomataKey {
  def generate: AutomataKey =
    AutomataKey(BindingKey.generate.value)
}

case class AutomataLifeSpan(millis: Int)
