package indigo.shared.display

import scala.collection.mutable.ListBuffer
import indigo.shared.scenegraph.Blend

final case class DisplayLayer(entities: ListBuffer[DisplayEntity], magnification: Option[Int], depth: Int, blend: Blend)
