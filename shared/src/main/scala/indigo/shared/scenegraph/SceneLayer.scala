package indigo.shared.scenegraph

import indigo.shared.datatypes.Tint

final class SceneLayer(val nodes: List[SceneGraphNode], val tint: Tint, val saturation: Double, val magnification: Option[Int]) {

  def |+|(other: SceneLayer): SceneLayer = {
    val newSaturation: Double =
      (saturation, other.saturation) match {
        case (1d, b) => b
        case (a, 1d) => a
        case (a, b)  => Math.min(a, b)
      }

    SceneLayer(nodes ++ other.nodes, tint + other.tint, newSaturation, magnification.orElse(other.magnification))
  }

  def ++(moreNodes: List[SceneGraphNode]): SceneLayer =
    SceneLayer(nodes ++ moreNodes, tint, saturation, magnification)

  def withTint(newTint: Tint): SceneLayer =
    SceneLayer(nodes, newTint, saturation, magnification)

  def withSaturationLevel(amount: Double): SceneLayer =
    SceneLayer(nodes, tint, amount, magnification)

  def withMagnification(level: Int): SceneLayer =
    SceneLayer(nodes, tint, saturation, SceneLayer.sanitiseMagnification(level))
}

object SceneLayer {

  def apply(nodes: List[SceneGraphNode]): SceneLayer =
    new SceneLayer(nodes, Tint.None, 1.0d, Option.empty[Int])

  def apply(nodes: List[SceneGraphNode], tint: Tint, saturation: Double, magnification: Option[Int]): SceneLayer =
    new SceneLayer(nodes, tint, saturation, magnification.flatMap(sanitiseMagnification))

  def None: SceneLayer =
    SceneLayer(Nil, Tint.None, 1.0d, Option.empty[Int])

  def sanitiseMagnification(level: Int): Option[Int] =
    Option(Math.max(1, Math.min(256, level)))

}
