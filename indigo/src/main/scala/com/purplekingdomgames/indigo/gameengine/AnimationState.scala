package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.indigo.gameengine.scenegraph._

object AnimationState {

  def extractAnimationStates(sceneGraphRootNode: SceneGraphRootNodeInternal): AnimationStates = AnimationStates {
    sceneGraphRootNode.nonEmptyLayers.flatMap { l =>
      l.node.flatten
        .flatMap {
          case s: SpriteInternal => s.saveAnimationMemento :: Nil
          case _ => Nil
        }
        .collect {
          case Some(s) => s
        }
    }
  }

  def applyAnimationStates(animationStates: AnimationStates, sceneGraphNode: SceneGraphNodeInternal): SceneGraphNodeInternal =
    sceneGraphNode.applyAnimationMemento(animationStates)

}

case class AnimationStates(states: List[AnimationMemento]) {

  def withBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value == bindingKey.value)

}
