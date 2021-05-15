package indigo.shared

import indigo.shared.scenegraph.Shape
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.Stroke
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.FontInfo
import indigo.shared.materials.Material
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.Text
import indigo.platform.assets.DynamicText

class BoundaryLocatorTests extends munit.FunSuite {

  import Samples._

  val fontRegister: FontRegister =
    new FontRegister

  fontRegister.register(fontInfo)

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, fontRegister, new DynamicText)

  test("Text boundary calculations.Text as lines with bounds.empty") {
    val actual = boundaryLocator.textAsLinesWithBounds("", fontKey)
    assertEquals(actual.length, 0)
  }
  test("Text boundary calculations.Text as lines with bounds.abc") {
    val actual = boundaryLocator.textAsLinesWithBounds("abc", fontKey)
    assertEquals(actual.length, 1)
    assertEquals(actual.headOption.get.text, "abc")
    assertEquals(actual.headOption.get.lineBounds, Rectangle(0, 0, 42, 20))
  }
  test("Text boundary calculations.Text as lines with bounds.ab->c") {
    val actual = boundaryLocator.textAsLinesWithBounds("ab\nc", fontKey)
    assertEquals(actual.length, 2)
    assertEquals(actual(0).text, "ab")
    assertEquals(actual(0).lineBounds, Rectangle(0, 0, 26, 20))
    assertEquals(actual(1).text, "c")
    assertEquals(actual(1).lineBounds, Rectangle(0, 20, 16, 16))
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

    val chars = List(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 10, 20),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("boundary locator tests")

    val fontInfo = FontInfo(fontKey, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    val text: Text =
      Text("<test>", 50, 50, 1, fontKey, material)
  }

  // Standard bounds calculation

  import indigo.shared.scenegraph.EntityNode
  import indigo.shared.datatypes.Point
  import indigo.shared.datatypes.Size
  import indigo.shared.datatypes.Radians
  import indigo.shared.datatypes.Vector2
  import indigo.shared.datatypes.Depth
  import indigo.shared.datatypes.Flip
  import indigo.shared.materials.ShaderData
  import indigo.shared.shader.ShaderId

  test("EntityNode bounds - normal") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
      def size: Size        = Size(40, 40)
      def rotation: Radians = Radians.zero
      def scale: Vector2    = Vector2.one
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip.default
      def ref: Point        = Point.zero

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(0, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal moved") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point(10, 20)
      def size: Size        = Size(40, 40)
      def rotation: Radians = Radians.zero
      def scale: Vector2    = Vector2.one
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip.default
      def ref: Point        = Point.zero

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Size(30, 40), entity.ref)
    val expected = Rectangle(10, 20, 30, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - flipped") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
      def size: Size        = Size(40, 40)
      def rotation: Radians = Radians.zero
      def scale: Vector2    = Vector2.one
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip(true, true)
      def ref: Point        = Point.zero

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(0, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - centered ref") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
      def size: Size        = Size(40, 40)
      def rotation: Radians = Radians.zero
      def scale: Vector2    = Vector2.one
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip.default
      def ref: Point        = Point(20, 20)

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(-20, -20, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - bottom right ref") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
      def size: Size        = Size(40, 40)
      def rotation: Radians = Radians.zero
      def scale: Vector2    = Vector2.one
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip.default
      def ref: Point        = Point(40, 40)

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(-40, -40, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - bottom negative ref") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
      def size: Size        = Size(40, 40)
      def rotation: Radians = Radians.zero
      def scale: Vector2    = Vector2.one
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip.default
      def ref: Point        = Point(-10, 0)

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(10, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal scaled x1") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
      def size: Size        = Size(40, 40)
      def rotation: Radians = Radians.zero
      def scale: Vector2    = Vector2(1, 1)
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip.default
      def ref: Point        = Point.zero

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(0, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal scaled x2") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
      def size: Size        = Size(40, 40)
      def rotation: Radians = Radians.zero
      def scale: Vector2    = Vector2(2, 2)
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip.default
      def ref: Point        = Point.zero

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(0, 0, 80, 80)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - center ref scaled x3") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
      def size: Size        = Size(40, 40)
      def rotation: Radians = Radians.zero
      def scale: Vector2    = Vector2(3, 3)
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip.default
      def ref: Point        = Point(20, 20)

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

    val actual   = BoundaryLocator.findBounds(entity, entity.position, entity.size, entity.ref)
    val expected = Rectangle(-60, -60, 120, 120)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal scaled x2 rotated tau / 2") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
      def size: Size        = Size(20, 40)
      def rotation: Radians = Radians.TAUby2
      def scale: Vector2    = Vector2(2, 2)
      def depth: Depth      = Depth(0)
      def flip: Flip        = Flip.default
      def ref: Point        = Point.zero

      // Placeholder
      def bounds: Rectangle                      = Rectangle.zero
      def toShaderData: ShaderData               = ShaderData(ShaderId("test shader"))
      def withDepth(newDepth: Depth): EntityNode = this
      // Members declared in scala.Equals
      def canEqual(that: Any): Boolean = ???
      // Members declared in scala.Product
      def productArity: Int           = ???
      def productElement(n: Int): Any = ???
    }

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

    assertEquals(boundaryLocator.shapeBounds(s), expected)
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

    assertEquals(boundaryLocator.shapeBounds(s), expected)
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

    assertEquals(boundaryLocator.shapeBounds(s), expected)
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

    assertEquals(boundaryLocator.shapeBounds(s), expected)
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

    assertEquals(boundaryLocator.shapeBounds(s), expected)
  }

  test("calculateShapeBounds - polygon") {

    val verts =
      List(
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

    assertEquals(boundaryLocator.shapeBounds(s), expected)
  }

}
