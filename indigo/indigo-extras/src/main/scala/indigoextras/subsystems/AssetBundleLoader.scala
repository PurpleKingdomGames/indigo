package indigoextras.subsystems

import indigo.shared.Outcome
import indigo.shared.assets.AssetPath
import indigo.shared.assets.AssetType
import indigo.shared.assets.AssetTypePrimitive
import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey
import indigo.shared.events.AssetEvent
import indigo.shared.events.GlobalEvent
import indigo.shared.events.SubSystemEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemId

// Provides "at least once" message delivery for updates on a bundle's loading status.
final class AssetBundleLoader[Model] extends SubSystem[Model]:
  type EventType      = GlobalEvent
  type SubSystemModel = AssetBundleTracker
  type ReferenceData  = Unit

  val id: SubSystemId = SubSystemId("[indigo_AssetBundleLoader_subsystem]")

  val eventFilter: GlobalEvent => Option[GlobalEvent] = {
    case e: AssetBundleLoaderEvent => Some(e)
    case e: AssetEvent             => Some(e)
    case _                         => None
  }

  def reference(model: Model): ReferenceData =
    ()

  def initialModel: Outcome[AssetBundleTracker] =
    Outcome(AssetBundleTracker.empty)

  private given CanEqual[Option[Set[AssetType]], Option[Set[AssetType]]] = CanEqual.derived

  def update(
      context: SubSystemContext[ReferenceData],
      tracker: AssetBundleTracker
  ): GlobalEvent => Outcome[AssetBundleTracker] =
    // Asset Bundle Loader Commands
    case AssetBundleLoaderEvent.Load(key, assets) =>
      createBeginLoadingOutcome(key, assets, tracker)

    case AssetBundleLoaderEvent.Retry(key) =>
      tracker.findBundleByKey(key).map(_.giveAssetSet) match {
        case None         => Outcome(tracker)
        case Some(assets) => createBeginLoadingOutcome(key, assets, tracker)
      }

    // Asset Response Events
    case AssetEvent.AssetBatchLoaded(key, _, true) if tracker.containsBundle(key) =>
      Outcome(tracker)
        .addGlobalEvents(AssetBundleLoaderEvent.Success(key))

    case AssetEvent.AssetBatchLoaded(key, _, false) if tracker.containsAssetFromKey(key) =>
      // In this case the "batch" will consist of one item and
      // the BindingKey is actually the AssetPath value and we
      // know the asset is in one of our bundles.
      processAssetUpdateEvent(AssetPath(key.toString), true, tracker)

    case AssetEvent.AssetBatchLoadError(key, message) if tracker.containsBundle(key) =>
      Outcome(tracker)
        .addGlobalEvents(AssetBundleLoaderEvent.Failure(key, message))

    case AssetEvent.AssetBatchLoadError(key, _) if tracker.containsAssetFromKey(key) =>
      // In this case the "batch" will consist of one item and
      // the BindingKey is actually the AssetPath value and we
      // know the asset is in one of our bundles.
      processAssetUpdateEvent(AssetPath(key.toString), false, tracker)

    // Everything else.
    case _ =>
      Outcome(tracker)

  def present(
      context: SubSystemContext[ReferenceData],
      model: AssetBundleTracker
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

  private def createBeginLoadingOutcome(
      key: BindingKey,
      assets: Set[AssetType],
      tracker: AssetBundleTracker
  ): Outcome[AssetBundleTracker] =
    val assetPrimitives = AssetType.flattenAssetList(assets.toList)

    val events: Batch[GlobalEvent] =
      Batch.fromList(
        assetPrimitives
          .map(asset => AssetEvent.LoadAsset(asset, BindingKey(asset.path.toString), false))
      )

    Outcome(
      tracker.addBundle(key, assetPrimitives)
    ).addGlobalEvents(AssetBundleLoaderEvent.Started(key) :: events)

  private def processAssetUpdateEvent(
      path: AssetPath,
      completedSuccessfully: Boolean,
      tracker: AssetBundleTracker
  ): Outcome[AssetBundleTracker] =
    val updatedTracker =
      tracker.assetLoadComplete(path, completedSuccessfully)

    val statusBasedEvents: List[GlobalEvent] =
      updatedTracker.register
        .filter(_.containsAsset(path))
        .flatMap { bundle =>
          bundle.status match
            case AssetBundleStatus.LoadComplete(completed, count) =>
              List(
                AssetBundleLoaderEvent.LoadProgress(bundle.key, 100, completed, count),
                AssetEvent.LoadAssetBatch(bundle.giveAssetSet, bundle.key, true)
              )

            case AssetBundleStatus.LoadFailed(percent, completed, count, _) =>
              List(
                AssetBundleLoaderEvent.LoadProgress(bundle.key, percent, completed, count),
                AssetEvent
                  .AssetBatchLoadError(bundle.key, s"Asset batch with key '${bundle.key.toString}' failed to load")
              )

            case AssetBundleStatus.LoadInProgress(percent, completed, count) =>
              List(
                AssetBundleLoaderEvent.LoadProgress(bundle.key, percent, completed, count)
              )
        }

    Outcome(updatedTracker, Batch.fromList(statusBasedEvents))

object AssetBundleLoader:

  def apply[Model]: AssetBundleLoader[Model] =
    new AssetBundleLoader[Model]

enum AssetBundleLoaderEvent extends GlobalEvent derives CanEqual:
  // commands
  case Load(key: BindingKey, assets: Set[AssetType]) extends AssetBundleLoaderEvent with SubSystemEvent
  case Retry(key: BindingKey)                        extends AssetBundleLoaderEvent with SubSystemEvent

  // result events
  case Started(key: BindingKey)                                                extends AssetBundleLoaderEvent
  case LoadProgress(key: BindingKey, percent: Int, completed: Int, total: Int) extends AssetBundleLoaderEvent
  case Success(key: BindingKey)                                                extends AssetBundleLoaderEvent
  case Failure(key: BindingKey, message: String)                               extends AssetBundleLoaderEvent

final case class AssetBundleTracker(val register: List[AssetBundle]):
  val bundleCount: Int =
    register.length

  def addBundle(key: BindingKey, assets: List[AssetTypePrimitive]): AssetBundleTracker =
    if assets.isEmpty || findBundleByKey(key).isDefined then this
    else
      val newBundle =
        AssetBundle(
          key,
          assets.size,
          assets.map { assetType =>
            assetType.path -> AssetToLoad(assetType, false, false)
          }.toMap
        )

      AssetBundleTracker(register ++ List(newBundle))

  def findBundleByKey(key: BindingKey): Option[AssetBundle] =
    register.find(_.key == key)

  def findAssetByPath(path: AssetPath): List[AssetToLoad] =
    register.flatMap(_.assets.get(path).toList)

  def containsBundle(key: BindingKey): Boolean =
    register.exists(_.key == key)

  def containsAssetFromKey(key: BindingKey): Boolean =
    containsAsset(AssetPath(key.toString))

  def containsAsset(path: AssetPath): Boolean =
    register.exists(_.containsAsset(path))

  def assetLoadComplete(key: AssetPath, loaded: Boolean): AssetBundleTracker =
    AssetBundleTracker(register.map(_.assetLoadComplete(key, loaded)))

  def checkBundleStatus(key: BindingKey): Option[AssetBundleStatus] =
    findBundleByKey(key).map(_.status)

object AssetBundleTracker:
  val empty: AssetBundleTracker =
    AssetBundleTracker(Nil)

final case class AssetBundle(key: BindingKey, assetCount: Int, assets: Map[AssetPath, AssetToLoad]):
  private given CanEqual[Option[AssetToLoad], Option[AssetToLoad]] = CanEqual.derived

  def assetLoadComplete(assetPath: AssetPath, loaded: Boolean): AssetBundle =
    AssetBundle(
      key,
      assetCount,
      assets.updatedWith(assetPath) {
        case None    => None
        case Some(v) => Some(AssetToLoad(v.asset, true, loaded))
      }
    )

  def status: AssetBundleStatus =
    val assetList         = assets.toList
    val count             = assetList.length
    val errors            = assetList.filter(p => p._2.complete && !p._2.loaded).map(_._1)
    val errorCount        = errors.length
    val successes         = assetList.filter(p => p._2.complete && p._2.loaded).map(_._1)
    val successCount      = successes.length
    val combined          = errorCount + successCount
    val percentage        = Math.round(100.0d * combined.toDouble / count.toDouble).toInt
    val clampedPercentage = Math.min(100, Math.max(0, percentage))

    if errorCount + successCount < count then AssetBundleStatus.LoadInProgress(clampedPercentage, combined, count)
    else if errorCount > 0 then AssetBundleStatus.LoadFailed(clampedPercentage, combined, count, errors)
    else AssetBundleStatus.LoadComplete(combined, count)

  def giveAssetLoadState(path: AssetPath): Option[AssetToLoad] =
    assets.get(path)

  def giveAssetSet: Set[AssetType] =
    assets.toList.map(_._2.asset).toSet

  def containsAsset(path: AssetPath): Boolean =
    assets.contains(path)

final case class AssetToLoad(asset: AssetTypePrimitive, complete: Boolean, loaded: Boolean) derives CanEqual

enum AssetBundleStatus(val percent: Int, val completed: Int, val count: Int) derives CanEqual:
  case LoadComplete(completedLoading: Int, loadCount: Int) extends AssetBundleStatus(100, completedLoading, loadCount)

  case LoadFailed(percentLoaded: Int, completedLoading: Int, loadCount: Int, failures: List[AssetPath])
      extends AssetBundleStatus(percentLoaded, completedLoading, loadCount)

  case LoadInProgress(percentLoaded: Int, completedLoading: Int, loadCount: Int)
      extends AssetBundleStatus(percentLoaded: Int, completedLoading: Int, loadCount: Int)
