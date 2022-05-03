package indigo.benchmarks

import indigo.*

import japgolly.scalajs.benchmark._
import japgolly.scalajs.benchmark.gui._

import scalajs.js
import scalajs.js.JSConverters.*

object ChainBenchmarks:

  val graphics1000 =
    (0 to 1000).map(_ => Graphic(Size(32), Material.Bitmap(AssetName("test"))))

  val bigList    = graphics1000.toList
  val bigJsArray = graphics1000.toJSArray
  val bigChain   = Chain.Wrapped(bigJsArray)
  val bigNestedChain = {
    val s1 = bigJsArray.splitAt(100)
    val s2 = s1._2.splitAt(300)
    val s3 = s2._2.splitAt(200)

    val a1 = s1._1
    val a2 = s2._1
    val a3 = s3._1
    val a4 = s3._2

    Chain.Combine(
      Chain.Singleton(a1.head),
      Chain.Combine(
        Chain.Wrapped(a1.tail),
        Chain.Combine(
          Chain.Combine(
            Chain(a2),
            Chain(a3) |+| Chain(a4)
          ),
          Chain.Empty
        )
      )
    )
  }

  println(bigList.size)
  println(bigJsArray.size)
  println(bigChain.size)
  println(bigNestedChain.size)

  val suite = GuiSuite(
    Suite("Chain Benchmarks")(
      Benchmark("concat - list") {
        bigList ++ bigList
      },
      Benchmark("concat - chain") {
        bigChain ++ bigChain
      },
      Benchmark("concat - chain nested") {
        bigNestedChain ++ bigNestedChain
      },
      Benchmark("map - list") {
        bigList.map(_.moveBy(5, 5))
      },
      Benchmark("map - chain") {
        bigChain.map(_.moveBy(5, 5))
      },
      Benchmark("map - chain nested") {
        bigNestedChain.map(_.moveBy(5, 5))
      },
      Benchmark("toJSArray - list") {
        bigList.toJSArray
      },
      Benchmark("toJSArray - chain") {
        bigChain.toJSArray
      },
      Benchmark("toJSArray - chain nested") {
        bigNestedChain.toJSArray
      }
    )
  )
