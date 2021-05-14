package indigo.shared.scenegraph

import indigo._
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.platform.assets.DynamicText

class ShapeTests extends munit.FunSuite:

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText)

  test("Bounds calculation - box") {

    val s: Shape.Box =
      Shape.Box(
        dimensions = Rectangle(15, 25, 100, 200),
        fill = Fill.None,
        stroke = Stroke(8, RGBA.Red)
      )

    val actual =
      s.calculatedBounds(boundaryLocator)

    val expected =
      Rectangle(15 - 4, 25 - 4, 100 + 8, 200 + 8)

    assertEquals(actual, expected)
  }

  test("Bounds calculation - circle") {

    val s: Shape.Circle =
      Shape.Circle(
        center = Point(50, 50),
        radius = 17,
        fill = Fill.None,
        stroke = Stroke(7, RGBA.Red)
      )

    val actual =
      s.calculatedBounds(boundaryLocator)

    val expected =
      Rectangle(50 - 17 - 3, 50 - 17 - 3, 17 + 17 + 7, 17 + 17 + 7)

    assertEquals(actual, expected)
  }

  test("Bounds calculation - line") {

    val s: Shape.Line =
      Shape.Line(
        start = Point(50, 10),
        end = Point(75, 60),
        stroke = Stroke(5, RGBA.Red)
      )

    val actual =
      s.calculatedBounds(boundaryLocator)

    val expected =
      Rectangle(50 - 2, 10 - 2, 25 + 5, 50 + 5)

    assertEquals(actual, expected)
  }

  test("Bounds calculation - polygon") {

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

    val actual =
      s.calculatedBounds(boundaryLocator)

    val expected =
      Rectangle(25 - 2, 10 - 2, 50 + 4, 50 + 4)

    assertEquals(actual, expected)

  }
