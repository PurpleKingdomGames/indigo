package indigo.shared.assets

import indigo.shared.animation.AnimationMemento
import indigo.shared.datatypes.BindingKey

final case class AnimationStates(states: List[AnimationMemento]) extends AnyVal {

  def findStateWithBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value == bindingKey.value)

}
