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
object SceneNode:
  given CanEqual[Option[SceneNode], Option[SceneNode]] = CanEqual.derived
  given CanEqual[List[SceneNode], List[SceneNode]]     = CanEqual.derived

/** RenderNodes are built-in node types that have their own size, and where Indigo understands how to build the shader
  * data.
  */
trait RenderNode[T <: SceneNode] extends SceneNode:
  type Out = T
  def size: Size
  override def withDepth(newDepth: Depth): T
  def eventHandlerEnabled: Boolean
  def eventHandler: ((T, GlobalEvent)) => Option[GlobalEvent]
object RenderNode:
  given [T <: SceneNode]: CanEqual[Option[RenderNode[T]], Option[RenderNode[T]]] = CanEqual.derived
  given [T <: SceneNode]: CanEqual[List[RenderNode[T]], List[RenderNode[T]]]     = CanEqual.derived

/** DependentNodes are built-in node types where Indigo understands how to build the shader data, and the bounds are
  * dependant on the contents of the node.
  */
trait DependentNode[T <: SceneNode] extends SceneNode:
  type Out = T
  override def withDepth(newDepth: Depth): T
  def eventHandlerEnabled: Boolean
  def eventHandler: ((T, GlobalEvent)) => Option[GlobalEvent]
object DependentNode:
  given [T <: SceneNode]: CanEqual[Option[DependentNode[T]], Option[DependentNode[T]]] = CanEqual.derived
  given [T <: SceneNode]: CanEqual[List[DependentNode[T]], List[DependentNode[T]]]     = CanEqual.derived

/** EntityNodes can be extended to create custom scene elements.
  *
  * Can be made cloneable by extending `Cloneable`.
  */
trait EntityNode[T <: SceneNode] extends RenderNode[T]:
  def toShaderData: ShaderData
  override def withDepth(newDepth: Depth): T

  def bounds: Rectangle =
    BoundaryLocator.findBounds(this, position, size, ref)

object EntityNode:
  given [T <: SceneNode]: CanEqual[Option[EntityNode[T]], Option[EntityNode[T]]] = CanEqual.derived
  given [T <: SceneNode]: CanEqual[List[EntityNode[T]], List[EntityNode[T]]]     = CanEqual.derived
