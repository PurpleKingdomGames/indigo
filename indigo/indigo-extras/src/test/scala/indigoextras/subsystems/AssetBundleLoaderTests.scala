package indigoextras.subsystems

import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetPath
import indigo.shared.assets.AssetType
import indigo.shared.assets.AssetTypePrimitive
import indigo.shared.datatypes.BindingKey
import indigo.shared.events.AssetEvent

class AssetBundleLoaderTests extends munit.FunSuite {

  import indigoextras.subsystems.FakeSubSystemFrameContext._

  val defaultAssets: List[AssetTypePrimitive] =
    List(
      AssetType.Image(AssetName("image 1"), AssetPath("/image_1.png")),
      AssetType.Image(AssetName("image 2"), AssetPath("/image_2.png")),
      AssetType.Image(AssetName("image 3"), AssetPath("/image_3.png"))
    )

  test("AssetBundleLoader - Journey (happy path)") {

    val loader  = AssetBundleLoader
    val tracker = AssetBundleTracker.empty

    val key = BindingKey("test")

    // Someone requests that a bundle of assets be loaded.
    val loadOutcome = loader.update(context(0), tracker)(AssetBundleLoaderEvent.Load(key, defaultAssets.toSet))

    // That results in events being triggered to load, but not process, each asset
    assertEquals(defaultAssets.length + 1, loadOutcome.unsafeGlobalEvents.length)
    assertEquals(
      defaultAssets.forall { asset =>
        loadOutcome.unsafeGlobalEvents.contains(AssetEvent.LoadAsset(asset, BindingKey(asset.path.toString), false))
      },
      true
    )
    assertEquals(loadOutcome.unsafeGlobalEvents.contains(AssetBundleLoaderEvent.Started(key)), true)

    // As each asset comes in, the status is checked and events are emitted.
    val nextLoader1 =
      loader.update(context(0), loadOutcome.unsafeGet)(AssetEvent.AssetBatchLoaded(BindingKey("/image_1.png"), false))
    assertEquals(nextLoader1.unsafeGlobalEvents.length, 1)
    assertEquals(nextLoader1.unsafeGlobalEvents.contains(AssetBundleLoaderEvent.LoadProgress(key, 33, 1, 3)), true)

    val nextLoader2 =
      loader.update(context(0), nextLoader1.unsafeGet)(AssetEvent.AssetBatchLoaded(BindingKey("/image_2.png"), false))
    assertEquals(nextLoader2.unsafeGlobalEvents.length, 1)
    assertEquals(nextLoader2.unsafeGlobalEvents.contains(AssetBundleLoaderEvent.LoadProgress(key, 67, 2, 3)), true)

    // Eventually all assets are loaded individually, and an event is emmitted to
    // load the whole bundle and also to process it.
    val nextLoader3 =
      loader.update(context(0), nextLoader2.unsafeGet)(AssetEvent.AssetBatchLoaded(BindingKey("/image_3.png"), false))
    assertEquals(nextLoader3.unsafeGlobalEvents.length, 2)
    assertEquals(nextLoader3.unsafeGlobalEvents.contains(AssetBundleLoaderEvent.LoadProgress(key, 100, 3, 3)), true)
    assertEquals(
      nextLoader3.unsafeGlobalEvents.contains(AssetEvent.LoadAssetBatch(defaultAssets.toSet, key, true)),
      true
    )

    // Once the whole bundle has finished, a completion event is emitted.
    val finalLoader = loader.update(context(0), nextLoader3.unsafeGet)(AssetEvent.AssetBatchLoaded(key, true))
    assertEquals(finalLoader.unsafeGlobalEvents.length, 1)
    assertEquals(finalLoader.unsafeGlobalEvents.contains(AssetBundleLoaderEvent.Success(key)), true)
    val asset2 = finalLoader.unsafeGet.findBundleByKey(key).get.giveAssetLoadState(AssetPath("/image_2.png")).get
    assertEquals(asset2.asset.name, AssetName("image 2"))
    assertEquals(asset2.asset.path, AssetPath("/image_2.png"))
    assertEquals(asset2.complete, true)
    assertEquals(asset2.loaded, true)
  }

