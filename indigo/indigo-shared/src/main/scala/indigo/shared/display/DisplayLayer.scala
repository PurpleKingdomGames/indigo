package indigo.shared.display

import scala.collection.mutable.ListBuffer
import indigo.shared.scenegraph.Blend
import indigo.shared.shader.ShaderId
import indigo.shared.datatypes.RGBA

final case class DisplayLayer(
    entities: ListBuffer[DisplayEntity],
    lightsData: Array[Float],
    bgColor: RGBA,
    magnification: Option[Int],
    depth: Int,
    entityBlend: Blend,
    layerBlend: Blend,
    shaderId: ShaderId,
    shaderUniformData: Option[DisplayObjectUniformData]
)
