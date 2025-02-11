package indigo.benchmarks

import indigo.*
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*

object BoundaryLocatorBenchmarks:

  val fontRegister: FontRegister =
    new FontRegister

  fontRegister.register(TextSamples.fontInfo)

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, fontRegister)

  val suite = GuiSuite(
    Suite("BoundaryLocator Benchmarks")(
      Benchmark("(text) textLineBounds (purge)") {
        boundaryLocator.purgeCache()
        boundaryLocator.textLineBounds(TextSamples.textValue, TextSamples.fontInfo, 0, 0)
      },
      Benchmark("(text) textLineBounds (no purge)") {
        boundaryLocator.textLineBounds(TextSamples.textValue, TextSamples.fontInfo, 0, 0)
      },
      Benchmark("(text) textAsLinesWithBounds (purge)") {
        boundaryLocator.purgeCache()
        boundaryLocator.textAsLinesWithBounds(TextSamples.textValue, TextSamples.fontKey, 0, 0)
      },
      Benchmark("(text) textAsLinesWithBounds (no purge)") {
        boundaryLocator.textAsLinesWithBounds(TextSamples.textValue, TextSamples.fontKey, 0, 0)
      },
      Benchmark("(text) textAllLineBounds (purge)") {
        boundaryLocator.purgeCache()
        boundaryLocator.textAllLineBounds(TextSamples.textValue, TextSamples.fontKey, 0, 0)
      },
      Benchmark("(text) textAllLineBounds (no purge)") {
        boundaryLocator.textAllLineBounds(TextSamples.textValue, TextSamples.fontKey, 0, 0)
      },
      Benchmark("(text) textBounds (purge)") {
        boundaryLocator.purgeCache()
        boundaryLocator.textBounds(TextSamples.text)
      },
      Benchmark("(text) textBounds (no purge)") {
        boundaryLocator.textBounds(TextSamples.text)
      }
    )
  )

object TextSamples {
  val textValue = "abcdefghijklmnopqrstuvwxyz"

  val material = Material.Bitmap(AssetName("font-sheet"))

  val chars = Batch(
    FontChar("a", 0, 16, 16, 16),
    FontChar("b", 16, 16, 10, 20),
    FontChar("c", 32, 16, 16, 16),
    FontChar("d", 32, 16, 16, 16), // Identical from here down
    FontChar("e", 32, 16, 16, 16),
    FontChar("f", 32, 16, 16, 16),
    FontChar("g", 32, 16, 16, 16),
    FontChar("h", 32, 16, 16, 16),
    FontChar("i", 32, 16, 16, 16),
    FontChar("j", 32, 16, 16, 16),
    FontChar("k", 32, 16, 16, 16),
    FontChar("l", 32, 16, 16, 16),
    FontChar("m", 32, 16, 16, 16),
    FontChar("n", 32, 16, 16, 16),
    FontChar("o", 32, 16, 16, 16),
    FontChar("p", 32, 16, 16, 16),
    FontChar("q", 32, 16, 16, 16),
    FontChar("r", 32, 16, 16, 16),
    FontChar("s", 32, 16, 16, 16),
    FontChar("t", 32, 16, 16, 16),
    FontChar("u", 32, 16, 16, 16),
    FontChar("v", 32, 16, 16, 16),
    FontChar("w", 32, 16, 16, 16),
    FontChar("x", 32, 16, 16, 16),
    FontChar("y", 32, 16, 16, 16),
    FontChar("z", 32, 16, 16, 16)
  )

  val fontKey = FontKey("boundary locator tests")

  val fontInfo = FontInfo(fontKey, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

  val text: Text[?] =
    Text("abcdefghijklmnopqrstuvwxyz", 50, 50, fontKey, material).alignLeft
}
