package indigo.shared.platform

import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Graphic
import indigo.shared.datatypes.Rectangle
import indigo.shared.materials.Material
import indigo.shared.assets.AssetName
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.BoundaryLocator
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Vector3
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.Stroke
import indigo.shared.datatypes.RGBA
import indigo.shared.scenegraph.CloneId
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayClone
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.Group
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.datatypes.Radians
import indigo.shared.shader.Uniform
import indigo.shared.scenegraph.RenderNode
import indigo.shared.assets.AssetName
import indigo.platform.assets.AtlasId

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class DisplayObjectConversionsTests extends munit.FunSuite {

  val graphic: Graphic =
    Graphic(Rectangle(10, 20, 200, 100), 2, Material.Bitmap(AssetName("texture")))

  val animationRegister          = new AnimationsRegister
  val fontRegister               = new FontRegister
  val boundaryLocator            = new BoundaryLocator(animationRegister, fontRegister)
  val texture                    = new TextureRefAndOffset(AtlasId("texture"), Vector2(100, 100), Point.zero)
  val assetMapping: AssetMapping = new AssetMapping(Map(AssetName("texture") -> texture))

  val cloneBlankMapping: Map[CloneId, DisplayObject] = Map.empty[CloneId, DisplayObject]

  val doc = new DisplayObjectConversions(
    boundaryLocator,
    animationRegister,
    fontRegister
  )

  def convert(node: SceneNode): DisplayObject = {
    doc.purgeCaches()

    doc
      .sceneNodesToDisplayObjects(
        List(node),
        GameTime.is(Seconds(1)),
        assetMapping,
        cloneBlankMapping
      )
      .head match {
      case _: DisplayClone =>
        throw new Exception("failed (DisplayClone)")

      case _: DisplayCloneBatch =>
        throw new Exception("failed (DisplayCloneBatch)")

      case d: DisplayObject =>
        d
    }
  }

  test("convert a graphic to a display object") {
    val actual: DisplayObject =
      convert(graphic)

    assertEquals(actual.transform.x, 110.0d)
    assertEquals(actual.transform.y, 70.0d)
    assertEquals(actual.z, 2.0d)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
  }

  test("convert a group with a graphic in it") {
    val actual: DisplayObject =
      convert(
        Group(graphic)
          .moveBy(5, 15)
          .withDepth(Depth(100))
      )

    assertEquals(actual.transform.x, 115.0d)
    assertEquals(actual.transform.y, 85.0d)
    assertEquals(actual.z, 102.0d)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
  }

  test("convert a group of a group with a graphic in it") {
    val actual: DisplayObject =
      convert(
        Group(
          Group(
            graphic
          )
        )
      )

    assertEquals(actual.transform.x, 110.0d)
    assertEquals(actual.transform.y, 70.0d)
    assertEquals(actual.z, 2.0d)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
  }

  test("create a CheapMatrix4 from a SceneNode.translation") {

    val node: RenderNode =
      Graphic(100, 100, Material.Bitmap(AssetName("test")))
        .moveTo(10, 20)

    val expected: CheapMatrix4 =
      CheapMatrix4(
        (100, 0, 0, 0),
        (0, -100, 0, 0),
        (0, 0, 1, 0),
        (10 + 50, 20 + 50, 0, 1)
      )

    val actual =
      DisplayObjectConversions.nodeToMatrix4(node, node.position.toVector, Vector3(100.0d, 100.0d, 1.0d))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("create a CheapMatrix4 from a SceneNode.translation with ref") {

    val node: RenderNode =
      Graphic(100, 100, Material.Bitmap(AssetName("test")))
        .moveTo(10, 20)
        .withRef(50, 50)

    val expected: CheapMatrix4 =
      CheapMatrix4(
        (100, 0, 0, 0),
        (0, -100, 0, 0),
        (0, 0, 1, 0),
        (-40 + 50, -30 + 50, 0, 1)
      )

    val actual =
      DisplayObjectConversions.nodeToMatrix4(node, node.position.toVector, Vector3(100.0d, 100.0d, 1.0d))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("create a CheapMatrix4 from a SceneNode.scale") {

    val node: RenderNode =
      Graphic(100, 100, Material.Bitmap(AssetName("test")))
        .scaleBy(2, 10)

    val expected: CheapMatrix4 =
      CheapMatrix4(
        (200, 0, 0, 0),
        (0, -1000, 0, 0),
        (0, 0, 1, 0),
        (100, 500, 0, 1)
      )

    val actual =
      DisplayObjectConversions.nodeToMatrix4(node, node.position.toVector, Vector3(100.0d, 100.0d, 1.0d))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("create a CheapMatrix4 from a SceneNode.rotation") {

    val node: RenderNode =
      Graphic(100, 100, Material.Bitmap(AssetName("test")))
        .rotateTo(Radians.TAUby4)

    val c = 0.0d
    val s = 100.0d

    val expected: CheapMatrix4 =
      CheapMatrix4(
        (c, s, 0, 0),
        (s, -c, 0, 0),
        (0, 0, 1, 0),
        (-50, 50, 0, 1)
      )

    val actual =
      DisplayObjectConversions.nodeToMatrix4(node, node.position.toVector, Vector3(100.0d, 100.0d, 1.0d))

    assert(clue(actual.toMatrix4) ~== clue(expected.toMatrix4))
  }

  test("create a CheapMatrix4 from a SceneNode.translation with flip") {

    val width: Int  = 100
    val height: Int = 100

    val node: RenderNode =
      Graphic(width, height, Material.Bitmap(AssetName("test")))
        .moveTo(10, 20)
        .flipHorizontal(true)
        .flipVertical(true)

    val expected: CheapMatrix4 =
      CheapMatrix4(
        (-100, 0, 0, 0),
        (0, 100, 0, 0),
        (0, 0, 1, 0),
        ((width.toDouble / 2) + 10, (height.toDouble / 2) + 20, 0, 1)
      )

    val actual =
      DisplayObjectConversions.nodeToMatrix4(
        node,
        node.position.toVector,
        Vector3(width.toDouble, height.toDouble, 1.0d)
      )

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("ubo packing") {

    import indigo.shared.shader.ShaderPrimitive._

    val uniforms =
      List(
        Uniform("a") -> float(1),
        Uniform("b") -> float(2),
        Uniform("c") -> vec3(3, 4, 5),
        Uniform("d") -> float(6),
        Uniform("e") -> array(4)(vec2(7, 8), vec2(9, 10), vec2(11, 12)),
        Uniform("f") -> float(13)
      )

    val expected: Array[Float] =
      Array[Array[Float]](
        Array[Float](1, 2, 0, 0),
        Array[Float](3, 4, 5, 0),
        Array[Float](6, 0, 0, 0),
        Array[Float](7, 8, 0, 0, 9, 10, 0, 0, 11, 12, 0, 0, 0, 0, 0, 0),
        Array[Float](13, 0, 0, 0)
      ).flatten

    val actual: Array[Float] =
      DisplayObjectConversions.packUBO(uniforms)

    assertEquals(actual.toList, expected.toList)

  }

  test("ubo packing - arrays") {

    import indigo.shared.shader.ShaderPrimitive._

    val uniforms =
      List(
        Uniform("ASPECT_RATIO") -> vec2(1.0),
        Uniform("STROKE_WIDTH") -> float(2.0),
        Uniform("COUNT")        -> float(3.0),
        Uniform("STROKE_COLOR") -> vec4(4.0),
        Uniform("FILL_COLOR")   -> vec4(5.0)
      )

    val expected: Array[Float] =
      Array[Array[Float]](
        Array[Float](1, 1),
        Array[Float](2),
        Array[Float](3),
        Array[Float](4, 4, 4, 4),
        Array[Float](5, 5, 5, 5)
      ).flatten

    assertEquals(
      DisplayObjectConversions.packUBO(uniforms).toList,
      expected.toList
    )

    // Exact 3 array.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(3)(vec2(6.0), vec2(7.0), vec2(8.0)))
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0)
    )

    // 4 array padded.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(4)(vec2(6.0), vec2(7.0), vec2(8.0)))
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0)
    )

    // 5 array padded.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(5)(vec2(6.0), vec2(7.0), vec2(8.0)))
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0) ++ List[Float](
        0,
        0,
        0,
        0
      )
    )

    // 6 array padded.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(6)(vec2(6.0), vec2(7.0), vec2(8.0)))
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0) ++ List[Float](
        0,
        0,
        0,
        0
      ) ++ List[Float](0, 0, 0, 0)
    )

    // 7 array padded.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(7)(vec2(6.0), vec2(7.0), vec2(8.0)))
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0) ++ List[Float](
        0,
        0,
        0,
        0
      ) ++ List[Float](0, 0, 0, 0) ++ List[Float](0, 0, 0, 0)
    )

    // 8 array padded.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(8)(vec2(6.0), vec2(7.0), vec2(8.0)))
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++ List[Float](0, 0, 0, 0) ++ List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0)
    )

    // 16 array padded.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(16)(vec2(6.0), vec2(7.0), vec2(8.0)))
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0)
    )

  }

  test("calculateShapeBounds - box (no stroke)") {

    val s: Shape.Box =
      Shape.Box(
        dimensions = Rectangle(0, 0, 200, 100),
        fill = Fill.None,
        stroke = Stroke.None
      )

    val expected =
      Rectangle(0, -50, 200, 100).toSquare

    assertEquals(DisplayObjectConversions.calculateShapeBounds(s), expected)
  }

  test("calculateShapeBounds - box (with stroke)") {

    val s: Shape.Box =
      Shape.Box(
        dimensions = Rectangle(15, 25, 100, 200),
        fill = Fill.None,
        stroke = Stroke(8, RGBA.Red)
      )

    val expected =
      Rectangle(15 - 4 - 50, 25 - 4, 100 + 8, 200 + 8).toSquare

    assertEquals(DisplayObjectConversions.calculateShapeBounds(s), expected)
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

    assertEquals(DisplayObjectConversions.calculateShapeBounds(s), expected)
  }

  test("calculateShapeBounds - line") {

    val s: Shape.Line =
      Shape.Line(
        start = Point(50, 10),
        end = Point(75, 60),
        stroke = Stroke(5, RGBA.Red)
      )

    val expected =
      Rectangle(50 - 2, 10 - 2, 25 + 5, 50 + 5).toSquare

    assertEquals(DisplayObjectConversions.calculateShapeBounds(s), expected)
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

    assertEquals(DisplayObjectConversions.calculateShapeBounds(s), expected)
  }

}
