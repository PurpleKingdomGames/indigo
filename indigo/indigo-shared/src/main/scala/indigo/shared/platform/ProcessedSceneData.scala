package indigo.shared.platform

import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayLayer
import indigo.shared.shader.ShaderId
import indigo.shared.display.DisplayObjectUniformData

final class ProcessedSceneData(
    val layers: List[DisplayLayer],
    val cloneBlankDisplayObjects: Map[String, DisplayObject],
    val shaderId: ShaderId,
    val shaderUniformData: Option[DisplayObjectUniformData]
)
