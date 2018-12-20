package indigo.gameengine.assets

import indigo.gameengine.scenegraph._
import indigo.gameengine.scenegraph.datatypes.BindingKey

final case class AnimationStates(states: List[AnimationMemento]) extends AnyVal {

  def findStateWithBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value == bindingKey.value)

}
