package indigo.benchmarks

import org.scalajs.dom.document
import japgolly.scalajs.benchmark.gui.BenchmarkGUI

import scala.scalajs.js.annotation.JSExportTopLevel

object Main:

  @JSExportTopLevel("main")
  def main(args: Array[String]) =
    val body = document.getElementById("body")
    BenchmarkGUI.renderSuite(body)(Example.suite)
