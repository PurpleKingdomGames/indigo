package indigo.gameengine.assets

import indigo.gameengine.scenegraph.animation.AnimationMemento
import indigo.gameengine.scenegraph.datatypes.BindingKey

import indigo.shared.EqualTo._

final class AnimationStates(val states: List[AnimationMemento]) extends AnyVal {

  def findStateWithBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value === bindingKey.value)

}
object AnimationStates {

  def apply(states: List[AnimationMemento]): AnimationStates =
    new AnimationStates(states)

  val empty: AnimationStates =
    AnimationStates(Nil)

}
