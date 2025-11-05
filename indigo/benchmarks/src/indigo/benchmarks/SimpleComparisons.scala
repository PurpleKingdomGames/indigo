package indigo.benchmarks

import indigo.*
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*

object SimpleComparisons:

  val textBlob: String =
    """Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus porta felis vel felis bibendum, vitae sollicitudin elit aliquam. Integer in eros non ipsum vulputate feugiat vel in ex. Nam pellentesque interdum neque, vitae malesuada metus fringilla et. Maecenas laoreet facilisis ornare. Nulla facilisi. In gravida maximus erat, sed finibus erat interdum in. Aenean viverra massa a lacus dictum aliquet id ac libero."""

  val textBlobHashCode: Int =
    textBlob.hashCode

  val suite = GuiSuite(
    Suite("Simple Comparisons")(
      Benchmark("String to String") {
        textBlob == textBlob
      },
      Benchmark("hashCode to Strings hashCode") {
        textBlobHashCode == textBlob.hashCode
      }
    )
  )
