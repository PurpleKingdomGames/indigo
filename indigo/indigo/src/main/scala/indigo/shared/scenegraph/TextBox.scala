package indigo.shared.scenegraph

import indigo.shared.scenegraph.syntax.BasicSpatial
import indigo.shared.scenegraph.syntax.Spatial

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
) extends RenderNode derives CanEqual:

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

  def withDepth(newDepth: Depth): TextBox =
    this.copy(depth = newDepth)

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

  given BasicSpatial[TextBox] with
    extension (textBox: TextBox)
      def position: Point =
        textBox.position
      def rotation: Radians =
        textBox.rotation
      def scale: Vector2 =
        textBox.scale
      def depth: Depth =
        textBox.depth
      def ref: Point =
        textBox.ref
      def flip: Flip =
        textBox.flip

      def withPosition(newPosition: Point): TextBox =
        textBox.copy(position = newPosition)
      def withRotation(newRotation: Radians): TextBox =
        textBox.copy(rotation = newRotation)
      def withScale(newScale: Vector2): TextBox =
        textBox.copy(scale = newScale)
      def withRef(newRef: Point): TextBox =
        textBox.copy(ref = newRef)
      def withRef(x: Int, y: Int): TextBox =
        withRef(Point(x, y))
      def withDepth(newDepth: Depth): TextBox =
        textBox.copy(depth = newDepth)
      def withFlip(newFlip: Flip): TextBox =
        textBox.copy(flip = newFlip)

  given Spatial[TextBox] = Spatial.default[TextBox]
