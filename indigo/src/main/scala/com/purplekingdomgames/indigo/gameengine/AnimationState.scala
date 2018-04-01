package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.indigo.gameengine.scenegraph._

object AnimationState {

  private def saveMementos(nodes: List[Renderable]): List[AnimationMemento] = {
    def rec(remaining: List[Renderable], keysDone: List[String], acc: List[AnimationMemento]): List[AnimationMemento] = {
      remaining match {
        case Nil =>
          acc

        case (s: Sprite) :: xs if !keysDone.contains(s.bindingKey.value) =>
          rec(xs, s.bindingKey.value :: keysDone, s.saveAnimationMemento.map(p => p :: acc).getOrElse(acc))

        case _ :: xs =>
          rec(xs, keysDone, acc)
      }
    }

    rec(nodes, Nil, Nil)
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
