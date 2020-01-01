package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.scenegraph.SceneLayer

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneLayer")
final class SceneLayerDelegate(_nodes: js.Array[SceneGraphNodeDelegate], _tint: TintDelegate, _saturation: Double, _magnification: Option[Int]) {

  @JSExport
  val nodes = _nodes
  @JSExport
  val tint = _tint
  @JSExport
  val saturation = _saturation
  @JSExport
  val magnification = _magnification

  @JSExport
  def addLayerNodes(newNodes: js.Array[SceneGraphNodeDelegate]): SceneLayerDelegate =
    new SceneLayerDelegate(nodes ++ newNodes, tint, saturation, magnification)

  @JSExport
  def withTint(newTint: TintDelegate): SceneLayerDelegate =
    new SceneLayerDelegate(nodes, newTint, saturation, magnification)

  @JSExport
  def withSaturationLevel(amount: Double): SceneLayerDelegate =
    new SceneLayerDelegate(nodes, tint, amount, magnification)

  @JSExport
  def withMagnification(level: Int): SceneLayerDelegate =
    new SceneLayerDelegate(nodes, tint, saturation, SceneLayer.sanitiseMagnification(level))

  def toInternal: SceneLayer =
    SceneLayer(nodes.map(_.toInternal).toList, tint.toInternal, saturation, magnification)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneLayerHelper")
@JSExportAll
object SceneLayerDelegate {

  def None: SceneLayerDelegate =
    new SceneLayerDelegate(new js.Array(), TintDelegate.None, 1.0d, Option.empty[Int])

}
