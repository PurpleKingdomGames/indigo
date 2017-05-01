package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.indigo.gameengine.scenegraph._

object AnimationState {

  private def saveMementos[ViewEventDataType](nodes: List[SceneGraphNodeLeaf[ViewEventDataType]]): List[AnimationMemento] = {
    nodes
      .flatMap {
        case s: Sprite[ViewEventDataType] => s.saveAnimationMemento :: Nil
        case _ => Nil
      }
      .collect {
        case Some(s) => s
      }
  }

  def extractAnimationStates[ViewEventDataType](sceneGraphRootNode: SceneGraphRootNodeFlat[ViewEventDataType]): AnimationStates = AnimationStates {
    saveMementos(sceneGraphRootNode.game.nodes) ++
    saveMementos(sceneGraphRootNode.lighting.nodes) ++
    saveMementos(sceneGraphRootNode.ui.nodes)
  }

}

case class AnimationStates(states: List[AnimationMemento]) {

  def withBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value == bindingKey.value)

}
