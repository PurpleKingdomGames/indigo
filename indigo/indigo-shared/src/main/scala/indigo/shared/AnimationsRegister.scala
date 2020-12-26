package indigo.shared

import indigo.shared.time.GameTime
import indigo.shared.datatypes.BindingKey
import indigo.shared.animation.{Animation, AnimationKey}
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.AnimationMemento
import scala.collection.mutable
import indigo.shared.animation.AnimationRef

final class AnimationsRegister {

  private val animationRegistry: mutable.HashMap[AnimationKey, AnimationRef] = new mutable.HashMap()
  private val animationStates: mutable.HashMap[BindingKey, AnimationMemento] = new mutable.HashMap()

  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def register(animation: Animation): Unit = {
    animationRegistry.put(animation.animationKey, AnimationRef.fromAnimation(animation))
    ()
  }

  def findByAnimationKey(animationKey: AnimationKey): Option[AnimationRef] =
    animationRegistry.get(animationKey)

  def findMementoByBindingKey(key: BindingKey): Option[AnimationMemento] =
    animationStates.get(key)

  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def fetchAnimationForSprite(gameTime: GameTime, bindingKey: BindingKey, animationKey: AnimationKey, animationActions: List[AnimationAction]): Option[AnimationRef] =
    fetchAnimationInLastState(bindingKey, animationKey).map { anim =>
      val newAnim = anim.runActions(animationActions, gameTime)

      animationStates.put(bindingKey, newAnim.saveMemento(bindingKey))

      newAnim
    }

  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def fetchAnimationInLastState(bindingKey: BindingKey, animationKey: AnimationKey): Option[AnimationRef] =
    findByAnimationKey(animationKey)
      .map { anim =>
        findMementoByBindingKey(bindingKey)
          .map(m => anim.applyMemento(m))
          .getOrElse(anim)
      }
}
