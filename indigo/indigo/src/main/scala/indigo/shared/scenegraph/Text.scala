package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.datatypes._
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.materials.ShaderData

/** Used to draw text onto the screen based on font sprite sheets (images / textures) and a character mapping instance
  * called `FontInfo`. `Text` instances are a bit of work to set up, but give super crisp pixel perfect results.
  */
final case class Text[M <: Material](
    text: String,
    alignment: TextAlignment,
    fontKey: FontKey,
    material: M,
    eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends DependentNode
    with EventHandler
    with SpatialModifiers[Text[M]]
    derives CanEqual {

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

  def withMaterial[MB <: Material](newMaterial: MB): Text[MB] =
    this.copy(material = newMaterial)

  def modifyMaterial[MB <: Material](alter: M => MB): Text[MB] =
    this.copy(material = alter(material))

  def moveTo(pt: Point): Text[M] =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Text[M] =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Text[M] =
    moveTo(newPosition)

  def moveBy(pt: Point): Text[M] =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Text[M] =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Text[M] =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Text[M] =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Text[M] =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Text[M] =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Text[M] =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Text[M] =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Text[M] =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Text[M] =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withDepth(newDepth: Depth): Text[M] =
    this.copy(depth = newDepth)

  def withRef(newRef: Point): Text[M] =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Text[M] =
    withRef(Point(x, y))

  def flipHorizontal(isFlipped: Boolean): Text[M] =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Text[M] =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Text[M] =
    this.copy(flip = newFlip)

  def withAlignment(newAlignment: TextAlignment): Text[M] =
    this.copy(alignment = newAlignment)

  def alignLeft: Text[M] =
    this.copy(alignment = TextAlignment.Left)
  def alignCenter: Text[M] =
    this.copy(alignment = TextAlignment.Center)
  def alignRight: Text[M] =
    this.copy(alignment = TextAlignment.Right)

  def withText(newText: String): Text[M] =
    this.copy(text = newText)

  def withFontKey(newFontKey: FontKey): Text[M] =
    this.copy(fontKey = newFontKey)

  def onEvent(e: ((Rectangle, GlobalEvent)) => List[GlobalEvent]): Text[M] =
    this.copy(eventHandler = e)

}

object Text {

  def apply[M <: Material](text: String, x: Int, y: Int, depth: Int, fontKey: FontKey, material: M): Text[M] =
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

  def apply[M <: Material](text: String, fontKey: FontKey, material: M): Text[M] =
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

}