  test("AssetBundleLoader - Journey (unhappy path)") {
    val loader  = AssetBundleLoader
    val tracker = AssetBundleTracker.empty

    val key = BindingKey("test")

    // Someone requests that a bundle of assets be loaded.
    val loadOutcome = loader.update(context(0), tracker)(AssetBundleLoaderEvent.Load(key, defaultAssets.toSet))

    // That results in events being triggered to load, but not process, each asset
    assertEquals(defaultAssets.length + 1, loadOutcome.unsafeGlobalEvents.length)
    assertEquals(
      defaultAssets.forall { asset =>
        loadOutcome.unsafeGlobalEvents.contains(AssetEvent.LoadAsset(asset, BindingKey(asset.path.toString), false))
      },
      true
    )
    assertEquals(loadOutcome.unsafeGlobalEvents.contains(AssetBundleLoaderEvent.Started(key)), true)

    // As each asset comes in, the status is checked and events are emitted.
    val nextLoader1 =
      loader.update(context(0), loadOutcome.unsafeGet)(AssetEvent.AssetBatchLoaded(BindingKey("/image_1.png"), false))
    assertEquals(nextLoader1.unsafeGlobalEvents.length, 1)
    assertEquals(nextLoader1.unsafeGlobalEvents.contains(AssetBundleLoaderEvent.LoadProgress(key, 33, 1, 3)), true)

    val nextLoader2 =
      loader.update(context(0), nextLoader1.unsafeGet)(AssetEvent.AssetBatchLoaded(BindingKey("/image_2.png"), false))
    assertEquals(nextLoader2.unsafeGlobalEvents.length, 1)
    assertEquals(nextLoader2.unsafeGlobalEvents.contains(AssetBundleLoaderEvent.LoadProgress(key, 67, 2, 3)), true)

    // All assets are loaded individually, but one of them fails.
    val nextLoader3 = loader.update(context(0), nextLoader2.unsafeGet)(
      AssetEvent.AssetBatchLoadError(BindingKey("/image_3.png"), "error")
    )
    assertEquals(nextLoader3.unsafeGlobalEvents.length, 2)
    assertEquals(nextLoader3.unsafeGlobalEvents.contains(AssetBundleLoaderEvent.LoadProgress(key, 100, 3, 3)), true)
    assertEquals(
      nextLoader3.unsafeGlobalEvents.contains(
        AssetEvent.AssetBatchLoadError(key, "Asset batch with key 'test' failed to load")
      ),
      true
    )

    // Eventually the whole bundle is complete, but in a failed state, and
    // a completion event is emitted listing the errors.
    val finalLoader = loader.update(context(0), nextLoader3.unsafeGet)(
      AssetEvent.AssetBatchLoadError(key, "Asset batch with key 'test' failed to load")
    )
    assertEquals(finalLoader.unsafeGlobalEvents.length, 1)
    assertEquals(
      finalLoader.unsafeGlobalEvents.contains(
        AssetBundleLoaderEvent.Failure(key, "Asset batch with key 'test' failed to load")
      ),
      true
    )
    val asset1 = finalLoader.unsafeGet.findBundleByKey(key).get.giveAssetLoadState(AssetPath("/image_1.png")).get
    assertEquals(asset1.asset.name, AssetName("image 1"))
    assertEquals(asset1.asset.path, AssetPath("/image_1.png"))
    assertEquals(asset1.complete, true)
    assertEquals(asset1.loaded, true)
    val asset2 = finalLoader.unsafeGet.findBundleByKey(key).get.giveAssetLoadState(AssetPath("/image_2.png")).get
    assertEquals(asset2.asset.name, AssetName("image 2"))
    assertEquals(asset2.asset.path, AssetPath("/image_2.png"))
    assertEquals(asset2.complete, true)
    assertEquals(asset2.loaded, true)
    val asset3 = finalLoader.unsafeGet.findBundleByKey(key).get.giveAssetLoadState(AssetPath("/image_3.png")).get
    assertEquals(asset3.asset.name, AssetName("image 3"))
    assertEquals(asset3.asset.path, AssetPath("/image_3.png"))
    assertEquals(asset3.complete, true)
    assertEquals(asset3.loaded, false)
  }

