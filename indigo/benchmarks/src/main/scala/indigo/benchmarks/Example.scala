package indigo.benchmarks

import japgolly.scalajs.benchmark._
import japgolly.scalajs.benchmark.gui._

object Example:

  val suite = GuiSuite(
    Suite("Example Benchmarks")(
      // Benchmark #1
      Benchmark("foreach") {
        var s = Set.empty[Int]
        (1 to 100) foreach (s += _)
        s
      },

      // Benchmark #2
      Benchmark("fold") {
        (1 to 100).foldLeft(Set.empty[Int])(_ + _)
      }
    )
  )
