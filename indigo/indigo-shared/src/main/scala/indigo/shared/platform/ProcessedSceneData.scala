package indigo.shared.platform

import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayLayer
import indigo.shared.scenegraph.Light
import indigo.shared.datatypes.RGBA
import indigo.shared.shader.ShaderId
import indigo.shared.display.DisplayObjectUniformData

final class ProcessedSceneData(
    val layers: List[DisplayLayer],
    val cloneBlankDisplayObjects: Map[String, DisplayObject],
    val lights: List[Light],
    val clearColor: RGBA,
    val shaderId: ShaderId,
    val shaderUniformData: Option[DisplayObjectUniformData]
)
