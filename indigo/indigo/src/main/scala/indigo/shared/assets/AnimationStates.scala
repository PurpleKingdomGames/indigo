package indigo.shared.assets

import indigo.shared.animation.AnimationMemento
import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey

opaque type AnimationStates = Batch[AnimationMemento]
object AnimationStates:
  inline def apply(states: Batch[AnimationMemento]): AnimationStates = states

  extension (as: AnimationStates)
    def findStateWithBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
      as.find(_.bindingKey == bindingKey)
