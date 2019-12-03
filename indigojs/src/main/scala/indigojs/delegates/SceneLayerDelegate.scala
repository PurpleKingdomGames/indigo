package indigojs.delegates

// import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.scenegraph.SceneLayer

final class SceneLayerDelegate(val nodes: js.Array[SceneGraphNodeDelegate], val tint: TintDelegate, val saturation: Double, val magnification: Option[Int]) {

  def withTint(newTint: TintDelegate): SceneLayerDelegate =
    new SceneLayerDelegate(nodes, newTint, saturation, magnification)

  def withSaturationLevel(amount: Double): SceneLayerDelegate =
    new SceneLayerDelegate(nodes, tint, amount, magnification)

  def withMagnification(level: Int): SceneLayerDelegate =
    new SceneLayerDelegate(nodes, tint, saturation, SceneLayer.sanitiseMagnification(level))

  def toInternal: SceneLayer =
    SceneLayer(nodes.map(_.toInternal).toList, tint.toInternal, saturation, magnification)
}

object SceneLayerDelegate {

  def None: SceneLayerDelegate =
    new SceneLayerDelegate(new js.Array(), TintDelegate.None, 1.0d, Option.empty[Int])

}
