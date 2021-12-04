package indigo.benchmarks

import org.scalajs.dom.document
import japgolly.scalajs.benchmark.gui.BenchmarkGUI

import scala.scalajs.js.annotation.JSExportTopLevel

object Main:

  @JSExportTopLevel("main")
  def main(): Unit =
    val body = document.getElementById("body")
    BenchmarkGUI.renderMenu(body)(
      Caching.suite,
      Collisions.suite
    )
