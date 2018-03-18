package com.purplekingdomgames.indigoexts.automata

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point

/*
Properties of an automata:
They have a fixed lifespan
They have a thing to render
They have procedural rules / modifiers based on time and previous value
They can emit events
 */
object AutomataFarm {

//  def register(automata: Automata): Unit = {}
  def update(): Unit = {}
  def render(): Unit = {}

}

case class SpawnedAutomata(automata: Automata, createdAt: Long, spawnedAt: Point)
