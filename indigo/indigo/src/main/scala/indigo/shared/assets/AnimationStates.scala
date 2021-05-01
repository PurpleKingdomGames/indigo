package indigo.shared.assets

import indigo.shared.animation.AnimationMemento
import indigo.shared.datatypes.BindingKey

opaque type AnimationStates = List[AnimationMemento]
object AnimationStates:
  def apply(states: List[AnimationMemento]): AnimationStates = states

  extension (as: AnimationStates)
    def findStateWithBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
      as.find(_.bindingKey == bindingKey)
