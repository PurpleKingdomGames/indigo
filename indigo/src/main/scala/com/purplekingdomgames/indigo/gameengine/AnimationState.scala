package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.indigo.gameengine.scenegraph._

object AnimationState {

  private def flattenNode[ViewEventDataType](node: SceneGraphNode[ViewEventDataType]): List[AnimationMemento] = {
    node
      .flatten
      .flatMap {
        case s: Sprite[ViewEventDataType] => s.saveAnimationMemento :: Nil
        case _ => Nil
      }
      .collect {
        case Some(s) => s
      }
  }

  def extractAnimationStates[ViewEventDataType](sceneGraphRootNode: SceneGraphRootNode[ViewEventDataType]): AnimationStates = AnimationStates {
    flattenNode(sceneGraphRootNode.game.node) ++
    flattenNode(sceneGraphRootNode.lighting.node) ++
    flattenNode(sceneGraphRootNode.ui.node)
  }

  def applyAnimationStates[ViewEventDataType](animationStates: AnimationStates, sceneGraphNode: SceneGraphNode[ViewEventDataType]): SceneGraphNode[ViewEventDataType] =
    sceneGraphNode.applyAnimationMemento(animationStates)

}

case class AnimationStates(states: List[AnimationMemento]) {

  def withBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value == bindingKey.value)

}
