package indigo.shared.scenegraph

import indigo.shared.scenegraph.syntax.BasicSpatial
import indigo.shared.scenegraph.syntax.Spatial

import indigo.shared.datatypes._
import indigo.shared.materials.Material
import indigo.shared.materials.ShaderData
import indigo.shared.events.GlobalEvent
import indigo.shared.BoundaryLocator

/** Used to draw text onto the screen.
  */
final case class Text(
    text: String,
    alignment: TextAlignment,
    fontKey: FontKey,
    material: Material,
    eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends DependentNode
    with EventHandler derives CanEqual {

  def calculatedBounds(locator: BoundaryLocator): Option[Rectangle] =
    Option(locator.textBounds(this)).map { rect =>
      val offset: Int =
        alignment match {
          case TextAlignment.Left   => 0
          case TextAlignment.Center => rect.size.width / 2
          case TextAlignment.Right  => rect.size.width
        }

      BoundaryLocator.findBounds(this, rect.position, rect.size, ref + Point(offset, 0))
    }

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withMaterial(newMaterial: Material): Text =
    this.copy(material = newMaterial)

  def modifyMaterial(alter: Material => Material): Text =
    this.copy(material = alter(material))

  def withDepth(newDepth: Depth): Text =
    this.copy(depth = newDepth)

  def withAlignment(newAlignment: TextAlignment): Text =
    this.copy(alignment = newAlignment)

  def alignLeft: Text =
    this.copy(alignment = TextAlignment.Left)
  def alignCenter: Text =
    this.copy(alignment = TextAlignment.Center)
  def alignRight: Text =
    this.copy(alignment = TextAlignment.Right)

  def withText(newText: String): Text =
    this.copy(text = newText)

  def withFontKey(newFontKey: FontKey): Text =
    this.copy(fontKey = newFontKey)

  def onEvent(e: ((Rectangle, GlobalEvent)) => List[GlobalEvent]): Text =
    this.copy(eventHandler = e)

}

object Text {

  def apply(text: String, x: Int, y: Int, depth: Int, fontKey: FontKey, material: Material): Text =
    Text(
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(depth),
      ref = Point.zero,
      flip = Flip.default,
      text = text,
      alignment = TextAlignment.Left,
      fontKey = fontKey,
      eventHandler = (_: (Rectangle, GlobalEvent)) => Nil,
      material = material
    )

  def apply(text: String, fontKey: FontKey, material: Material): Text =
    Text(
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(1),
      ref = Point.zero,
      flip = Flip.default,
      text = text,
      alignment = TextAlignment.Left,
      fontKey = fontKey,
      eventHandler = (_: (Rectangle, GlobalEvent)) => Nil,
      material = material
    )

  given BasicSpatial[Text] with
    extension (text: Text)
      def position: Point =
        text.position
      def rotation: Radians =
        text.rotation
      def scale: Vector2 =
        text.scale
      def depth: Depth =
        text.depth
      def ref: Point =
        text.ref
      def flip: Flip =
        text.flip

      def withPosition(newPosition: Point): Text =
        text.copy(position = newPosition)
      def withRotation(newRotation: Radians): Text =
        text.copy(rotation = newRotation)
      def withScale(newScale: Vector2): Text =
        text.copy(scale = newScale)
      def withRef(newRef: Point): Text =
        text.copy(ref = newRef)
      def withRef(x: Int, y: Int): Text =
        withRef(Point(x, y))
      def withDepth(newDepth: Depth): Text =
        text.copy(depth = newDepth)
      def withFlip(newFlip: Flip): Text =
        text.copy(flip = newFlip)

  given Spatial[Text] = Spatial.default[Text]
}
