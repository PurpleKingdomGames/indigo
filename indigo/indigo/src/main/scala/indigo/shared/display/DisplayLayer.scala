package indigo.shared.display

import indigo.shared.datatypes.RGBA
import indigo.shared.scenegraph.Blend
import indigo.shared.scenegraph.Camera
import indigo.shared.scenegraph.LayerKey
import indigo.shared.shader.ShaderId

final case class DisplayLayer(
    layerKey: Option[LayerKey],
    entities: scalajs.js.Array[DisplayEntity],
    lightsData: scalajs.js.Array[Float],
    bgColor: RGBA,
    magnification: Option[Int],
    entityBlend: Blend,
    layerBlend: Blend,
    shaderId: ShaderId,
    shaderUniformData: scalajs.js.Array[DisplayObjectUniformData],
    camera: Option[Camera]
) derives CanEqual
