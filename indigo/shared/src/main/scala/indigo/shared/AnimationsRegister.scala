package indigo.shared

import indigo.shared.time.GameTime
import indigo.shared.datatypes.BindingKey
import indigo.shared.animation.{Animation, AnimationKey}
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.AnimationMemento

final class AnimationsRegister {

  private implicit val animationsRegistry: QuickCache[Animation]      = QuickCache.empty
  private implicit val animationsStates: QuickCache[AnimationMemento] = QuickCache.empty

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def register(animation: Animation): Unit = {
    QuickCache(animation.animationKey.value)(animation)
    ()
  }

  def findByAnimationKey(animationKey: AnimationKey): Option[Animation] =
    animationsRegistry.fetch(CacheKey(animationKey.value))

  def findMementoByBindingKey(key: BindingKey): Option[AnimationMemento] =
    animationsStates.fetch(CacheKey(key.value))

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def fetchAnimationForSprite(gameTime: GameTime, bindingKey: BindingKey, animationKey: AnimationKey, animationActions: List[AnimationAction]): Option[Animation] =
    findByAnimationKey(animationKey)
      .map { anim =>
        val updatedAnim =
          findMementoByBindingKey(bindingKey)
            .map(m => anim.applyMemento(m))
            .getOrElse(anim)

        val newAnim = updatedAnim.runActions(animationActions, gameTime)

        animationsStates.add(CacheKey(bindingKey.value), newAnim.saveMemento(bindingKey))

        newAnim
      }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def fetchAnimationInLastState(bindingKey: BindingKey, animationKey: AnimationKey): Option[Animation] =
    findByAnimationKey(animationKey)
      .map { anim =>
        findMementoByBindingKey(bindingKey)
          .map(m => anim.applyMemento(m))
          .getOrElse(anim)
      }
}
