package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.FontFamily
import indigo.shared.datatypes.FontStyle
import indigo.shared.datatypes.FontWeight
import indigo.shared.datatypes.Pixels
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.TextAlign
import indigo.shared.datatypes.TextStroke
import indigo.shared.datatypes.TextStyle
import indigo.shared.datatypes.Vector2
import indigo.shared.events.GlobalEvent

/** Used to draw text on the screen quickly based on a font. Much quicker and eaiser to use that `Text`, however suffers
  * from all the problems of browser rendered fonts, most notably, you cannot have pixel perfect fonts (fine for
  * mock-ups of HD UI layers perhaps).
  */
final case class TextBox(
    text: String,
    style: TextStyle,
    size: Size,
    eventHandlerEnabled: Boolean,
    eventHandler: ((TextBox, GlobalEvent)) => Option[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    ref: Point,
    flip: Flip
) extends RenderNode[TextBox]
    with SpatialModifiers[TextBox] derives CanEqual:

  def bounds: Rectangle =
    BoundaryLocator.findBounds(this, position, size, ref)

  def withText(newText: String): TextBox =
    this.copy(text = newText)

  def withTextStyle(newStyle: TextStyle): TextBox =
    this.copy(style = newStyle)
  def modifyStyle(modifier: TextStyle => TextStyle): TextBox =
    this.copy(style = modifier(style))

  def withSize(newSize: Size): TextBox =
    this.copy(size = newSize)

  // convenience methods

  def withColor(newColor: RGBA): TextBox =
    modifyStyle(_.withColor(newColor))

  def withStroke(newStroke: TextStroke): TextBox =
    modifyStyle(_.withStroke(newStroke))

  def withFontFamily(newFamily: FontFamily): TextBox =
    modifyStyle(_.modifyFont(_.withFontFamily(newFamily)))

  def withFontSize(newSize: Pixels): TextBox =
    modifyStyle(_.modifyFont(_.withSize(newSize)))

  def bold: TextBox =
    modifyStyle(_.modifyFont(_.withWeight(FontWeight.Bold)))
  def noBold: TextBox =
    modifyStyle(_.modifyFont(_.withWeight(FontWeight.Normal)))

  def italic: TextBox =
    modifyStyle(_.modifyFont(_.withStyle(FontStyle.Italic)))
  def noItalic: TextBox =
    modifyStyle(_.modifyFont(_.withStyle(FontStyle.Normal)))

  def alignLeft: TextBox =
    modifyStyle(_.withAlign(TextAlign.Left))
  def alignCenter: TextBox =
    modifyStyle(_.withAlign(TextAlign.Center))
  def alignRight: TextBox =
    modifyStyle(_.withAlign(TextAlign.Right))
  def alignStart: TextBox =
    modifyStyle(_.withAlign(TextAlign.Start))
  def alignEnd: TextBox =
    modifyStyle(_.withAlign(TextAlign.End))
  //

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def moveTo(pt: Point): TextBox =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): TextBox =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): TextBox =
    moveTo(newPosition)

  def moveBy(pt: Point): TextBox =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): TextBox =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): TextBox =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): TextBox =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): TextBox =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): TextBox =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): TextBox =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): TextBox =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): TextBox =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): TextBox =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def flipHorizontal(isFlipped: Boolean): TextBox =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): TextBox =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): TextBox =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): TextBox =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): TextBox =
    withRef(Point(x, y))

  def withEventHandler(f: ((TextBox, GlobalEvent)) => Option[GlobalEvent]): TextBox =
    this.copy(eventHandler = f, eventHandlerEnabled = true)
  def onEvent(f: PartialFunction[(TextBox, GlobalEvent), GlobalEvent]): TextBox =
    withEventHandler(f.lift)
  def enableEvents: TextBox =
    this.copy(eventHandlerEnabled = true)
  def disableEvents: TextBox =
    this.copy(eventHandlerEnabled = false)

object TextBox:
  def apply(text: String): TextBox =
    TextBox(
      text,
      TextStyle.default,
      Size(300),
      false,
      Function.const(None),
      Point.zero,
      Radians.zero,
      Vector2.one,
      Point.zero,
      Flip.default
    )

  def apply(text: String, maxWidth: Int, maxHeight: Int): TextBox =
    TextBox(
      text,
      TextStyle.default,
      Size(maxWidth, maxHeight),
      false,
      Function.const(None),
      Point.zero,
      Radians.zero,
      Vector2.one,
      Point.zero,
      Flip.default
    )
