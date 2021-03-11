package indigo.shared.display

import scala.collection.mutable.ListBuffer
import indigo.shared.scenegraph.Blend
import indigo.shared.shader.ShaderId

final case class DisplayLayer(
    entities: ListBuffer[DisplayEntity],
    magnification: Option[Int],
    depth: Int,
    entityBlend: Blend,
    layerBlend: Blend,
    shaderId: ShaderId,
    shaderUniformData: Option[DisplayObjectUniformData]
)
