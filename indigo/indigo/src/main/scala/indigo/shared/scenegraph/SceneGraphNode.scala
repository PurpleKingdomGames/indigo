package indigo.shared.scenegraph

import indigo.shared.datatypes._
import indigo.shared.materials.ShaderData
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.BoundaryLocator

/** The parent type of anything that can affect the visual representation of the game, including internal elements.
  */
sealed trait SceneGraphNode derives CanEqual
object SceneGraphNode {
  given CanEqual[Option[SceneGraphNode], Option[SceneGraphNode]] = CanEqual.derived
  given CanEqual[List[SceneGraphNode], List[SceneGraphNode]]     = CanEqual.derived
}

sealed trait SceneNodeInternal extends SceneGraphNode:
  def position: Point
  def rotation: Radians
  def scale: Vector2
  def depth: Depth
  def flip: Flip
  def ref: Point
  def withDepth(newDepth: Depth): SceneNodeInternal
object SceneNodeInternal {
  given CanEqual[Option[SceneNodeInternal], Option[SceneNodeInternal]] = CanEqual.derived
  given CanEqual[List[SceneNodeInternal], List[SceneNodeInternal]]     = CanEqual.derived
}

/** RenderNodes are built-in node types where Indigo understands how to build the shader data.
  */
trait RenderNode extends SceneNodeInternal:
  def size: Size
  override def withDepth(newDepth: Depth): RenderNode

object RenderNode:
  given CanEqual[Option[RenderNode], Option[RenderNode]] = CanEqual.derived
  given CanEqual[List[RenderNode], List[RenderNode]]     = CanEqual.derived

/** EntityNodes can be extended to create custom scene elements.
  *
  * May be used in conjunction with `EventHandler` and `Cloneable`.
  */
trait EntityNode extends RenderNode:
  def toShaderData: ShaderData
  override def withDepth(newDepth: Depth): EntityNode

object EntityNode:
  given CanEqual[Option[EntityNode], Option[EntityNode]] = CanEqual.derived
  given CanEqual[List[EntityNode], List[EntityNode]]     = CanEqual.derived

/** DependentNodes are built-in node types where Indigo understands how to build the shader data, and the bounds are
  * dependant on the contents of the node.
  */
trait DependentNode extends SceneNodeInternal:
  override def withDepth(newDepth: Depth): DependentNode
object DependentNode:
  given CanEqual[Option[DependentNode], Option[DependentNode]] = CanEqual.derived
  given CanEqual[List[DependentNode], List[DependentNode]]     = CanEqual.derived

final case class Transformer(node: SceneGraphNode, transform: CheapMatrix4) extends SceneGraphNode derives CanEqual:
  def addTransform(matrix: CheapMatrix4): Transformer =
    this.copy(transform = transform * matrix)
