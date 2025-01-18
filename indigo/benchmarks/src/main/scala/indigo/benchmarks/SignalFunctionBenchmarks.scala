package indigo.benchmarks

import indigo.*
import indigo.syntax.*
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*

object SignalFunctionBenchmarks:

  import indigo.shared.temporal.SignalFunction as SF

  val graphic: Graphic[Material.Bitmap] =
    Graphic(Size(32), Material.Bitmap(AssetName("test")))

  val suite = GuiSuite(
    Suite("SignalFunction Comparisons")(
      Benchmark("Control test") {
        (Signal.Time |> SF(identity)).at(1.second)
      },
      Benchmark("lerp") {
        (Signal.Time |> SF.lerp(1.second)).at(1.second)
      },
      Benchmark("lerp to point") {
        (Signal.Time |> SF.lerp(Point(0), Point(10), 1.second)).at(1.second)
      },
      Benchmark("lerp to point and move graphic") {
        (Signal.Time |> (SF.lerp(Point(0), Point(10), 1.second) >>> SF(pt => graphic.moveTo(pt)))).at(1.second)
      },
      Benchmark("Chaining 10 identity SFs") {
        val sf: SignalFunction[Seconds, Seconds] =
          SF[Seconds, Seconds](identity) >>>
            SF(identity) >>>
            SF(identity) >>>
            SF(identity) >>>
            SF(identity) >>>
            SF(identity) >>>
            SF(identity) >>>
            SF(identity) >>>
            SF(identity) >>>
            SF(identity)

        (Signal.Time |> sf).at(1.second)
      }
    )
  )
