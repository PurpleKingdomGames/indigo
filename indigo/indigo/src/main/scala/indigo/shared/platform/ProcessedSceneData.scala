package indigo.shared.platform

import indigo.shared.display.DisplayLayer
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayObjectUniformData
import indigo.shared.scenegraph.Camera
import indigo.shared.scenegraph.CloneId
import indigo.shared.shader.ShaderId

final class ProcessedSceneData(
    val layers: Array[DisplayLayer],
    val cloneBlankDisplayObjects: Map[CloneId, DisplayObject],
    val shaderId: ShaderId,
    val shaderUniformData: Array[DisplayObjectUniformData],
    val camera: Option[Camera]
)
