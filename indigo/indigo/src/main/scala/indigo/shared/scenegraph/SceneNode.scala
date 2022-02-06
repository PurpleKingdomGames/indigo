package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.datatypes._
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.ShaderData

/** The parent type of all nodes a user might use or create. Defines the fields needed to draw something onto the
  * screen.
  */
sealed trait SceneNode:
  def position: Point
  def rotation: Radians
  def scale: Vector2
  def depth: Depth
  def flip: Flip
  def ref: Point
  def withDepth(newDepth: Depth): SceneNode
  def eventHandlerEnabled: Boolean
  def eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
  def calculatedBounds(locator: BoundaryLocator): Option[Rectangle]
object SceneNode:
  given CanEqual[Option[SceneNode], Option[SceneNode]] = CanEqual.derived
  given CanEqual[List[SceneNode], List[SceneNode]]     = CanEqual.derived

/** RenderNodes are built-in node types that have their own size, and where Indigo understands how to build the shader
  * data.
  */
trait RenderNode extends SceneNode:
  def size: Size
  override def withDepth(newDepth: Depth): RenderNode

object RenderNode:
  given CanEqual[Option[RenderNode], Option[RenderNode]] = CanEqual.derived
  given CanEqual[List[RenderNode], List[RenderNode]]     = CanEqual.derived

/** DependentNodes are built-in node types where Indigo understands how to build the shader data, and the bounds are
  * dependant on the contents of the node.
  */
trait DependentNode extends SceneNode:
  override def withDepth(newDepth: Depth): DependentNode
object DependentNode:
  given CanEqual[Option[DependentNode], Option[DependentNode]] = CanEqual.derived
  given CanEqual[List[DependentNode], List[DependentNode]]     = CanEqual.derived

/** EntityNodes can be extended to create custom scene elements.
  *
  * Can be made cloneable by extending `Cloneable`.
  */
trait EntityNode extends RenderNode:
  def toShaderData: ShaderData
  override def withDepth(newDepth: Depth): EntityNode

object EntityNode:
  given CanEqual[Option[EntityNode], Option[EntityNode]] = CanEqual.derived
  given CanEqual[List[EntityNode], List[EntityNode]]     = CanEqual.derived
