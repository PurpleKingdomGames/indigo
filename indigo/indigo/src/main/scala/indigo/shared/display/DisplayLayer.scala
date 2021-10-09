package indigo.shared.display

import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.RGBA
import indigo.shared.scenegraph.Blend
import indigo.shared.scenegraph.Camera
import indigo.shared.shader.ShaderId

final case class DisplayLayer(
    entities: Array[DisplayEntity],
    lightsData: Array[Float],
    bgColor: RGBA,
    magnification: Option[Int],
    depth: Depth,
    entityBlend: Blend,
    layerBlend: Blend,
    shaderId: ShaderId,
    shaderUniformData: Array[DisplayObjectUniformData],
    camera: Option[Camera]
) derives CanEqual
