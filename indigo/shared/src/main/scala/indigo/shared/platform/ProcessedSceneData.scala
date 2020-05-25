package indigo.shared.platform

import indigo.shared.datatypes.Matrix4
import scala.collection.mutable
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayObject
import indigo.shared.scenegraph.Light
import indigo.shared.ClearColor
import indigo.shared.datatypes.RGBA

final class ProcessedSceneData(
    val gameProjection: Matrix4,
    val lightingProjection: Matrix4,
    val uiProjection: Matrix4,
    val gameLayerDisplayObjects: mutable.ListBuffer[DisplayEntity],
    val lightingLayerDisplayObjects: mutable.ListBuffer[DisplayEntity],
    val distortionLayerDisplayObjects: mutable.ListBuffer[DisplayEntity],
    val uiLayerDisplayObjects: mutable.ListBuffer[DisplayEntity],
    val cloneBlankDisplayObjects: Map[String, DisplayObject],
    val lights: List[Light],
    val clearColor: ClearColor,
    val gameLayerColorOverlay: RGBA,
    val uiLayerColorOverlay: RGBA,
    val gameLayerTint: RGBA,
    val lightingLayerTint: RGBA,
    val uiLayerTint: RGBA,
    val gameLayerSaturation: Double,
    val lightingLayerSaturation: Double,
    val uiLayerSaturation: Double
)
