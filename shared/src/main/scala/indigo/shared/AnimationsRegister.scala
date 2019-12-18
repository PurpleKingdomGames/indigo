package indigo.shared

import indigo.shared.time.GameTime
import indigo.shared.datatypes.BindingKey
import indigo.shared.animation.{AnimationAction, Animation, AnimationKey}
import indigo.shared.assets.AnimationStates
import indigo.shared.metrics._
import indigo.shared.EqualTo._

import scala.annotation.tailrec
import scala.collection.mutable

object AnimationsRegister {

  // Base registry
  private implicit val animationsRegistry: QuickCache[Animation]        = QuickCache.empty
  private implicit val animationsCache: QuickCache[AnimationCacheEntry] = QuickCache.empty

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val actionsQueue: mutable.Queue[AnimationActionCommand] =
    new mutable.Queue[AnimationActionCommand]()

  //
  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def register(animations: Animation): Unit = {
    QuickCache(animations.animationsKey.value)(animations)
    ()
  }

  def findByAnimationKey(animationsKey: AnimationKey): Option[Animation] =
    animationsRegistry.fetch(CacheKey(animationsKey.value))

  // Animation states
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var animationStates: AnimationStates = AnimationStates(Nil)

  def getAnimationStates: AnimationStates =
    animationStates

  def setAnimationStates(updatedAnimationStates: AnimationStates): Unit =
    animationStates = updatedAnimationStates

  // Frame animation actions queue
  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
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

  /*
    Look up of the cache entry means:
  - Inserting or fetching the entry for binding key x and animation key y
  - Applying an animation memento if one doesn't exist and running the commands queued against it.
   */
  def fetchFromCache(gameTime: GameTime, bindingKey: BindingKey, animationsKey: AnimationKey, metrics: Metrics): Option[Animation] =
    QuickCacheMaybe(s"${bindingKey.value}_${animationsKey.value}") {
      findByAnimationKey(animationsKey)
        .map { anim =>
          metrics.record(ApplyAnimationMementoStartMetric)
          val updated = animationStates.findStateWithBindingKey(bindingKey).map(m => anim.applyMemento(m)).getOrElse(anim)
          metrics.record(ApplyAnimationMementoEndMetric)

          metrics.record(RunAnimationActionsStartMetric)
          val commands = dequeueAndDeduplicateActions(bindingKey, animationsKey).reverse
          val newAnim  = commands.foldLeft(updated)((a, action) => a.addAction(action.action)).runActions(gameTime)
          metrics.record(RunAnimationActionsEndMetric)

          AnimationCacheEntry(bindingKey, newAnim)
        }
    }.map(_.animations)

  private def saveAnimationMementos(): Unit =
    setAnimationStates(
      AnimationStates(
        animationsCache.all
          .map(_._2)
          .map(e => e.animations.saveMemento(e.bindingKey))
          .toList
      )
    )

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def persistAnimationStates(): Unit = {
    saveAnimationMementos()
    animationsCache.purgeAll()
    clearActionsQueue()
  }
}

final case class AnimationActionCommand(bindingKey: BindingKey, animationsKey: AnimationKey, action: AnimationAction) {
  val hash: String = s"${bindingKey.value}_${animationsKey.value}_${action.hash}"
}

final case class AnimationCacheEntry(bindingKey: BindingKey, animations: Animation)
