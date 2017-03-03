package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.{AnimationMemento, BindingKey, SceneGraphNode, Sprite}

object AnimationState {

  def extractAnimationStates(sceneGraphNode: SceneGraphNode): AnimationStates = AnimationStates {
    sceneGraphNode.flatten(Nil).flatMap {
      case s: Sprite => s.saveAnimationMemento :: Nil
      case _ => Nil
    }
  }

  def applyAnimationStates(animationStates: AnimationStates, sceneGraphNode: SceneGraphNode): SceneGraphNode =
    sceneGraphNode.applyAnimationMemento(animationStates)

}

case class AnimationStates(states: List[AnimationMemento]) {

  def withBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value == bindingKey.value)

}
