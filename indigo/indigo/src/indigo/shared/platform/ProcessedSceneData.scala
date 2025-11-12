package indigo.shared.platform

import indigo.shared.display.DisplayLayer
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayObjectUniformData
import indigo.shared.scenegraph.Camera
import indigo.shared.shader.ShaderId

final class ProcessedSceneData(
    val layers: scalajs.js.Array[DisplayLayer],
    val cloneBlankDisplayObjects: scalajs.js.Dictionary[DisplayObject],
    val shaderId: ShaderId,
    val shaderUniformData: scalajs.js.Array[DisplayObjectUniformData],
    val camera: Option[Camera]
)
