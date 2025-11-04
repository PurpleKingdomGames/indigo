package indigo.shared.scenegraph

import indigo.shared.datatypes.*
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material

/** Used to draw text onto the screen based on font sprite sheets (images / textures) and a character mapping instance
  * called `FontInfo`. `Text` instances are a bit of work to set up, but give super crisp pixel perfect results.
  */
final case class Text[M <: Material](
    text: String,
    alignment: TextAlignment,
    fontKey: FontKey,
    lineHeight: Int,
    letterSpacing: Int,
    material: M,
    eventHandlerEnabled: Boolean,
    eventHandler: ((Text[?], GlobalEvent)) => Option[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    ref: Point,
    flip: Flip
) extends DependentNode[Text[M]]
    with SpatialModifiers[Text[M]] derives CanEqual:

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

  /** Sets the vertical gap between lines of text _in addition_ (relative to) to the actual height of the text. Defaults
    * to 0.
    */
  def withLineHeight(amount: Int): Text[M] =
    this.copy(lineHeight = amount)

  /** Sets the horiztonal gap between letters in a line of text. Defaults to 0. */
  def withLetterSpacing(amount: Int): Text[M] =
    this.copy(letterSpacing = amount)

  def withEventHandler(f: ((Text[?], GlobalEvent)) => Option[GlobalEvent]): Text[M] =
    this.copy(eventHandler = f, eventHandlerEnabled = true)
  def onEvent(f: PartialFunction[((Text[?], GlobalEvent)), GlobalEvent]): Text[M] =
    withEventHandler(f.lift)
  def enableEvents: Text[M] =
    this.copy(eventHandlerEnabled = true)
  def disableEvents: Text[M] =
    this.copy(eventHandlerEnabled = false)

object Text:

  def apply[M <: Material](text: String, x: Int, y: Int, fontKey: FontKey, material: M): Text[M] =
    Text(
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default,
      text = text,
      alignment = TextAlignment.Left,
      fontKey = fontKey,
      lineHeight = 0,
      letterSpacing = 0,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      material = material
    )

  def apply[M <: Material](text: String, fontKey: FontKey, material: M): Text[M] =
    Text(
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default,
      text = text,
      alignment = TextAlignment.Left,
      fontKey = fontKey,
      lineHeight = 0,
      letterSpacing = 0,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      material = material
    )
