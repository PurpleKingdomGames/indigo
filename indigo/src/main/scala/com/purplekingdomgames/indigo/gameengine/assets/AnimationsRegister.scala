package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.indigo.gameengine.scenegraph.{AnimationAction, Animations, AnimationsKey}

import scala.collection.mutable

object AnimationsRegister {

  // Base registry
  private val animationsRegistry: mutable.HashMap[AnimationsKey, Animations] = mutable.HashMap()

  private[gameengine] def register(animations: Animations): Unit = {
    animationsRegistry.put(animations.animationsKey, animations)
    ()
  }

  def findByAnimationsKey(animationsKey: AnimationsKey): Option[Animations] =
    animationsRegistry.get(animationsKey)

  // Animations states
  private var animationStates: AnimationStates = AnimationStates(Nil)

  private[gameengine] def getAnimationStates: AnimationStates =
    animationStates

  private[gameengine] def setAnimationStates(updatedAnimationStates: AnimationStates): Unit =
    animationStates = updatedAnimationStates

  // Frame animation actions queue
  private val actionsQueue: mutable.Queue[AnimationActionCommand] =
    new mutable.Queue[AnimationActionCommand]()

  def addAction(bindingKey: BindingKey, animationsKey: AnimationsKey, action: AnimationAction): Unit =
    actionsQueue.enqueue(AnimationActionCommand(bindingKey, animationsKey, action))

  private def dequeueAndDeduplicateActions(bindingKey: BindingKey, animationsKey: AnimationsKey): List[AnimationActionCommand] = {
    def rec(remaining: List[AnimationActionCommand], hashesDone: List[String], acc: List[AnimationActionCommand]): List[AnimationActionCommand] = {
      remaining match {
        case Nil =>
          acc

        case x :: xs if hashesDone.contains(x.hash) =>
          rec(xs, hashesDone, acc)

        case x :: xs =>
          rec(xs, x.hash :: hashesDone, x :: acc)
      }
    }

    rec(actionsQueue.dequeueAll(p => p.animationsKey === animationsKey && p.bindingKey === bindingKey).toList, Nil, Nil)
  }

  def clearActionsQueue(): Unit = {
    actionsQueue.dequeueAll(_ == true)
    ()
  }

  // The running animation cache
  private val animationsCache: mutable.HashMap[String, AnimationCacheEntry] = mutable.HashMap()

  def markAllAsUnseen(): Unit = {
    animationsCache.foreach { entry =>
      animationsCache.update(entry._1, entry._2.markAsUnseen)
    }
  }

  def fetchFromCache(gameTime: GameTime, bindingKey: BindingKey, animationsKey: AnimationsKey): Option[Animations] = {
    val key: String = s"${bindingKey.value}_${animationsKey.key}"

    val cacheEntry: Option[AnimationCacheEntry] = animationsCache.get(key).orElse {
      findByAnimationsKey(animationsKey).map { a =>
        AnimationCacheEntry(seen = false, bindingKey, animations = a)
      }
    }

    val res = cacheEntry.map { anim =>
      val updated = animationStates.findStateWithBindingKey(bindingKey).map(m => anim.animations.applyMemento(m)).getOrElse(anim.animations)
      val commands = dequeueAndDeduplicateActions(bindingKey, animationsKey)
      commands.foldLeft(updated)((anim, action) => anim.addAction(action.action)).runActions(gameTime)
    }

    cacheEntry.foreach { e => animationsCache.update(key, e.markAsSeen) }

    res
  }

  def removeAllUnseenFromCache(): Unit = {
    animationsCache.filter(_._2.wasUnseen).keys.foreach { key =>
      animationsCache.remove(key)
    }
  }

  def saveAnimationMementos(): Unit = {
    setAnimationStates(AnimationStates(animationsCache.map(e => e._2.animations.saveMemento(e._2.bindingKey)).toList))
  }

  /*
  TODO:
  Processing view:
  Actions for all animations will have been added to the queue

  Mark all cache entries as unseen

  DisplayObjectConversions calls the cache and says get or create and get me an entry for this bindingkey+animationkey pair

  Look up of the cache entry means:
  - Inserting or fetching the entry for binding key x and animation key y
  - Applying an animation memento if one exists
  - Running the commands queued against it.
  - Marking as seen

  DisplayObjectConversions then does the work to render the Sprite.

  Remove any entries not marked as seen.
  Save mementos
  Clear the actions queue

   */

}

case class AnimationActionCommand(bindingKey: BindingKey, animationsKey: AnimationsKey, action: AnimationAction) {
  val hash: String = s"${bindingKey.value}_${animationsKey.key}_${action.hash}"
}

case class AnimationCacheEntry(seen: Boolean, bindingKey: BindingKey, animations: Animations) {
  def wasUnseen: Boolean = !seen
  def markAsUnseen: AnimationCacheEntry = this.copy(seen = false)
  def markAsSeen: AnimationCacheEntry = this.copy(seen = true)
}