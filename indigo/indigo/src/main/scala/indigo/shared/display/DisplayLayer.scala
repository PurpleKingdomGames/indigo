package indigo.shared.display

import indigo.BindingKey
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.RGBA
import indigo.shared.scenegraph.Blend
import indigo.shared.scenegraph.Camera
import indigo.shared.shader.ShaderId

final case class DisplayLayer(
    bindingKey: Option[BindingKey],
    entities: scalajs.js.Array[DisplayEntity],
    lightsData: scalajs.js.Array[Float],
    bgColor: RGBA,
    magnification: Option[Int],
    depth: Depth,
    entityBlend: Blend,
    layerBlend: Blend,
    shaderId: ShaderId,
    shaderUniformData: scalajs.js.Array[DisplayObjectUniformData],
    camera: Option[Camera]
) derives CanEqual
