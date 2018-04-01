package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.indigo.gameengine.scenegraph._

object AnimationState {

  private def saveMementos(nodes: List[SceneGraphNodeLeaf]): List[AnimationMemento] = {
    nodes
      .flatMap {
        case s: Sprite => s.saveAnimationMemento :: Nil
        case _ => Nil
      }
      .collect {
        case Some(s) => s
      }
  }

  def extractAnimationStates(sceneGraphRootNode: SceneGraphRootNodeFlat): AnimationStates = AnimationStates {
    saveMementos(sceneGraphRootNode.game.nodes) ++
    saveMementos(sceneGraphRootNode.lighting.nodes) ++
    saveMementos(sceneGraphRootNode.ui.nodes)
  }

}

case class AnimationStates(states: List[AnimationMemento]) extends AnyVal {

  def findStateWithBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value == bindingKey.value)

}
