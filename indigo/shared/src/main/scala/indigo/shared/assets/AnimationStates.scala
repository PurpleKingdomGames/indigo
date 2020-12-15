package indigo.shared.assets

import indigo.shared.animation.AnimationMemento
import indigo.shared.datatypes.BindingKey

final class AnimationStates(val states: List[AnimationMemento]) extends AnyVal {

  def findStateWithBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value == bindingKey.value)

}
object AnimationStates {

  def apply(states: List[AnimationMemento]): AnimationStates =
    new AnimationStates(states)
}