  test("AssetBundleTracker.Can add a bundle to the tracker") {
    assertEquals(AssetBundleTracker.empty.bundleCount, 0)
    assertEquals(AssetBundleTracker.empty.addBundle(BindingKey("a"), defaultAssets).bundleCount, 1)
  }

  test("AssetBundleTracker.Doesn't add empty bundles") {
    assertEquals(AssetBundleTracker.empty.bundleCount, 0)
    assertEquals(AssetBundleTracker.empty.addBundle(BindingKey("a"), Nil).bundleCount, 0)
  }

  test("AssetBundleTracker.Doesn't re-add or replace bundles with existing identical keys") {
    val assets: List[AssetTypePrimitive] =
      List(
        AssetType.Image(AssetName("image 1"), AssetPath("/image_1.png")),
        AssetType.Image(AssetName("image 2"), AssetPath("/image_2.png")),
        AssetType.Image(AssetName("image 3"), AssetPath("/image_3.png")),
        AssetType.Image(AssetName("image 4"), AssetPath("/image_4.png"))
      )

    val tracker = AssetBundleTracker.empty
    assertEquals(tracker.bundleCount, 0)
    val tracker1 = tracker.addBundle(BindingKey("a"), defaultAssets)
    assertEquals(tracker1.bundleCount, 1)
    val tracker2 = tracker1.addBundle(BindingKey("a"), assets)
    assertEquals(tracker2.bundleCount, 1)
    assertEquals(tracker2.findBundleByKey(BindingKey("a")).get.assetCount, 3)
  }

  test("AssetBundleTracker.Can get the asset count for a bundle in the tracker") {
    val tracker = AssetBundleTracker.empty.addBundle(BindingKey("a"), defaultAssets)
    assertEquals(tracker.findBundleByKey(BindingKey("a")).get.assetCount, 3)
  }

  test("AssetBundleTracker.Initially, all assets are not completed or loaded") {
    val tracker = AssetBundleTracker.empty.addBundle(BindingKey("a"), defaultAssets)
    assertEquals(
      tracker.findBundleByKey(BindingKey("a")).get.assets.toList.forall(p => !p._2.complete && !p._2.loaded),
      true
    )
  }

  test("AssetBundleTracker.Can update an asset in a bundle with a completed + loaded state (success)") {
    val tracker =
      AssetBundleTracker.empty
        .addBundle(BindingKey("bundle"), defaultAssets)
        .assetLoadComplete(AssetPath("/image_1.png"), true)

    val asset = tracker
      .findBundleByKey(BindingKey("bundle"))
      .get
      .assets
      .get(AssetPath("/image_1.png"))
      .get

    assertEquals(asset.complete, true)
    assertEquals(asset.loaded, true)
  }

  test("AssetBundleTracker.Can update an asset in a bundle with a completed + loaded state (failure)") {
    val tracker =
      AssetBundleTracker.empty
        .addBundle(BindingKey("bundle"), defaultAssets)
        .assetLoadComplete(AssetPath("/image_1.png"), false)

    val asset = tracker
      .findBundleByKey(BindingKey("bundle"))
      .get
      .assets
      .get(AssetPath("/image_1.png"))
      .get

    assertEquals(asset.complete, true)
    assertEquals(asset.loaded, false)
  }

  test("AssetBundleTracker.Bundle status: Can check if all assets in a bundle are completed (success - 100%)") {
    val tracker =
      AssetBundleTracker.empty
        .addBundle(BindingKey("a"), defaultAssets)
        .assetLoadComplete(AssetPath("/image_1.png"), true)
        .assetLoadComplete(AssetPath("/image_2.png"), true)
        .assetLoadComplete(AssetPath("/image_3.png"), true)

    tracker.checkBundleStatus(BindingKey("a")) match {
      case Some(AssetBundleStatus.LoadComplete(completed, count)) =>
        assertEquals(completed, 3)
        assertEquals(count, 3)

      case _ =>
        assertEquals("failed", "fail")
    }
  }

