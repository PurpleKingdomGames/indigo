package indigo.benchmarks

import indigo.*
import indigo.facades.WeakMap
import indigo.shared.QuickCache
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*

import scala.scalajs.js

object Caching:

  val default = Graphic(32, 32, Material.Bitmap(AssetName("test_nada")))

  given QuickCache[Graphic[Material.Bitmap]] = QuickCache.empty

  val entries = (0 to 100).toList.map { i =>
    ("entry" + i, Graphic(32, 32, Material.Bitmap(AssetName("test" + i))))
  }
  entries.foreach { case (k, v) =>
    QuickCache(k)(v)
  }

  val map: Map[String, Graphic[Material.Bitmap]] =
    entries.toMap

  final case class WeakMapKey(k: String)

  val weakMap: WeakMap[WeakMapKey, Graphic[Material.Bitmap]] = new WeakMap()
  entries.foreach { case (k, v) =>
    weakMap.set(WeakMapKey(k), v)
  }

  val weakMap2: WeakMap[js.Object, Graphic[Material.Bitmap]] = new WeakMap()
  entries.foreach { case (k, v) =>
    weakMap2.set(new js.Object { val key: String = k }, v)
  }

  val suite = GuiSuite(
    Suite("Caching strategies")(
      Benchmark("Lookup: QuickCache fetchOrAdd (current)") {
        QuickCache("entry97") {
          default
        }
      },
      Benchmark("Lookup: Map") {
        map.get("entry97").getOrElse(default)
      },
      // The weakmap versions are faster, but WeakMap lacks functionality we need.
      Benchmark("Lookup: WeakMap") {
        weakMap.get(WeakMapKey("entry97")).getOrElse(default)
      },
      Benchmark("Lookup: WeakMap js.Object") {
        weakMap2.get(new js.Object { val key: String = "entry97" }).getOrElse(default)
      }
    )
  )
