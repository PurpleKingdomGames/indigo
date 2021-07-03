package indigo.shared.platform

import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayLayer
import indigo.shared.shader.ShaderId
import indigo.shared.display.DisplayObjectUniformData
import indigo.shared.scenegraph.CloneId
import indigo.shared.scenegraph.Camera

final class ProcessedSceneData(
    val layers: List[DisplayLayer],
    val cloneBlankDisplayObjects: Map[CloneId, DisplayObject],
    val shaderId: ShaderId,
    val shaderUniformData: List[DisplayObjectUniformData],
    val camera: Option[Camera]
)
