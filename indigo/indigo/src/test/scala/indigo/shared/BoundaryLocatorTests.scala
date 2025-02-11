package indigo.shared

import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Stroke
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Text

class BoundaryLocatorTests extends munit.FunSuite {

  import Samples._

  val fontRegister: FontRegister =
    new FontRegister

  fontRegister.register(fontInfo)

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, fontRegister)

  test("Text boundary calculations.Text as lines with bounds.empty") {
    val actual = boundaryLocator.textAsLinesWithBounds("", fontKey, 0, 0)
    assertEquals(actual.length, 0)
  }
  test("Text boundary calculations.Text as lines with bounds.abc") {
    val actual = boundaryLocator.textAsLinesWithBounds("abc", fontKey, 0, 0)
    assertEquals(actual.length, 1)
    assertEquals(actual.headOption.get.text, "abc")
    assertEquals(actual.headOption.get.lineBounds, Rectangle(0, 0, 42, 20))
  }
  test("Text boundary calculations.Text as lines with bounds.ab->c") {
    val actual = boundaryLocator.textAsLinesWithBounds("ab\nc", fontKey, 0, 0)
    assertEquals(actual.length, 2)
    assertEquals(actual(0).text, "ab")
    assertEquals(actual(0).lineBounds, Rectangle(0, 0, 26, 20))
    assertEquals(actual(1).text, "c")
    assertEquals(actual(1).lineBounds, Rectangle(0, 20, 16, 16))
  }
  test("Text boundary calculations.Text as lines with bounds.abc with letterSpacing") {
    val actual = boundaryLocator.textAsLinesWithBounds("abc", fontKey, 10, 0)
    assertEquals(actual.length, 1)
    assertEquals(actual.head.text, "abc")
    assertEquals(actual.head.lineBounds, Rectangle(0, 0, 42 + 20, 20))
  }

  test("Text boundary calculations.textAllLineBounds with bounds.empty") {
    val actual = boundaryLocator.textAllLineBounds("", fontKey, 0, 0)
    assertEquals(actual.length, 0)
  }
  test("Text boundary calculations.textAllLineBounds with bounds.abc") {
    val actual = boundaryLocator.textAllLineBounds("abc", fontKey, 0, 0)
    assertEquals(actual.length, 1)
    assertEquals(actual.headOption.get, Rectangle(0, 0, 42, 20))
  }
  test("Text boundary calculations.textAllLineBounds with bounds.ab->c") {
    val actual = boundaryLocator.textAllLineBounds("ab\nc", fontKey, 0, 0)
    assertEquals(actual.length, 2)
    assertEquals(actual(0), Rectangle(0, 0, 26, 20))
    assertEquals(actual(1), Rectangle(0, 20, 16, 16))
  }
  test("Text boundary calculations.textAllLineBounds with bounds.ab->c with lineHeight") {
    val lineHeight = 10
    val actual     = boundaryLocator.textAllLineBounds("ab\nc", fontKey, 0, lineHeight)
    assertEquals(actual.length, 2)
    assertEquals(actual(0), Rectangle(0, 0, 26, 20 + lineHeight))
    assertEquals(actual(1), Rectangle(0, 20 + lineHeight, 16, 16 + lineHeight))
  }
  test("Text boundary calculations.textAllLineBounds with bounds.ab->c with letterSpacing") {
    val letterSpacing = 10
    val actual        = boundaryLocator.textAllLineBounds("ab\nc", fontKey, letterSpacing, 0)
    assertEquals(actual.length, 2)
    assertEquals(actual(0), Rectangle(0, 0, 26 + letterSpacing, 20))
    assertEquals(actual(1), Rectangle(0, 20, 16, 16))
  }
  test("Text boundary calculations.textAllLineBounds with bounds.ab->c with lineHeight and letterSpacing") {
    val lineHeight    = 10
    val letterSpacing = 10
    val actual        = boundaryLocator.textAllLineBounds("ab\nc", fontKey, letterSpacing, lineHeight)
    assertEquals(actual.length, 2)
    assertEquals(actual(0), Rectangle(0, 0, 26 + letterSpacing, 20 + lineHeight))
    assertEquals(actual(1), Rectangle(0, 20 + lineHeight, 16, 16 + lineHeight))
  }

  // These should be identical, regardless of alignment.
  // The lines move around within the same bounding area.
  test("Text boundary calculations.Text bounds.unaligned.empty") {
    val actual = boundaryLocator.textBounds(text.alignLeft.withText(""))
    assertEquals(actual, Rectangle(50, 50, 0, 0))
  }
  test("Text boundary calculations.Text bounds.unaligned.abc") {
    val actual = boundaryLocator.textBounds(text.alignLeft.withText("abc"))
    assertEquals(actual, Rectangle(50, 50, 42, 20))
  }
  test("Text boundary calculations.Text bounds.unaligned.ab->c") {
    val actual = boundaryLocator.textBounds(text.alignLeft.withText("ab\nc"))
    assertEquals(actual, Rectangle(50, 50, 26, 36))
  }

  object Samples {
    val material = Material.Bitmap(AssetName("font-sheet"))

    val chars = Batch(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 10, 20),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("boundary locator tests")

    val fontInfo = FontInfo(fontKey, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    val text: Text[?] =
      Text("<test>", 50, 50, fontKey, material)
  }

  // Standard bounds calculation

  import indigo.shared.scenegraph.EntityNode
  import indigo.shared.datatypes.Point
  import indigo.shared.datatypes.Size
  import indigo.shared.datatypes.Radians
  import indigo.shared.datatypes.Vector2
  import indigo.shared.datatypes.Flip
  import indigo.shared.shader.ShaderData
  import indigo.shared.shader.ShaderId

  final case class TestEntity(
      position: Point,
      size: Size,
      rotation: Radians,
      scale: Vector2,
      flip: Flip,
      ref: Point
  ) extends EntityNode[TestEntity]:
    def toShaderData: ShaderData                                         = ShaderData(ShaderId("test shader"))
    val eventHandlerEnabled: Boolean                                     = false
    def eventHandler: ((TestEntity, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

  test("EntityNode bounds - normal") {
    val entity = TestEntity(
      Point.zero,
      Size(40, 40),
      Radians.zero,
      Vector2.one,
      Flip.default,
      Point.zero
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(0, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal moved") {
    val entity = TestEntity(
      Point(10, 20),
      Size(40, 40),
      Radians.zero,
      Vector2.one,
      Flip.default,
      Point.zero
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Size(30, 40), entity.ref)
    val expected = Rectangle(10, 20, 30, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - flipped") {
    val entity = TestEntity(
      Point.zero,
      Size(40, 40),
      Radians.zero,
      Vector2.one,
      Flip(true, true),
      Point.zero
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(0, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - centered ref") {
    val entity = TestEntity(
      Point.zero,
      Size(40, 40),
      Radians.zero,
      Vector2.one,
      Flip.default,
      Point(20, 20)
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(-20, -20, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - bottom right ref") {
    val entity = TestEntity(
      Point.zero,
      Size(40, 40),
      Radians.zero,
      Vector2.one,
      Flip.default,
      Point(40, 40)
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(-40, -40, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - bottom negative ref") {
    val entity = TestEntity(
      Point.zero,
      Size(40, 40),
      Radians.zero,
      Vector2.one,
      Flip.default,
      Point(-10, 0)
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(10, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal scaled x1") {
    val entity = TestEntity(
      Point.zero,
      Size(40, 40),
      Radians.zero,
      Vector2(1, 1),
      Flip.default,
      Point.zero
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(0, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal scaled x2") {
    val entity = TestEntity(
      Point.zero,
      Size(40, 40),
      Radians.zero,
      Vector2(2, 2),
      Flip.default,
      Point.zero
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(0, 0, 80, 80)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - center ref scaled x3") {
    val entity = TestEntity(
      Point.zero,
      Size(40, 40),
      Radians.zero,
      Vector2(3, 3),
      Flip.default,
      Point(20, 20)
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(-60, -60, 120, 120)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal scaled x2 rotated tau / 2") {
    val entity = TestEntity(
      Point.zero,
      Size(20, 40),
      Radians.TAUby2,
      Vector2(2, 2),
      Flip.default,
      Point.zero
    )

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(-40, -80, 40, 80)

    assertEquals(actual, expected)
  }

  test("calculateShapeBounds - box (no stroke)") {

    val s: Shape.Box =
      Shape.Box(
        dimensions = Rectangle(0, 0, 200, 100),
        fill = Fill.None,
        stroke = Stroke.None
      )

    val expected =
      Rectangle(0, 0, 200, 100)

    assertEquals(BoundaryLocator.findShapeBounds(s), expected)
  }

  test("calculateShapeBounds - box (with stroke)") {

    val s: Shape.Box =
      Shape.Box(
        dimensions = Rectangle(15, 25, 100, 200),
        fill = Fill.None,
        stroke = Stroke(8, RGBA.Red)
      )

    val expected =
      Rectangle(15 - 4, 25 - 4, 100 + 8, 200 + 8)

    assertEquals(BoundaryLocator.findShapeBounds(s), expected)
  }

  test("calculateShapeBounds - circle") {

    val s: Shape.Circle =
      Shape.Circle(
        center = Point(50, 50),
        radius = 17,
        fill = Fill.None,
        stroke = Stroke(7, RGBA.Red)
      )

    val expected =
      Rectangle(50 - 17 - 3, 50 - 17 - 3, 17 + 17 + 7, 17 + 17 + 7).toSquare

    assertEquals(BoundaryLocator.findShapeBounds(s), expected)
  }

  test("calculateShapeBounds - line") {

    val start = Point(50, 10)
    val end   = Point(75, 60)

    val strokeWidth    = 5
    val strokeWidthBy2 = strokeWidth / 2

    val s: Shape.Line =
      Shape.Line(start, end, Stroke(strokeWidth, RGBA.Red))

    val expected =
      Rectangle(
        start.x - strokeWidthBy2,
        start.y - strokeWidthBy2,
        end.x - start.x + strokeWidth + strokeWidthBy2,
        end.y - start.y + strokeWidth + strokeWidthBy2
      )

    assertEquals(BoundaryLocator.findShapeBounds(s), expected)
  }

  test("calculateShapeBounds - line 2") {

    val start = Point(30, 80)
    val end   = Point(100, 20)

    val strokeWidth    = 10
    val strokeWidthBy2 = strokeWidth / 2

    val s: Shape.Line =
      Shape.Line(start, end, Stroke(strokeWidth, RGBA.Red))

    val expected =
      Rectangle(
        start.x - strokeWidthBy2,
        end.y - strokeWidthBy2,
        end.x - start.x + strokeWidth + strokeWidthBy2,
        start.y - end.y + strokeWidth + strokeWidthBy2
      )

    assertEquals(BoundaryLocator.findShapeBounds(s), expected)
  }

  test("calculateShapeBounds - polygon") {

    val verts =
      Batch(
        Point(50, 10),
        Point(75, 60),
        Point(25, 60)
      )

    val s: Shape.Polygon =
      Shape.Polygon(
        vertices = verts,
        fill = Fill.None,
        stroke = Stroke(4, RGBA.Red)
      )

    val expected =
      Rectangle(25 - 2, 10 - 2, 50 + 4, 50 + 4).toSquare

    assertEquals(BoundaryLocator.findShapeBounds(s), expected)
  }

}
