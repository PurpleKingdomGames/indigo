package indigo.shared.platform

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
import indigo.shared.scenegraph.CloneId
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayText
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.scenegraph.Group
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.datatypes.Radians
import indigo.shared.shader.Uniform
import indigo.shared.scenegraph.RenderNode
import indigo.shared.assets.AssetName
import indigo.platform.assets.AtlasId
import indigo.platform.assets.DynamicText
import indigo.shared.QuickCache
import indigo.shared.display.DisplayGroup

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class DisplayObjectConversionsTests extends munit.FunSuite {

  val graphic: Graphic[_] =
    Graphic(Rectangle(10, 20, 200, 100), 2, Material.Bitmap(AssetName("texture")))

  val animationRegister = new AnimationsRegister
  val fontRegister      = new FontRegister
  val boundaryLocator   = new BoundaryLocator(animationRegister, fontRegister, new DynamicText)
  val texture = new TextureRefAndOffset(AtlasId("texture"), Vector2(100, 100), Vector2.zero, Vector2(200, 100))
  val assetMapping: AssetMapping = new AssetMapping(Map(AssetName("texture") -> texture))

  val cloneBlankMapping: Map[CloneId, DisplayObject] = Map.empty[CloneId, DisplayObject]

  implicit val cache: QuickCache[Array[Float]] = QuickCache.empty

  val doc = new DisplayObjectConversions(
    boundaryLocator,
    animationRegister,
    fontRegister
  )

  def convert(node: SceneGraphNode): DisplayObject = {
    doc.purgeCaches()

    doc
      .sceneNodesToDisplayObjects(
        List(node),
        GameTime.is(Seconds(1)),
        assetMapping,
        cloneBlankMapping
      )
      .head match {
      case _: DisplayCloneBatch =>
        throw new Exception("failed (DisplayCloneBatch)")

      case _: DisplayText =>
        throw new Exception("failed (DisplayText)")

      case _: DisplayGroup =>
        throw new Exception("failed (DisplayGroup)")

      case d: DisplayObject =>
        d
    }
  }

  def convertWithGroup(node: SceneGraphNode): DisplayGroup = {
    doc.purgeCaches()

    doc
      .sceneNodesToDisplayObjects(
        List(node),
        GameTime.is(Seconds(1)),
        assetMapping,
        cloneBlankMapping
      )
      .head match {
      case _: DisplayCloneBatch =>
        throw new Exception("failed (DisplayCloneBatch)")

      case _: DisplayText =>
        throw new Exception("failed (DisplayText)")

      case _: DisplayObject =>
        throw new Exception("failed (DisplayObject)")

      case d: DisplayGroup =>
        d
    }
  }

  override def beforeEach(context: BeforeEach): Unit =
    cache.purgeAllNow()

  test("convert a graphic to a display object") {
    val actual: DisplayObject =
      convert(graphic)

    assertEquals(actual.transform.x, 110.0f)
    assertEquals(actual.transform.y, 70.0f)
    assertEquals(actual.z.toFloat, 2.0f)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
  }

  test("convert a group with a graphic in it") {
    val actual: DisplayObject =
      convertWithGroup(
        Group(graphic)
          .moveBy(5, 15)
          .withDepth(Depth(100))
      ).entities.head match
        case d: DisplayObject =>
          d

        case d =>
          throw new Exception("Got: " + d.toString)

    assertEquals(actual.transform.x, 110.0f)
    assertEquals(actual.transform.y, 70.0f)
    assertEquals(actual.z.toFloat, 2.0f)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
  }

  test("convert a group of a group with a graphic in it") {
    val actual: DisplayObject =
      convertWithGroup(
        Group(
          Group(
            graphic
          )
        )
      ).entities.head match
        case d: DisplayGroup =>
          d.entities.head match
            case dd: DisplayObject =>
              dd

            case dd =>
              throw new Exception("Got: " + d.toString)

        case d =>
          throw new Exception("Got: " + d.toString)

    assertEquals(actual.transform.x, 110.0f)
    assertEquals(actual.transform.y, 70.0f)
    assertEquals(actual.z.toFloat, 2.0f)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
  }

  test("create a CheapMatrix4 from a SceneGraphNode.translation") {

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
      DisplayObjectConversions.nodeToMatrix4(node, node.position.toVector, Vector3(100.0f, 100.0f, 1.0f))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("create a CheapMatrix4 from a SceneGraphNode.translation with ref") {

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
      DisplayObjectConversions.nodeToMatrix4(node, node.position.toVector, Vector3(100.0f, 100.0f, 1.0f))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("create a CheapMatrix4 from a SceneGraphNode.scale") {

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
      DisplayObjectConversions.nodeToMatrix4(node, node.position.toVector, Vector3(100.0f, 100.0f, 1.0f))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("create a CheapMatrix4 from a SceneGraphNode.rotation") {

    val node: RenderNode =
      Graphic(100, 100, Material.Bitmap(AssetName("test")))
        .rotateTo(Radians.TAUby4)

    val c = 0.0f
    val s = 100.0f

    val expected: CheapMatrix4 =
      CheapMatrix4(
        (c, s, 0, 0),
        (s, -c, 0, 0),
        (0, 0, 1, 0),
        (-50, 50, 0, 1)
      )

    val actual =
      DisplayObjectConversions.nodeToMatrix4(node, node.position.toVector, Vector3(100.0f, 100.0f, 1.0f))

    assert(clue(actual.toMatrix4) ~== clue(expected.toMatrix4))
  }

  test("create a CheapMatrix4 from a SceneGraphNode.translation with flip") {

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
        ((width.toFloat / 2) + 10, (height.toFloat / 2) + 20, 0, 1)
      )

    val actual =
      DisplayObjectConversions.nodeToMatrix4(
        node,
        node.position.toVector,
        Vector3(width.toDouble, height.toDouble, 1.0f)
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

  test("ubo packing - raw array of floats") {

    import indigo.shared.shader.ShaderPrimitive._

    val uniforms =
      List(
        Uniform("TEST") -> rawArray(Array(0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f))
      )

    val expected: Array[Float] =
      Array(0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f)

    assertEquals(
      DisplayObjectConversions.packUBO(uniforms).toList,
      expected.toList
    )

  }

}
