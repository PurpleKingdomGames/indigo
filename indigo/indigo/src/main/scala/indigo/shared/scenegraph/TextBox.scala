package indigo.shared.scenegraph

import indigo.shared.datatypes.{Point, Size, Radians, Vector2, Rectangle, Depth, Flip}
import indigo.shared.materials.ShaderData
import indigo.shared.shader.StandardShaders
import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.TextStyle
import indigo.shared.datatypes.TextAlign
import indigo.shared.datatypes.TextStroke
import indigo.shared.datatypes.FontWeight
import indigo.shared.datatypes.FontStyle
import indigo.shared.datatypes.FontFamily
import indigo.shared.datatypes.Pixels
import indigo.shared.datatypes.RGBA

final case class TextBox(
    text: String,
    style: TextStyle,
    size: Size,
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends CompositeNode
    with SpatialModifiers[TextBox] derives CanEqual:

  def bounds: Rectangle =
    BoundaryLocator.findBounds(this, position, size)

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

  def calculatedBounds(locator: BoundaryLocator): Option[Rectangle] =
    locator.findBounds(this)

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

  def withDepth(newDepth: Depth): TextBox =
    this.copy(depth = newDepth)

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

object TextBox:
  def apply(text: String): TextBox =
    TextBox(
      text,
      TextStyle.default,
      Size(300),
      Point.zero,
      Radians.zero,
      Vector2.one,
      Depth.Zero,
      Point.zero,
      Flip.default
    )

  def apply(text: String, maxWidth: Int, maxHeight: Int): TextBox =
    TextBox(
      text,
      TextStyle.default,
      Size(maxWidth, maxHeight),
      Point.zero,
      Radians.zero,
      Vector2.one,
      Depth.Zero,
      Point.zero,
      Flip.default
    )
