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

  val suite = GuiSuite(
    Suite("Chain Benchmarks")(
      Benchmark("concat - list") {
        bigList ++ bigList
      },
      Benchmark("concat - chain") {
        bigChain ++ bigChain
      },
      Benchmark("map - list") {
        bigList.map(_.moveBy(5, 5))
      },
      Benchmark("map - chain") {
        bigChain.map(_.moveBy(5, 5))
      },
      Benchmark("toJSArray - list") {
        bigList.toJSArray
      },
      Benchmark("toJSArray - chain") {
        bigChain.toJSArray
      }
    )
  )
