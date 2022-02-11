package indigo.shared

import indigo.shared.animation.Animation
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.AnimationKey
import indigo.shared.animation.AnimationMemento
import indigo.shared.animation.AnimationRef
import indigo.shared.datatypes.BindingKey
import indigo.shared.time.GameTime

final class AnimationsRegister:

  private val animationRegistry: scalajs.js.Dictionary[AnimationRef] = scalajs.js.Dictionary.empty
  private val animationStates: scalajs.js.Dictionary[AnimationMemento] = scalajs.js.Dictionary.empty

  def register(animation: Animation): Unit = {
    animationRegistry.put(animation.animationKey.toString, AnimationRef.fromAnimation(animation))
    ()
  }

  def findByAnimationKey(animationKey: AnimationKey): Option[AnimationRef] =
    animationRegistry.get(animationKey.toString)

  def findMementoByBindingKey(key: BindingKey): Option[AnimationMemento] =
    animationStates.get(key.toString)

  def fetchAnimationForSprite(
      gameTime: GameTime,
      bindingKey: BindingKey,
      animationKey: AnimationKey,
      animationActions: List[AnimationAction]
  ): Option[AnimationRef] =
    fetchAnimationInLastState(bindingKey, animationKey).map { anim =>
      val newAnim = anim.runActions(animationActions, gameTime)

      animationStates.put(bindingKey.toString, newAnim.saveMemento(bindingKey))

      newAnim
    }

  def fetchAnimationInLastState(bindingKey: BindingKey, animationKey: AnimationKey): Option[AnimationRef] =
    findByAnimationKey(animationKey)
      .map { anim =>
        findMementoByBindingKey(bindingKey)
          .map(m => anim.applyMemento(m))
          .getOrElse(anim)
      }
