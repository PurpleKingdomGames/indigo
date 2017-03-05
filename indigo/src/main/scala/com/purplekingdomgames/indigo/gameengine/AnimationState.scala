package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.indigo.gameengine.scenegraph.{AnimationMemento, SceneGraphNodeInternal, Sprite}

object AnimationState {

  def extractAnimationStates(sceneGraphNode: SceneGraphNodeInternal): AnimationStates = AnimationStates {
    sceneGraphNode.flatten(Nil)
      .flatMap {
        case s: Sprite => s.saveAnimationMemento :: Nil
        case _ => Nil
      }
      .collect {
        case Some(s) => s
      }
  }

  def applyAnimationStates(animationStates: AnimationStates, sceneGraphNode: SceneGraphNodeInternal): SceneGraphNodeInternal =
    sceneGraphNode.applyAnimationMemento(animationStates)

}

case class AnimationStates(states: List[AnimationMemento]) {

  def withBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value == bindingKey.value)

}
