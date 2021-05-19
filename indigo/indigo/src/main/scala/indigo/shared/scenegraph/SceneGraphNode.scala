package indigo.shared.scenegraph

import indigo.shared.datatypes._
import indigo.shared.materials.ShaderData
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.BoundaryLocator

/** The internal parent type of anything that can affect the visual representation of the game.
  *
  * There are some SceneGraphNode's used only for internal scene processing.
  */
sealed trait SceneGraphNode derives CanEqual
object SceneGraphNode {
  given CanEqual[Option[SceneGraphNode], Option[SceneGraphNode]] = CanEqual.derived
  given CanEqual[List[SceneGraphNode], List[SceneGraphNode]]     = CanEqual.derived
}

/** The parent type of all nodes a user might use or create. Defines the fields needed to draw something onto the
  * screen.
  */
sealed trait SceneNode extends SceneGraphNode:
  def position: Point
  def rotation: Radians
  def scale: Vector2
  def depth: Depth
  def flip: Flip
  def ref: Point
  def withDepth(newDepth: Depth): SceneNode
object SceneNode {
  given CanEqual[Option[SceneNode], Option[SceneNode]] = CanEqual.derived
  given CanEqual[List[SceneNode], List[SceneNode]]     = CanEqual.derived
}

/** RenderNodes are built-in node types that have their own size, and where Indigo understands how to build the shader
  * data.
  */
trait RenderNode extends SceneNode:
  def size: Size

object RenderNode:
  given CanEqual[Option[RenderNode], Option[RenderNode]] = CanEqual.derived
  given CanEqual[List[RenderNode], List[RenderNode]]     = CanEqual.derived

/** DependentNodes are built-in node types where Indigo understands how to build the shader data, and the bounds are
  * dependant on the contents of the node.
  */
trait DependentNode extends SceneNode

object DependentNode:
  given CanEqual[Option[DependentNode], Option[DependentNode]] = CanEqual.derived
  given CanEqual[List[DependentNode], List[DependentNode]]     = CanEqual.derived

/** EntityNodes can be extended to create custom scene elements.
  *
  * May be used in conjunction with `EventHandler` and `Cloneable`.
  */
trait EntityNode extends RenderNode:
  def toShaderData: ShaderData

object EntityNode:
  given CanEqual[Option[EntityNode], Option[EntityNode]] = CanEqual.derived
  given CanEqual[List[EntityNode], List[EntityNode]]     = CanEqual.derived

/** Used internally during scene processing
  */
final case class Transformer(node: SceneGraphNode, transform: CheapMatrix4) extends SceneGraphNode derives CanEqual:
  def addTransform(matrix: CheapMatrix4): Transformer =
    this.copy(transform = transform * matrix)
