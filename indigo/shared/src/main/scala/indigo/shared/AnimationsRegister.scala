package indigo.shared

import indigo.shared.time.GameTime
import indigo.shared.datatypes.BindingKey
import indigo.shared.animation.{Animation, AnimationKey}
import indigo.shared.metrics._
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.AnimationMemento

final class AnimationsRegister {

  private implicit val animationsRegistry: QuickCache[Animation] = QuickCache.empty
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
  def fetchAnimationForSprite(gameTime: GameTime, bindingKey: BindingKey, animationKey: AnimationKey, animationActions: List[AnimationAction], metrics: Metrics): Option[Animation] =
    findByAnimationKey(animationKey)
      .map { anim =>
        metrics.record(ApplyAnimationMementoStartMetric)
        val updatedAnim =
          findMementoByBindingKey(bindingKey)
            .map(m => anim.applyMemento(m))
            .getOrElse(anim)
        metrics.record(ApplyAnimationMementoEndMetric)

        metrics.record(RunAnimationActionsStartMetric)
        val newAnim = updatedAnim.runActions(animationActions, gameTime)
        metrics.record(RunAnimationActionsEndMetric)

        animationsStates.add(CacheKey(bindingKey.value), newAnim.saveMemento(bindingKey))

        newAnim
      }
}
