package indigoextras.subsystems.assetbundleloader

import indigo.shared.subsystems.SubSystem
import indigo.shared.events.GlobalEvent
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.assets.AssetType
import indigo.shared.datatypes.BindingKey
import indigo.shared.assets.AssetPath
import indigo.shared.assets.AssetTypePrimitive
import indigo.shared.events.AssetEvent
import indigo.shared.FrameContext

// Provides "at least once" message delivery for updates on a bundle's loading status.
final case class AssetBundleLoader(tracker: AssetBundleTracker) extends SubSystem {

  type EventType = GlobalEvent

  val eventFilter: GlobalEvent => Option[GlobalEvent] = {
    case e: AssetBundleLoaderEvent => Some(e)
    case e: AssetEvent             => Some(e)
    case _                         => None
  }

  def update(frameContext: FrameContext): GlobalEvent => Outcome[AssetBundleLoader] = {
    // Asset Bundle Loader Commands
    case AssetBundleLoaderEvent.Load(key, assets) =>
      createBeginLoadingOutcome(key, assets)

    case AssetBundleLoaderEvent.Retry(key) =>
      tracker.findBundleByKey(key).map(_.giveAssetSet) match {
        case None         => Outcome(this)
        case Some(assets) => createBeginLoadingOutcome(key, assets)
      }

    // Asset Response Events
    case AssetEvent.AssetBatchLoaded(Some(key), true) if tracker.containsBundle(key) =>
      Outcome(this)
        .addGlobalEvents(AssetBundleLoaderEvent.Success(key))

    case AssetEvent.AssetBatchLoaded(Some(key), false) if tracker.containsAssetFromKey(key) =>
      // In this case the "batch" will consist of one item and
      // the BindingKey is actually the AssetPath value and we
      // know the asset is in one of our bundles.
      processAssetUpdateEvent(AssetPath(key.value), true)

    case AssetEvent.AssetBatchLoadError(Some(key)) if tracker.containsBundle(key) =>
      Outcome(this)
        .addGlobalEvents(AssetBundleLoaderEvent.Failure(key))

    case AssetEvent.AssetBatchLoadError(Some(key)) if tracker.containsAssetFromKey(key) =>
      // In this case the "batch" will consist of one item and
      // the BindingKey is actually the AssetPath value and we
      // know the asset is in one of our bundles.
      processAssetUpdateEvent(AssetPath(key.value), false)

    // Everything else.
    case _ =>
      Outcome(this)
  }

  def render(frameContext: FrameContext): SceneUpdateFragment =
    SceneUpdateFragment.empty

  private def createBeginLoadingOutcome(key: BindingKey, assets: Set[AssetType]): Outcome[AssetBundleLoader] = {
    val assetPrimitives = AssetType.flattenAssetList(assets.toList)

    val events: List[GlobalEvent] =
      assetPrimitives.toList
        .map(asset => AssetEvent.LoadAsset(asset, Some(BindingKey(asset.path.value)), false))

    Outcome(
      this.copy(
        tracker.addBundle(key, assetPrimitives)
      )
    ).addGlobalEvents(AssetBundleLoaderEvent.Started(key) :: events)
  }

  private def processAssetUpdateEvent(path: AssetPath, completedSuccessfully: Boolean): Outcome[AssetBundleLoader] = {
    val updatedTracker =
      tracker.assetLoadComplete(path, completedSuccessfully)

    val statusBasedEvents: List[GlobalEvent] =
      updatedTracker.register
        .filter(_.containsAsset(path))
        .flatMap { bundle =>
          bundle.status match {
            case AssetBundleStatus.LoadComplete(completed, count) =>
              List[GlobalEvent](
                AssetBundleLoaderEvent.LoadProgress(bundle.key, 100, completed, count),
                AssetEvent.LoadAssetBatch(bundle.giveAssetSet, Some(bundle.key), true)
              )

            case AssetBundleStatus.LoadFailed(percent, completed, count, _) =>
              List[GlobalEvent](
                AssetBundleLoaderEvent.LoadProgress(bundle.key, percent, completed, count),
                AssetEvent.AssetBatchLoadError(Some(bundle.key))
              )

            case AssetBundleStatus.LoadInProgress(percent, completed, count) =>
              List[GlobalEvent](
                AssetBundleLoaderEvent.LoadProgress(bundle.key, percent, completed, count)
              )
          }
        }

    Outcome(
      this.copy(
        tracker = updatedTracker
      )
    ).addGlobalEvents(statusBasedEvents)
  }
}
object AssetBundleLoader {
  val subSystem: AssetBundleLoader =
    AssetBundleLoader(AssetBundleTracker.empty)
}

sealed trait AssetBundleLoaderEvent extends GlobalEvent
object AssetBundleLoaderEvent {
  // commands
  final case class Load(key: BindingKey, assets: Set[AssetType]) extends AssetBundleLoaderEvent
  final case class Retry(key: BindingKey)                        extends AssetBundleLoaderEvent

  // result events
  final case class Started(key: BindingKey)                                                extends AssetBundleLoaderEvent
  final case class LoadProgress(key: BindingKey, percent: Int, completed: Int, total: Int) extends AssetBundleLoaderEvent
  final case class Success(key: BindingKey)                                                extends AssetBundleLoaderEvent
  final case class Failure(key: BindingKey)                                                extends AssetBundleLoaderEvent
}

final case class AssetBundleTracker(val register: List[AssetBundle]) {
  val bundleCount: Int =
    register.length

  def addBundle(key: BindingKey, assets: List[AssetTypePrimitive]): AssetBundleTracker =
    if (assets.isEmpty || findBundleByKey(key).isDefined) this
    else {
      val newBundle =
        new AssetBundle(
          key,
          assets.size,
          assets.map { assetType =>
            (assetType.path -> new AssetToLoad(assetType, false, false))
          }.toMap
        )

      AssetBundleTracker(register :+ newBundle)
    }

  def findBundleByKey(key: BindingKey): Option[AssetBundle] =
    register.find(_.key === key)

  def findAssetByPath(path: AssetPath): List[AssetToLoad] =
    register.flatMap(_.assets.get(path).toList)

  def containsBundle(key: BindingKey): Boolean =
    register.exists(_.key === key)

  def containsAssetFromKey(key: BindingKey): Boolean =
    containsAsset(AssetPath(key.value))

  def containsAsset(path: AssetPath): Boolean =
    register.exists(_.containsAsset(path))

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
      AssetBundleStatus.LoadInProgress(clampedPercentage, combined, count)
    } else if (errorCount > 0) {
      AssetBundleStatus.LoadFailed(clampedPercentage, combined, count, errors)
    } else {
      AssetBundleStatus.LoadComplete(combined, count)
    }
  }

  def giveAssetLoadState(path: AssetPath): Option[AssetToLoad] =
    assets.get(path)

  def giveAssetSet: Set[AssetType] =
    assets.toList.map(_._2.asset).toSet

  def containsAsset(path: AssetPath): Boolean =
    assets.contains(path)
}
final class AssetToLoad(val asset: AssetTypePrimitive, val complete: Boolean, val loaded: Boolean)

sealed trait AssetBundleStatus {
  val percent: Int
  val completed: Int
  val count: Int
}
object AssetBundleStatus {
  final case class LoadComplete(completed: Int, count: Int) extends AssetBundleStatus {
    val percent: Int = 100
  }
  final case class LoadFailed(percent: Int, completed: Int, count: Int, failures: List[AssetPath]) extends AssetBundleStatus
  final case class LoadInProgress(percent: Int, completed: Int, count: Int)                        extends AssetBundleStatus
}
