package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.indigo.gameengine.scenegraph.{AnimationAction, Animations, AnimationsKey}
import com.purplekingdomgames.indigo.runtime.metrics._

import scala.collection.mutable

object AnimationsRegister {

  // Base registry
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val animationsRegistry: mutable.HashMap[AnimationsKey, Animations] = mutable.HashMap()

  private[gameengine] def register(animations: Animations): Unit = {
    animationsRegistry.update(animations.animationsKey, animations)
    ()
  }

  def findByAnimationsKey(animationsKey: AnimationsKey): Option[Animations] =
    animationsRegistry.get(animationsKey)

  // Animations states
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var animationStates: AnimationStates = AnimationStates(Nil)

  private[gameengine] def getAnimationStates: AnimationStates =
    animationStates

  private[gameengine] def setAnimationStates(updatedAnimationStates: AnimationStates): Unit =
    animationStates = updatedAnimationStates

  // Frame animation actions queue
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val actionsQueue: mutable.Queue[AnimationActionCommand] =
    new mutable.Queue[AnimationActionCommand]()

  def addAction(bindingKey: BindingKey, animationsKey: AnimationsKey, action: AnimationAction): Unit =
    actionsQueue.enqueue(AnimationActionCommand(bindingKey, animationsKey, action))

  private def dequeueAndDeduplicateActions(bindingKey: BindingKey, animationsKey: AnimationsKey): List[AnimationActionCommand] = {
    def rec(remaining: List[AnimationActionCommand],
            hashesDone: List[String],
            acc: List[AnimationActionCommand]): List[AnimationActionCommand] =
      remaining match {
        case Nil =>
          acc

        case x :: xs if hashesDone.contains(x.hash) =>
          rec(xs, hashesDone, acc)

        case x :: xs =>
          rec(xs, x.hash :: hashesDone, x :: acc)
      }

    rec(actionsQueue.dequeueAll(p => p.animationsKey === animationsKey && p.bindingKey === bindingKey).toList, Nil, Nil)
  }

  private def clearActionsQueue(): Unit = {
    actionsQueue.dequeueAll(_ => true)
    ()
  }

  // The running animation cache
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val animationsCache: mutable.HashMap[String, AnimationCacheEntry] = mutable.HashMap()

  /*
    Look up of the cache entry means:
  - Inserting or fetching the entry for binding key x and animation key y
  - Applying an animation memento if one doesn't exist and running the commands queued against it.
   */
  def fetchFromCache(gameTime: GameTime, bindingKey: BindingKey, animationsKey: AnimationsKey)(
      implicit metrics: IMetrics
  ): Option[Animations] = {
    val key: String = s"${bindingKey.value}_${animationsKey.key}"

    val cacheEntry: Option[AnimationCacheEntry] = animationsCache.get(key).orElse {
      findByAnimationsKey(animationsKey).map { anim =>
        metrics.record(ApplyAnimationMementoStartMetric)
        val updated = animationStates.findStateWithBindingKey(bindingKey).map(m => anim.applyMemento(m)).getOrElse(anim)
        metrics.record(ApplyAnimationMementoEndMetric)

        metrics.record(RunAnimationActionsStartMetric)
        val commands = dequeueAndDeduplicateActions(bindingKey, animationsKey)
        val newAnim  = commands.foldLeft(updated)((a, action) => a.addAction(action.action)).runActions(gameTime)
        metrics.record(RunAnimationActionsEndMetric)

        AnimationCacheEntry(bindingKey, newAnim)
      }
    }

    cacheEntry.foreach { e =>
      animationsCache.update(key, e)
    }

    cacheEntry.map(_.animations)
  }

  private def clearCache(): Unit =
    animationsCache.keys.foreach { key =>
      animationsCache.remove(key)
    }

  private def saveAnimationMementos(): Unit =
    setAnimationStates(AnimationStates(animationsCache.map(e => e._2.animations.saveMemento(e._2.bindingKey)).toList))

  def persistAnimationStates(): Unit = {
    saveAnimationMementos()
    clearCache()
    clearActionsQueue()
  }
}

case class AnimationActionCommand(bindingKey: BindingKey, animationsKey: AnimationsKey, action: AnimationAction) {
  val hash: String = s"${bindingKey.value}_${animationsKey.key}_${action.hash}"
}

case class AnimationCacheEntry(bindingKey: BindingKey, animations: Animations)
