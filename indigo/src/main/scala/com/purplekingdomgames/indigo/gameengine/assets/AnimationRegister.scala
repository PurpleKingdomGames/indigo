package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.gameengine.scenegraph.{Animations, AnimationsKey}

import scala.collection.mutable

object AnimationRegister {

  private val animationsRegistry: mutable.HashMap[AnimationsKey, Animations] = mutable.HashMap()
  private var animationStates: AnimationStates = AnimationStates(Nil)

  private[gameengine] def register(animations: Animations): Unit = {
    animationsRegistry.put(animations.animationsKey, animations)
    ()
  }

  private[gameengine] def getAnimationStates: AnimationStates =
    animationStates

  private[gameengine] def setAnimationStates(updatedAnimationStates: AnimationStates): Unit =
    animationStates = updatedAnimationStates


}
