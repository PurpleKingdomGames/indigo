package indigo.shared.scenegraph

import indigo.shared.datatypes._
import indigo.shared.materials.ShaderData
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.BoundaryLocator

/** The parent type of anything that can affect the visual representation of the game.
  */
sealed trait SceneNode extends Product with Serializable derives CanEqual
object SceneNode {
  given CanEqual[Option[SceneNode], Option[SceneNode]] = CanEqual.derived
  given CanEqual[List[SceneNode], List[SceneNode]]     = CanEqual.derived
}

sealed trait RenderNode extends SceneNode {
  def position: Point
  def rotation: Radians
  def scale: Vector2
  def depth: Depth
  def flip: Flip
  def ref: Point

  def withDepth(newDepth: Depth): RenderNode
}
object RenderNode:
  given CanEqual[Option[RenderNode], Option[RenderNode]] = CanEqual.derived
  given CanEqual[List[RenderNode], List[RenderNode]]     = CanEqual.derived

/** Can be extended to create custom scene elements.
  *
  * May be used in conjunction with `EventHandler` and `Cloneable`.
  */
trait EntityNode extends RenderNode {
  def bounds: Rectangle
  def toShaderData: ShaderData
}

trait DependentNode extends SceneNode {
  def position: Point
  def rotation: Radians
  def scale: Vector2
  def depth: Depth
  def flip: Flip

  def withDepth(newDepth: Depth): DependentNode
}

trait CompositeNode extends RenderNode {
  def calculatedBounds(locator: BoundaryLocator): Option[Rectangle]
}

final case class Transformer(node: SceneNode, transform: CheapMatrix4) extends SceneNode derives CanEqual {
  def addTransform(matrix: CheapMatrix4): Transformer =
    this.copy(transform = transform * matrix)
}

