package indigo.shared.platform

import indigo.shared.datatypes.mutable.CheapMatrix4
import scala.collection.mutable
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayObject
import indigo.shared.scenegraph.Light
import indigo.shared.datatypes.RGBA

final class ProcessedSceneData(
    val gameProjection: CheapMatrix4,
    val lightingProjection: CheapMatrix4,
    val uiProjection: CheapMatrix4,
    val gameLayerDisplayObjects: mutable.ListBuffer[DisplayEntity],
    val lightingLayerDisplayObjects: mutable.ListBuffer[DisplayEntity],
    val distortionLayerDisplayObjects: mutable.ListBuffer[DisplayEntity],
    val uiLayerDisplayObjects: mutable.ListBuffer[DisplayEntity],
    val cloneBlankDisplayObjects: Map[String, DisplayObject],
    val lights: List[Light],
    val clearColor: RGBA,
    val gameLayerColorOverlay: RGBA,
    val uiLayerColorOverlay: RGBA,
    val gameLayerTint: RGBA,
    val lightingLayerTint: RGBA,
    val uiLayerTint: RGBA,
    val gameLayerSaturation: Double,
    val lightingLayerSaturation: Double,
    val uiLayerSaturation: Double
)
