package indigo.shared.platform

import indigo.platform.assets.AtlasId
import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister
import indigo.shared.QuickCache
import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.config.RenderingTechnology
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Vector2
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.display.DisplayCloneTiles
import indigo.shared.display.DisplayGroup
import indigo.shared.display.DisplayMutants
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayTextLetters
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.SceneNode
import indigo.shared.shader.Uniform
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

import scala.scalajs.js.JSConverters.*

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class DisplayObjectConversionsTests extends munit.FunSuite {

  val graphic: Graphic[?] =
    Graphic(Rectangle(10, 20, 200, 100), Material.Bitmap(AssetName("texture")))

  val animationRegister = new AnimationsRegister
  val fontRegister      = new FontRegister
  val boundaryLocator   = new BoundaryLocator(animationRegister, fontRegister)
  val texture = new TextureRefAndOffset(AtlasId("texture"), Vector2(100, 100), Vector2.zero, Vector2(200, 100))
  val assetMapping: AssetMapping = new AssetMapping(scalajs.js.Dictionary("texture" -> texture))

  val cloneBlankMapping: scalajs.js.Dictionary[DisplayObject] = scalajs.js.Dictionary.empty[DisplayObject]

  implicit val cache: QuickCache[scalajs.js.Array[Float]] = QuickCache.empty

  val doc = new DisplayObjectConversions(
    boundaryLocator,
    animationRegister,
    fontRegister
  )

  def convert(node: SceneNode): DisplayObject = {
    doc.purgeCaches()

    doc
      .processSceneNodes(
        List(node).toJSArray,
        GameTime.is(Seconds(1)),
        assetMapping,
        cloneBlankMapping,
        RenderingTechnology.WebGL2,
        256,
        scalajs.js.Array[GlobalEvent](),
        (_: GlobalEvent) => ()
      )
      ._1
      .head match {
      case _: DisplayCloneBatch =>
        throw new Exception("failed (DisplayCloneBatch)")

      case _: DisplayCloneTiles =>
        throw new Exception("failed (DisplayCloneTiles)")

      case _: DisplayMutants =>
        throw new Exception("failed (DisplayMutants)")

      case _: DisplayTextLetters =>
        throw new Exception("failed (DisplayTextLetters)")

      case _: DisplayGroup =>
        throw new Exception("failed (DisplayGroup)")

      case d: DisplayObject =>
        d
    }
  }

  override def beforeEach(context: BeforeEach): Unit =
    cache.purgeAllNow()

  test("convert a graphic to a display object") {
    val actual: DisplayObject =
      convert(graphic)

    assertEquals(actual.x, 10.0f)
    assertEquals(actual.y, 20.0f)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
  }

  test("ubo packing") {

    import indigo.shared.shader.ShaderPrimitive._

    val uniforms =
      Batch(
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

    val actual: scalajs.js.Array[Float] =
      DisplayObjectConversions.packUBO(uniforms, "", true)

    assertEquals(actual.toList, expected.toList)

  }

  test("ubo packing - do not straddle byte boundaries") {

    import indigo.shared.shader.ShaderPrimitive._

    val uniforms =
      Batch(
        Uniform("a") -> float(1),
        Uniform("b") -> vec2(2, 3)
      )

    val expected: scalajs.js.Array[Float] =
      scalajs.js
        .Array[scalajs.js.Array[Float]](
          scalajs.js.Array[Float](1, 0, 2, 3)
        )
        .flatten

    val actual: scalajs.js.Array[Float] =
      DisplayObjectConversions.packUBO(uniforms, "", true)

    assertEquals(actual.toList, expected.toList)

  }

  test("ubo packing - arrays") {

    import indigo.shared.shader.ShaderPrimitive._

    val uniforms =
      Batch(
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
      DisplayObjectConversions.packUBO(uniforms, "", true).toList,
      expected.toList
    )

    // Exact 3 array.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(3)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0)
    )

    // 4 array padded.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(4)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0)
    )

    // 5 array padded.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(5)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
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
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(6)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
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
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(7)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
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
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(8)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
        .toList,
      expected.toList ++ List[Float](6, 6, 0, 0, 7, 7, 0, 0, 8, 8, 0, 0) ++ List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0) ++ List[Float](0, 0, 0, 0) ++ List[Float](0, 0, 0, 0) ++
        List[Float](0, 0, 0, 0)
    )

    // 16 array padded.
    assertEquals(
      DisplayObjectConversions
        .packUBO(uniforms :+ Uniform("VERTICES") -> array(16)(vec2(6.0), vec2(7.0), vec2(8.0)), "", true)
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
      Batch(
        Uniform("TEST") -> rawArray(Array(0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f))
      )

    val expected: Array[Float] =
      Array(0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f)

    assertEquals(
      DisplayObjectConversions.packUBO(uniforms, "", true).toList,
      expected.toList
    )

  }

}
