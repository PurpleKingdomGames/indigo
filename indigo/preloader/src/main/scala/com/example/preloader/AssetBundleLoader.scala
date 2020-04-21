package com.example.preloader

import indigoexts.subsystems.SubSystem
import indigo.shared.events.GlobalEvent
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.assets.AssetType
import indigo.shared.datatypes.BindingKey
import indigo.shared.EqualTo._
import indigo.shared.assets.AssetPath
import indigo.shared.assets.AssetTypePrimitive

/*
All it does it track the progress of loading batches and emits events.
- Loading of batch X started
- Loading of batch X, N percent completed
- Loading of batch X completed success | failure

If you want to visualise the loader - make another sub system that's
listening for the events this one emits.
 */
final case class AssetBundleLoader(tracker: AssetBundleTracker) extends SubSystem {

  type EventType = AssetBundleLoaderEvent

  val eventFilter: GlobalEvent => Option[AssetBundleLoaderEvent] =
    _ => None

  def update(gameTime: GameTime, dice: Dice): AssetBundleLoaderEvent => Outcome[SubSystem] =
    _ => Outcome(this)

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty

}

final case class AssetBundleTracker(val register: List[AssetBundle]) {
  val bundleCount: Int =
    register.length

  def addBundle(key: BindingKey, assets: Set[AssetType]): AssetBundleTracker =
    if (assets.isEmpty || findBundleByKey(key).isDefined) this
    else {
      val flatAssets = AssetType.flattenAssetList(assets.toList)

      val newBundle =
        new AssetBundle(
          key,
          flatAssets.size,
          flatAssets.map { assetType =>
            (assetType.path -> new AssetToLoad(assetType, false, false))
          }.toMap
        )

      AssetBundleTracker(register :+ newBundle)
    }

  def findBundleByKey(key: BindingKey): Option[AssetBundle] =
    register.find(_.key === key)

  def findAssetByPath(path: AssetPath): List[AssetToLoad] =
    register.flatMap(_.assets.get(path).toList)

  def assetLoadComplete(key: AssetPath, loaded: Boolean): AssetBundleTracker =
    AssetBundleTracker(register.map(_.assetLoadComplete(key, loaded)))

  def checkBundleStatus(key: BindingKey): Option[AssetBundleStatus] =
    findBundleByKey(key).map(_.status)
}
object AssetBundleTracker {
  val empty: AssetBundleTracker =
    new AssetBundleTracker(Nil)
}
final class AssetBundle(val key: BindingKey, val assetCount: Int, val assets: Map[AssetPath, AssetToLoad]) {
  def assetLoadComplete(assetPath: AssetPath, loaded: Boolean): AssetBundle =
    new AssetBundle(
      key,
      assetCount,
      assets.updatedWith(assetPath) {
        case None    => None
        case Some(v) => Some(new AssetToLoad(v.asset, true, loaded))
      }
    )

  def status: AssetBundleStatus = {
    val assetList         = assets.toList
    val count             = assetList.length
    val errors            = assetList.filter(p => p._2.complete && !p._2.loaded).map(_._1)
    val errorCount        = errors.length
    val successes         = assetList.filter(p => p._2.complete && p._2.loaded).map(_._1)
    val successCount      = successes.length
    val combined          = errorCount + successCount
    val percentage        = Math.round(100.0d * combined.toDouble / count.toDouble).toInt
    val clampedPercentage = Math.min(100, Math.max(0, percentage))

    if (errorCount + successCount < count) {
      AssetBundleStatus.LoadInProgress(clampedPercentage)
    } else if (errorCount > 0) {
      AssetBundleStatus.LoadFailed(clampedPercentage, errors)
    } else {
      AssetBundleStatus.LoadComplete
    }
  }
}
final class AssetToLoad(val asset: AssetTypePrimitive, val complete: Boolean, val loaded: Boolean)

sealed trait AssetBundleStatus {
  val percent: Int
}
object AssetBundleStatus {
  case object LoadComplete extends AssetBundleStatus {
    val percent: Int = 100
  }
  final case class LoadFailed(percent: Int, failures: List[AssetPath]) extends AssetBundleStatus
  final case class LoadInProgress(percent: Int)                        extends AssetBundleStatus
}

sealed trait AssetBundleLoaderEvent
object AssetBundleLoaderEvent {
  // commands:
  final case class Load(key: BindingKey, assets: Set[AssetType]) extends AssetBundleLoaderEvent
  final case class Retry(key: BindingKey)                        extends AssetBundleLoaderEvent

  // events:
  final case class Start(key: BindingKey)                       extends AssetBundleLoaderEvent
  final case class PercentLoaded(key: BindingKey, percent: Int) extends AssetBundleLoaderEvent
  final case class Success(key: BindingKey)                     extends AssetBundleLoaderEvent
  final case class Failure(key: BindingKey)                     extends AssetBundleLoaderEvent
}
