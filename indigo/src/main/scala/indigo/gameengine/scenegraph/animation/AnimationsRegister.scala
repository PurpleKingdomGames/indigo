package indigo.gameengine.assets

import indigo.time.GameTime
import indigo.gameengine.scenegraph.datatypes.BindingKey
import indigo.gameengine.scenegraph.animation.{AnimationAction, Animation, AnimationKey}
import indigo.runtime.metrics._
import indigo.shared.EqualTo._

import scala.annotation.tailrec

final class AnimationsRegister(val animations: Map[AnimationKey, Animation], val cache: AnimationsCache, val states: AnimationStates) {
  def findByAnimationKey(animationKey: AnimationKey): Option[Animation] =
    AnimationsRegister.findByAnimationKey(this, animationKey)

  def persistAnimationStates: AnimationsRegister =
    AnimationsRegister.persistAnimationStates(this)
}
object AnimationsRegister {

  def apply(animationsMap: Map[AnimationKey, Animation], animationsCache: AnimationsCache, animationStates: AnimationStates): AnimationsRegister =
    new AnimationsRegister(animationsMap, animationsCache, animationStates)

  def fromSet(animations: Set[Animation]): AnimationsRegister =
    AnimationsRegister(
      animations.foldLeft(Map.empty[AnimationKey, Animation])((acc, n) => acc + (n.animationKey -> n)),
      AnimationsCache.empty,
      AnimationStates.empty
    )

  def findByAnimationKey(register: AnimationsRegister, animationKey: AnimationKey): Option[Animation] =
    register.animations.get(animationKey)


/*
  When someone calls <sprite>.play() during a frame,
  it adds an action directly (yay singletons!) to this queue...
*/
  // Frame animation actions queue
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val actionsQueue: mutable.Queue[AnimationActionCommand] =
    new mutable.Queue[AnimationActionCommand]()

  def addAction(bindingKey: BindingKey, animationKey: AnimationKey, action: AnimationAction): Unit =
    actionsQueue.enqueue(AnimationActionCommand(bindingKey, animationKey, action))

  private def dequeueAndDeduplicateActions(bindingKey: BindingKey, animationKey: AnimationKey): List[AnimationActionCommand] = {
    @tailrec
    def rec(remaining: List[AnimationActionCommand], hashesDone: List[String], acc: List[AnimationActionCommand]): List[AnimationActionCommand] =
      remaining match {
        case Nil =>
          acc

        case x :: xs if hashesDone.contains(x.hash) =>
          rec(xs, hashesDone, acc)

        case x :: xs =>
          rec(xs, x.hash :: hashesDone, x :: acc)
      }

    rec(actionsQueue.dequeueAll(p => p.animationKey === animationKey && p.bindingKey === bindingKey).toList, Nil, Nil)
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  val clearActionsQueue: AnimationsRegister => AnimationsRegister = register => {
    actionsQueue.dequeueAll(_ => true)
    ()
  }

  /*
    The Cache is only used once per frame to save recalculating the same animation.
    Look up of the cache entry means:
  - Fetching the entry for sprite binding key x and animation key y, OR
  - If one doesn't exist yet, applying an animation memento (if one exists) and running any queued commands against it.
   */
  def fetchFromCache(register: AnimationsRegister, gameTime: GameTime, bindingKey: BindingKey, animationKey: AnimationKey)(
      implicit metrics: Metrics
  ): (AnimationsRegister, Option[Animation]) = {
    val key: String = s"${bindingKey.value}_${animationKey.value}"

    val cacheEntry: Option[AnimationCacheEntry] = register.cache.cache.get(key).orElse {
      register.findByAnimationKey(animationKey).map { anim =>
        metrics.record(ApplyAnimationMementoStartMetric)
        val updated =
          register.states
            .findStateWithBindingKey(bindingKey)
            .map(m => anim.applyMemento(m))
            .getOrElse(anim)
        metrics.record(ApplyAnimationMementoEndMetric)

        metrics.record(RunAnimationActionsStartMetric)
        val commands =
          dequeueAndDeduplicateActions(bindingKey, animationKey)

        val newAnim =
          commands.foldLeft(updated)((a, action) => a.addAction(action.action)).runActions(gameTime)
        metrics.record(RunAnimationActionsEndMetric)

        AnimationCacheEntry(bindingKey, newAnim)
      }
    }

    cacheEntry match {
      case Some(entry) =>
        (upsertCacheEntry(register, key, entry), Option(entry.animation))

      case None =>
        (register, None)
    }
  }

  def upsertCacheEntry(register: AnimationsRegister, key: String, value: AnimationCacheEntry): AnimationsRegister =
    new AnimationsRegister(register.animations, register.cache.upsert(key, value), register.states)

  val clearCache: AnimationsRegister => AnimationsRegister = register => AnimationsRegister(register.animations, AnimationsCache.empty, register.states)

  val saveAnimationMementos: AnimationsRegister => AnimationsRegister = register =>
    AnimationsRegister(
      register.animations,
      register.cache,
      AnimationStates(
        register.cache.cache
          .mapValues { e =>
            e.animation.saveMemento(e.bindingKey)
          }
          .values
          .toList
      )
    )

  def persistAnimationStates(register: AnimationsRegister): AnimationsRegister =
    (saveAnimationMementos andThen clearCache andThen clearActionsQueue)(register)
}

final class AnimationsCache(val cache: Map[String, AnimationCacheEntry]) {
  def upsert(key: String, value: AnimationCacheEntry): AnimationsCache =
    AnimationsCache(cache + (key -> value))
}
object AnimationsCache {

  def apply(cache: Map[String, AnimationCacheEntry]): AnimationsCache =
    new AnimationsCache(cache)

  val empty: AnimationsCache =
    new AnimationsCache(Map.empty[String, AnimationCacheEntry])
}

final class AnimationActionCommand(val bindingKey: BindingKey, val animationKey: AnimationKey, val action: AnimationAction) {
  val hash: String = s"${bindingKey.value}_${animationKey.value}_${action.hash}"
}
object AnimationActionCommand {
  def apply(bindingKey: BindingKey, animationKey: AnimationKey, action: AnimationAction): AnimationActionCommand =
    new AnimationActionCommand(bindingKey, animationKey, action)
}

final class AnimationCacheEntry(val bindingKey: BindingKey, val animation: Animation)
object AnimationCacheEntry {
  def apply(bindingKey: BindingKey, animation: Animation): AnimationCacheEntry =
    new AnimationCacheEntry(bindingKey, animation)
}
