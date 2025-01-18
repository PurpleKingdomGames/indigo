package indigo.benchmarks

import indigo.*
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*

import scala.annotation.nowarn

import scalajs.js
import scalajs.js.JSConverters.*

object BatchBenchmarks:

  val graphics1000 =
    (0 to 1000).map(_ => Graphic(Size(32), Material.Bitmap(AssetName("test"))))

  val bigList    = graphics1000.toList
  val bigJsArray = graphics1000.toJSArray
  val bigBatch   = Batch(bigJsArray)
  val bigNestedBatch = {
    val s1 = bigJsArray.splitAt(100)
    val s2 = s1._2.splitAt(300)
    val s3 = s2._2.splitAt(200)

    val a1 = s1._1
    val a2 = s2._1
    val a3 = s3._1
    val a4 = s3._2

    Batch(a1.head) |+| (Batch(a1.tail) |+| (Batch(a2) |+| Batch(a3) |+| Batch(a4)) |+| Batch.empty)
  }

  @nowarn("msg=discarded")
  val suite = GuiSuite(
    Suite("Batch Benchmarks")(
      Benchmark("concat - list") {
        bigList ++ bigList
      },
      Benchmark("concat - js.Array") {
        bigJsArray ++ bigJsArray
      },
      Benchmark("concat - batch") {
        bigBatch ++ bigBatch
      },
      Benchmark("concat - batch nested") {
        bigNestedBatch ++ bigNestedBatch
      },
      Benchmark("foreach - list") {
        bigList.foreach(_.moveBy(5, 5))
      },
      Benchmark("foreach - js.Array") {
        bigJsArray.foreach(_.moveBy(5, 5))
      },
      Benchmark("foreach - batch") {
        bigBatch.foreach(_.moveBy(5, 5))
      },
      Benchmark("foreach - batch nested") {
        bigNestedBatch.foreach(_.moveBy(5, 5))
      },
      Benchmark("map - list") {
        bigList.map(_.moveBy(5, 5))
      },
      Benchmark("map - js.Array") {
        bigJsArray.map(_.moveBy(5, 5))
      },
      Benchmark("map - batch") {
        bigBatch.map(_.moveBy(5, 5))
      },
      Benchmark("map - batch nested") {
        bigNestedBatch.map(_.moveBy(5, 5))
      },
      Benchmark("toJSArray - list") {
        bigList.toJSArray
      },
      Benchmark("toJSArray - batch") {
        bigBatch.toJSArray
      },
      Benchmark("toJSArray - batch nested") {
        bigNestedBatch.toJSArray
      }
    )
  )
