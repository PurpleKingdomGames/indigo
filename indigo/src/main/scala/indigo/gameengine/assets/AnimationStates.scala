package indigo.gameengine.assets

import indigo.gameengine.scenegraph.animation.AnimationMemento
import indigo.gameengine.scenegraph.datatypes.BindingKey

import indigo.shared.EqualTo._

final case class AnimationStates(states: List[AnimationMemento]) extends AnyVal {

  def findStateWithBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value === bindingKey.value)

}