  test("AssetBundleTracker.Bundle status: Can check if all assets in a bundle are completed (failed - ?%)") {
    val tracker =
      AssetBundleTracker.empty
        .addBundle(BindingKey("a"), defaultAssets)
        .assetLoadComplete(AssetPath("/image_1.png"), true)
        .assetLoadComplete(AssetPath("/image_2.png"), false)
        .assetLoadComplete(AssetPath("/image_3.png"), false)

    tracker.checkBundleStatus(BindingKey("a")) match {
      case Some(AssetBundleStatus.LoadFailed(percent, completed, count, failures)) =>
        assertEquals(percent, 100)
        assertEquals(completed, 3)
        assertEquals(count, 3)
        assertEquals(failures, List(AssetPath("/image_2.png"), AssetPath("/image_3.png")))

      case _ =>
        assertEquals("failed", "fail")
    }
  }

  test("AssetBundleTracker.Bundle status: Can check if a load is incomplete with percent done") {
    val tracker =
      AssetBundleTracker.empty
        .addBundle(BindingKey("a"), defaultAssets)
        .assetLoadComplete(AssetPath("/image_1.png"), true)
        .assetLoadComplete(AssetPath("/image_2.png"), false)

    tracker.checkBundleStatus(BindingKey("a")) match {
      case Some(AssetBundleStatus.LoadInProgress(percent, completed, count)) =>
        assertEquals(percent, 67)
        assertEquals(completed, 2)
        assertEquals(count, 3)

      case _ =>
        assertEquals("failed", "fail")
    }
  }

  test("AssetBundleTracker.Assets are not unique to a bundle, all instances are set as loaded") {
    val lookup =
      AssetBundleTracker.empty
        .addBundle(BindingKey("a"), defaultAssets)
        .addBundle(BindingKey("b"), defaultAssets)
        .assetLoadComplete(AssetPath("/image_1.png"), true)
        .findAssetByPath(AssetPath("/image_1.png"))

    assertEquals(lookup.forall(_.loaded == true), true)
  }

  test("AssetBundleTracker.Can look up the loaded state of all instances of an asset") {
    val lookup1 =
      AssetBundleTracker.empty
        .addBundle(BindingKey("a"), defaultAssets)
        .findAssetByPath(AssetPath("/image_1.png"))

    assertEquals(lookup1.length, 1)
    assertEquals(lookup1.headOption.get.asset.name, AssetName("image 1"))
    assertEquals(lookup1.headOption.get.asset.path, AssetPath("/image_1.png"))
    assertEquals(lookup1.headOption.get.loaded, false)

    val lookup2 =
      AssetBundleTracker.empty
        .addBundle(BindingKey("a"), defaultAssets)
        .addBundle(BindingKey("b"), defaultAssets)
        .findAssetByPath(AssetPath("/image_1.png"))

    assertEquals(lookup2.length, 2)
    assertEquals(lookup2.forall(_.asset.name == AssetName("image 1")), true)
    assertEquals(lookup2.forall(_.asset.path == AssetPath("/image_1.png")), true)
    assertEquals(lookup2.forall(_.loaded == false), true)
  }

  test("AssetBundleTracker.Does the tracker contain a bundle?") {
    val tracker = AssetBundleTracker.empty
      .addBundle(BindingKey("a"), defaultAssets)
      .addBundle(BindingKey("b"), defaultAssets)

    assertEquals(tracker.containsBundle(BindingKey("a")), true)
    assertEquals(tracker.containsBundle(BindingKey("b")), true)
    assertEquals(tracker.containsBundle(BindingKey("c")), false)
  }

  test("AssetBundleTracker.Does the tracker contain an asset in any bundle?") {
    val tracker = AssetBundleTracker.empty
      .addBundle(BindingKey("a"), defaultAssets)

    assertEquals(tracker.containsAsset(AssetPath("/image_1.png")), true)
    assertEquals(tracker.containsAsset(AssetPath("NOPE")), false)
  }

}
