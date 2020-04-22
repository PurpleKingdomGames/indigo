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

object AssetBundleLoaderTests extends TestSuite {

  val defaultAssets: List[AssetTypePrimitive] =
    List(
      AssetType.Image(AssetName("image 1"), AssetPath("/image_1.png")),
      AssetType.Image(AssetName("image 2"), AssetPath("/image_2.png")),
      AssetType.Image(AssetName("image 3"), AssetPath("/image_3.png"))
    )

  var tests: Tests =
    Tests {

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

      }

    }

}
