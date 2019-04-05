package indigo.gameengine.assets

import indigo.time.GameTime
import indigo.gameengine.scenegraph.datatypes.BindingKey
import indigo.gameengine.scenegraph.animation.{AnimationAction, Animation, AnimationKey}
import indigo.runtime.metrics._
import indigo.shared.EqualTo._

import scala.annotation.tailrec
import scala.collection.mutable

object AnimationsRegister {

  // Base registry
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val animationsRegistry: mutable.HashMap[AnimationKey, Animation] = mutable.HashMap()

  private[gameengine] def register(animations: Animation): Unit = {
    animationsRegistry.update(animations.animationsKey, animations)
    ()
  }

  def findByAnimationKey(animationsKey: AnimationKey): Option[Animation] =
    animationsRegistry.get(animationsKey)

  // Animation states
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

  def addAction(bindingKey: BindingKey, animationsKey: AnimationKey, action: AnimationAction): Unit =
    actionsQueue.enqueue(AnimationActionCommand(bindingKey, animationsKey, action))

  private def dequeueAndDeduplicateActions(bindingKey: BindingKey, animationsKey: AnimationKey): List[AnimationActionCommand] = {
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

    rec(actionsQueue.dequeueAll(p => p.animationsKey === animationsKey && p.bindingKey === bindingKey).toList, Nil, Nil)
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
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
  def fetchFromCache(gameTime: GameTime, bindingKey: BindingKey, animationsKey: AnimationKey)(
      implicit metrics: Metrics
  ): Option[Animation] = {
    val key: String = s"${bindingKey.value}_${animationsKey.value}"

    val cacheEntry: Option[AnimationCacheEntry] = animationsCache.get(key).orElse {
      findByAnimationKey(animationsKey).map { anim =>
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

final case class AnimationActionCommand(bindingKey: BindingKey, animationsKey: AnimationKey, action: AnimationAction) {
  val hash: String = s"${bindingKey.value}_${animationsKey.value}_${action.hash}"
}

final case class AnimationCacheEntry(bindingKey: BindingKey, animations: Animation)
