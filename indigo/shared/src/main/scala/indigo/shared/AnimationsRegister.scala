package indigo.shared

import indigo.shared.time.GameTime
import indigo.shared.datatypes.BindingKey
import indigo.shared.animation.{Animation, AnimationKey}
// import indigo.shared.assets.AnimationStates
import indigo.shared.metrics._
// import indigo.shared.EqualTo._

// import scala.annotation.tailrec
// import scala.collection.mutable
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.AnimationMemento

final class AnimationsRegister {

  // Base registry
  private implicit val animationsRegistry: QuickCache[Animation] = QuickCache.empty
  // private implicit val animationsCache: QuickCache[AnimationCacheEntry] = QuickCache.empty
  private implicit val animationsStates: QuickCache[AnimationMemento] = QuickCache.empty

  // @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  // private val actionsQueue: mutable.Queue[AnimationActionCommand] =
  //   new mutable.Queue[AnimationActionCommand]()

  //
  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def register(animation: Animation): Unit = {
    QuickCache(animation.animationKey.value)(animation)
    ()
  }

  def findByAnimationKey(animationKey: AnimationKey): Option[Animation] =
    animationsRegistry.fetch(CacheKey(animationKey.value))

  // Animation states
  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  // private val animationStates: AnimationStates = AnimationStates(Nil)

  // def getAnimationStates: AnimationStates =
  //   animationStates

  // def setAnimationStates(updatedAnimationStates: AnimationStates): Unit =
  //   animationStates = updatedAnimationStates

  // Frame animation actions queue
  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  // def addAction(bindingKey: BindingKey, animationKey: AnimationKey, action: AnimationAction): Unit =
  //   actionsQueue.enqueue(AnimationActionCommand(bindingKey, animationKey, action))

  // private def dequeueAndDeduplicateActions(bindingKey: BindingKey, animationKey: AnimationKey): List[AnimationActionCommand] = {
  //   @tailrec
  //   def rec(remaining: List[AnimationActionCommand], hashesDone: List[String], acc: List[AnimationActionCommand]): List[AnimationActionCommand] =
  //     remaining match {
  //       case Nil =>
  //         acc

  //       case x :: xs if hashesDone.contains(x.hash) =>
  //         rec(xs, hashesDone, acc)

  //       case x :: xs =>
  //         rec(xs, x.hash :: hashesDone, x :: acc)
  //     }

  //   rec(actionsQueue.dequeueAll(p => p.animationKey === animationKey && p.bindingKey === bindingKey).toList, Nil, Nil)
  // }

  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  // private def clearActionsQueue(): Unit = {
  //   actionsQueue.dequeueAll(_ => true)
  //   ()
  // }

  // The running animation cache

  /*
    Look up of the cache entry means:
  - Inserting or fetching the entry for binding key x and animation key y
  - Applying an animation memento if one doesn't exist and running the commands queued against it.
   */
  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def fetchAnimationForSprite(gameTime: GameTime, bindingKey: BindingKey, animationKey: AnimationKey, animationActions: List[AnimationAction], metrics: Metrics): Option[Animation] =
    // QuickCacheMaybe(s"${bindingKey.value}") {
      findByAnimationKey(animationKey)
        .map { anim =>
          val stateCacheKey = CacheKey(bindingKey.value)

          metrics.record(ApplyAnimationMementoStartMetric)
          val updatedAnim =
            animationsStates
              .fetch(stateCacheKey)
              .map(m => anim.applyMemento(m))
              .getOrElse(anim)
          // animationStates.findStateWithBindingKey(bindingKey).map(m => anim.applyMemento(m)).getOrElse(anim)
          metrics.record(ApplyAnimationMementoEndMetric)

          metrics.record(RunAnimationActionsStartMetric)
          val newAnim =
            updatedAnim.runActions(animationActions, gameTime)
            // animationActions
            //   .foldLeft(updatedAnim)((a, action) => a.addAction(action))
            //   .runActions(gameTime)
          metrics.record(RunAnimationActionsEndMetric)

          // AnimationCacheEntry(bindingKey, newAnim)
          animationsStates.add(stateCacheKey, newAnim.saveMemento(bindingKey))

          newAnim
        }
    // }

  // private def saveAnimationMementos(): Unit =
  //   setAnimationStates(
  //     AnimationStates(
  //       animationsCache.all
  //         .map(_._2)
  //         .map(e => e.animations.saveMemento(e.bindingKey))
  //         .toList
  //     )
  //   )

  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  // def persistAnimationStates(): Unit =
  //   // saveAnimationMementos()
  //   // animationsCache.purgeAll()
  //   // clearActionsQueue()
  //   ()
}

// final case class AnimationActionCommand(bindingKey: BindingKey, animationKey: AnimationKey, action: AnimationAction) {
//   val hash: String = s"${bindingKey.value}_${animationKey.value}_${action.hash}"
// }

// final case class AnimationCacheEntry(bindingKey: BindingKey, animations: Animation)
