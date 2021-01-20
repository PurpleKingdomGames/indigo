package indigo.facades.worker

import scala.collection.mutable
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayObject

import scala.scalajs.js
import scalajs.js.JSConverters._

final class ProcessedSceneData(
    val gameProjection: js.Array[Double],
    val lightingProjection: js.Array[Double],
    val uiProjection: js.Array[Double],
    val gameLayerDisplayObjects: js.Array[DisplayEntity],
    val lightingLayerDisplayObjects: js.Array[DisplayEntity],
    val distortionLayerDisplayObjects: js.Array[DisplayEntity],
    val uiLayerDisplayObjects: js.Array[DisplayEntity],
    val cloneBlankDisplayObjects: js.Map[String, DisplayObject],
    val lights: js.Array[js.Any],
    val clearColor: js.Any,
    val gameLayerColorOverlay: js.Any,
    val uiLayerColorOverlay: js.Any,
    val gameLayerTint: js.Any,
    val lightingLayerTint: js.Any,
    val uiLayerTint: js.Any,
    val gameLayerSaturation: Double,
    val lightingLayerSaturation: Double,
    val uiLayerSaturation: Double
) extends js.Object
