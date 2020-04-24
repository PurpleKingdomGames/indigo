package com.example.preloader

import utest._
import indigo.shared.datatypes.BindingKey
import indigo.shared.assets.AssetType
import indigo.shared.assets.AssetPath
import indigo.shared.assets.AssetName
import com.example.preloader.AssetBundleStatus.LoadComplete
import com.example.preloader.AssetBundleStatus.LoadFailed
import com.example.preloader.AssetBundleStatus.LoadInProgress
import indigo.shared.assets.AssetTypePrimitive
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.events.AssetEvent

object AssetBundleLoaderTests extends TestSuite {

  val defaultAssets: List[AssetTypePrimitive] =
    List(
      AssetType.Image(AssetName("image 1"), AssetPath("/image_1.png")),
      AssetType.Image(AssetName("image 2"), AssetPath("/image_2.png")),
      AssetType.Image(AssetName("image 3"), AssetPath("/image_3.png"))
    )

  var tests: Tests =
    Tests {

      "AssetBundleLoader - Journey (happy path)" - {
        val gt = GameTime.zero
        val d  = Dice.loaded(0)

        val loader = AssetBundleLoader.subSystem

        val key = BindingKey("test")

        // Someone requests that a bundle of assets be loaded.
        val loadOutcome = loader.update(gt, d)(AssetBundleLoaderEvent.Load(key, defaultAssets.toSet))

        // That results in events being triggered to load, but not process, each asset
        defaultAssets.length + 1 ==> loadOutcome.globalEvents.length
        defaultAssets.forall { asset =>
          loadOutcome.globalEvents.contains(AssetEvent.LoadAsset(asset, Some(BindingKey(asset.path.value)), false))
        } ==> true
        loadOutcome.globalEvents.contains(AssetBundleLoaderEvent.Started(key)) ==> true

        // As each asset comes in, the status is checked and events are emitted.
        val nextLoader1 = loadOutcome.state.update(gt, d)(AssetEvent.AssetBatchLoaded(Some(BindingKey("/image_1.png")), false))
        nextLoader1.globalEvents.length ==> 1
        nextLoader1.globalEvents.contains(AssetBundleLoaderEvent.PercentLoaded(key, 33)) ==> true

        val nextLoader2 = nextLoader1.state.update(gt, d)(AssetEvent.AssetBatchLoaded(Some(BindingKey("/image_2.png")), false))
        nextLoader2.globalEvents.length ==> 1
        nextLoader2.globalEvents.contains(AssetBundleLoaderEvent.PercentLoaded(key, 67)) ==> true

        // Eventually all assets are loaded individually, and an event is emmitted to
        // load the whole bundle and also to process it.
        val nextLoader3 = nextLoader2.state.update(gt, d)(AssetEvent.AssetBatchLoaded(Some(BindingKey("/image_3.png")), false))
        nextLoader3.globalEvents.length ==> 2
        nextLoader3.globalEvents.contains(AssetBundleLoaderEvent.PercentLoaded(key, 100)) ==> true
        nextLoader3.globalEvents.contains(AssetEvent.LoadAssetBatch(defaultAssets.toSet, Some(key), true)) ==> true

        // Once the whole bundle has finished, a completion event is emitted.
        val finalLoader = nextLoader3.state.update(gt, d)(AssetEvent.AssetBatchLoaded(Some(key), true))
        finalLoader.globalEvents.length ==> 1
        finalLoader.globalEvents.contains(AssetBundleLoaderEvent.Success(key)) ==> true
        val asset2 = finalLoader.state.tracker.findBundleByKey(key).get.giveAssetLoadState(AssetPath("/image_2.png")).get
        asset2.asset.name ==> AssetName("image 2")
        asset2.asset.path ==> AssetPath("/image_2.png")
        asset2.complete ==> true
        asset2.loaded ==> true
      }

      "AssetBundleLoader - Journey (unhappy path)" - {
        1 ==> 2

        //TODO
        // Someone requests that a bundle of assets be loaded.

        // That results in events being triggered to load, but not process, each asset

        // As each asset comes in, the status is checked and events are emitted.

        // All assets are loaded individually, but some of them fail.

        // Eventually the whole bundle is complete, but in a failed state, and
        // a completion event is emitted listing the errors.
      }

      "AssetBundleTracker" - {

        "Can add a bundle to the tracker" - {
          AssetBundleTracker.empty.bundleCount ==> 0
          AssetBundleTracker.empty.addBundle(BindingKey("a"), defaultAssets).bundleCount ==> 1
        }

        "Doesn't add empty bundles" - {
          AssetBundleTracker.empty.bundleCount ==> 0
          AssetBundleTracker.empty.addBundle(BindingKey("a"), Nil).bundleCount ==> 0
        }

        "Doesn't re-add or replace bundles with existing identical keys" - {
          val assets: List[AssetTypePrimitive] =
            List(
              AssetType.Image(AssetName("image 1"), AssetPath("/image_1.png")),
              AssetType.Image(AssetName("image 2"), AssetPath("/image_2.png")),
              AssetType.Image(AssetName("image 3"), AssetPath("/image_3.png")),
              AssetType.Image(AssetName("image 4"), AssetPath("/image_4.png"))
            )

          val tracker = AssetBundleTracker.empty
          tracker.bundleCount ==> 0
          val tracker1 = tracker.addBundle(BindingKey("a"), defaultAssets)
          tracker1.bundleCount ==> 1
          val tracker2 = tracker1.addBundle(BindingKey("a"), assets)
          tracker2.bundleCount ==> 1
          tracker2.findBundleByKey(BindingKey("a")).get.assetCount ==> 3
        }

        "Can get the asset count for a bundle in the tracker" - {
          val tracker = AssetBundleTracker.empty.addBundle(BindingKey("a"), defaultAssets)
          tracker.findBundleByKey(BindingKey("a")).get.assetCount ==> 3
        }

        "Initially, all assets are not completed or loaded" - {
          val tracker = AssetBundleTracker.empty.addBundle(BindingKey("a"), defaultAssets)
          tracker.findBundleByKey(BindingKey("a")).get.assets.toList.forall(p => !p._2.complete && !p._2.loaded) ==> true
        }

        "Can update an asset in a bundle with a completed + loaded state (success)" - {
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

          asset.complete ==> true
          asset.loaded ==> true
        }

        "Can update an asset in a bundle with a completed + loaded state (failure)" - {
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

          asset.complete ==> true
          asset.loaded ==> false
        }

        "Bundle status: Can check if all assets in a bundle are completed (success - 100%)" - {
          val tracker =
            AssetBundleTracker.empty
              .addBundle(BindingKey("a"), defaultAssets)
              .assetLoadComplete(AssetPath("/image_1.png"), true)
              .assetLoadComplete(AssetPath("/image_2.png"), true)
              .assetLoadComplete(AssetPath("/image_3.png"), true)

          tracker.checkBundleStatus(BindingKey("a")) match {
            case Some(LoadComplete) =>
              "passed" ==> "passed"

            case _ =>
              "failed" ==> "fail"
          }
        }

        "Bundle status: Can check if all assets in a bundle are completed (failed - ?%)" - {
          val tracker =
            AssetBundleTracker.empty
              .addBundle(BindingKey("a"), defaultAssets)
              .assetLoadComplete(AssetPath("/image_1.png"), true)
              .assetLoadComplete(AssetPath("/image_2.png"), false)
              .assetLoadComplete(AssetPath("/image_3.png"), false)

          tracker.checkBundleStatus(BindingKey("a")) match {
            case Some(LoadFailed(percent, failures)) =>
              percent ==> 100
              failures ==> List(AssetPath("/image_2.png"), AssetPath("/image_3.png"))

            case _ =>
              "failed" ==> "fail"
          }
        }

        "Bundle status: Can check if a load is incomplete with percent done" - {
          val tracker =
            AssetBundleTracker.empty
              .addBundle(BindingKey("a"), defaultAssets)
              .assetLoadComplete(AssetPath("/image_1.png"), true)
              .assetLoadComplete(AssetPath("/image_2.png"), false)

          tracker.checkBundleStatus(BindingKey("a")) match {
            case Some(LoadInProgress(percent)) =>
              percent ==> 67

            case _ =>
              "failed" ==> "fail"
          }
        }

        "Assets are not unique to a bundle, all instances are set as loaded" - {
          val lookup =
            AssetBundleTracker.empty
              .addBundle(BindingKey("a"), defaultAssets)
              .addBundle(BindingKey("b"), defaultAssets)
              .assetLoadComplete(AssetPath("/image_1.png"), true)
              .findAssetByPath(AssetPath("/image_1.png"))

          lookup.forall(_.loaded == true) ==> true
        }

        "Can look up the loaded state of all instances of an asset" - {
          val lookup1 =
            AssetBundleTracker.empty
              .addBundle(BindingKey("a"), defaultAssets)
              .findAssetByPath(AssetPath("/image_1.png"))

          lookup1.length ==> 1
          lookup1.headOption.get.asset.name ==> AssetName("image 1")
          lookup1.headOption.get.asset.path ==> AssetPath("/image_1.png")
          lookup1.headOption.get.loaded ==> false

          val lookup2 =
            AssetBundleTracker.empty
              .addBundle(BindingKey("a"), defaultAssets)
              .addBundle(BindingKey("b"), defaultAssets)
              .findAssetByPath(AssetPath("/image_1.png"))

          lookup2.length ==> 2
          lookup2.forall(_.asset.name == AssetName("image 1")) ==> true
          lookup2.forall(_.asset.path == AssetPath("/image_1.png")) ==> true
          lookup2.forall(_.loaded == false) ==> true
        }

        "Does the tracker contain a bundle?" - {
          val tracker = AssetBundleTracker.empty
            .addBundle(BindingKey("a"), defaultAssets)
            .addBundle(BindingKey("b"), defaultAssets)

          tracker.containsBundle(BindingKey("a")) ==> true
          tracker.containsBundle(BindingKey("b")) ==> true
          tracker.containsBundle(BindingKey("c")) ==> false
        }

        "Does the tracker contain an asset in any bundle?" - {
          val tracker = AssetBundleTracker.empty
            .addBundle(BindingKey("a"), defaultAssets)

          tracker.containsAsset(AssetPath("/image_1.png")) ==> true
          tracker.containsAsset(AssetPath("NOPE")) ==> false
        }

      }

    }

}
