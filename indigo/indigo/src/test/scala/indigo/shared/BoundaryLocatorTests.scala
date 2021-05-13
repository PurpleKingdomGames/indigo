package indigo.shared

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
  test("Text boundary calculations.Text bounds.aligned left.empty") {
    val actual = boundaryLocator.textBounds(text.alignLeft.withText("")).get
    assertEquals(actual, Rectangle(50, 50, 0, 0))
  }
  test("Text boundary calculations.Text bounds.aligned left.abc") {
    val actual = boundaryLocator.textBounds(text.alignLeft.withText("abc")).get
    assertEquals(actual, Rectangle(50, 50, 42, 20))
  }
  test("Text boundary calculations.Text bounds.aligned left.ab->c") {
    val actual = boundaryLocator.textBounds(text.alignLeft.withText("ab\nc")).get
    assertEquals(actual, Rectangle(50, 50, 26, 36))
  }

  test("Text boundary calculations.Text bounds.aligned right.empty") {
    val actual = boundaryLocator.textBounds(text.alignRight.withText("")).get
    assertEquals(actual, Rectangle(50, 50, 0, 0))
  }
  test("Text boundary calculations.Text bounds.aligned right.abc") {
    val actual = boundaryLocator.textBounds(text.alignRight.withText("abc")).get
    assertEquals(actual, Rectangle(50, 50, 42, 20))
  }
  test("Text boundary calculations.Text bounds.aligned right.ab->c") {
    val actual = boundaryLocator.textBounds(text.alignRight.withText("ab\nc")).get
    assertEquals(actual, Rectangle(50, 50, 26, 36))
  }

  test("Text boundary calculations.Text bounds.aligned center.empty") {
    val actual = boundaryLocator.textBounds(text.alignCenter.withText("")).get
    assertEquals(actual, Rectangle(50, 50, 0, 0))
  }
  test("Text boundary calculations.Text bounds.aligned center.abc") {
    val actual = boundaryLocator.textBounds(text.alignCenter.withText("abc")).get
    assertEquals(actual, Rectangle(50, 50, 42, 20))
  }
  test("Text boundary calculations.Text bounds.aligned center.ab->c") {
    val actual = boundaryLocator.textBounds(text.alignCenter.withText("ab\nc")).get
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
  import indigo.shared.datatypes.Radians
  import indigo.shared.datatypes.Vector2
  import indigo.shared.datatypes.Depth
  import indigo.shared.datatypes.Flip
  import indigo.shared.materials.ShaderData
  import indigo.shared.shader.ShaderId

  test("EntityNode bounds - normal") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(40, 40))
    val expected = Rectangle(0, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal moved") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point(10, 20)
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(30, 40))
    val expected = Rectangle(10, 20, 30, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - flipped") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(40, 40))
    val expected = Rectangle(0, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - centered ref") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(40, 40))
    val expected = Rectangle(-20, -20, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - bottom right ref") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(40, 40))
    val expected = Rectangle(-40, -40, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - bottom negative ref") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(40, 40))
    val expected = Rectangle(10, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal scaled x1") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(40, 40))
    val expected = Rectangle(0, 0, 40, 40)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal scaled x2") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(40, 40))
    val expected = Rectangle(0, 0, 80, 80)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - center ref scaled x3") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(40, 40))
    val expected = Rectangle(-60, -60, 120, 120)

    assertEquals(actual, expected)
  }

  test("EntityNode bounds - normal scaled x2 rotated tau / 2") {
    val entity: EntityNode = new EntityNode {
      def position: Point   = Point.zero
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

    val actual   = BoundaryLocator.findBounds(entity, entity.position, Point(20, 40))
    val expected = Rectangle(-40, -80, 40, 80)

    assertEquals(actual, expected)
  }

}
