package indigo.shared.platform

import indigo.shared.scenegraph.Graphic
import indigo.shared.datatypes.Rectangle
import indigo.shared.materials.StandardMaterial
import indigo.shared.assets.AssetName
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.BoundaryLocator
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Vector3
import indigo.shared.datatypes.Point
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayClone
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.scenegraph.Group
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.datatypes.Radians

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class DisplayObjectConversionsTests extends munit.FunSuite {

  val graphic: Graphic =
    Graphic(Rectangle(10, 20, 200, 100), 2, StandardMaterial.Bitmap(AssetName("texture")))

  val animationRegister          = new AnimationsRegister
  val fontRegister               = new FontRegister
  val boundaryLocator            = new BoundaryLocator(animationRegister, fontRegister)
  val texture                    = new TextureRefAndOffset("texture", Vector2(100, 100), Point.zero)
  val assetMapping: AssetMapping = new AssetMapping(Map("texture" -> texture))

  val cloneBlankMapping: Map[String, DisplayObject] = Map.empty[String, DisplayObject]

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

  test("create a CheapMatrix4 from a SceneGraphNode.translation") {

    val node: SceneGraphNode =
      Graphic(100, 100, StandardMaterial.Bitmap(AssetName("test")))
        .moveTo(10, 20)

    val expected: CheapMatrix4 =
      CheapMatrix4(
        (100, 0, 0, 0),
        (0, -100, 0, 0),
        (0, 0, 1, 0),
        (10 + 50, 20 + 50, 0, 1)
      )

    val actual =
      DisplayObjectConversions.nodeToMatrix4(node, Vector3(100.0d, 100.0d, 1.0d))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("create a CheapMatrix4 from a SceneGraphNode.translation with ref") {

    val node: SceneGraphNode =
      Graphic(100, 100, StandardMaterial.Bitmap(AssetName("test")))
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
      DisplayObjectConversions.nodeToMatrix4(node, Vector3(100.0d, 100.0d, 1.0d))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("create a CheapMatrix4 from a SceneGraphNode.scale") {

    val node: SceneGraphNode =
      Graphic(100, 100, StandardMaterial.Bitmap(AssetName("test")))
        .scaleBy(2, 10)

    val expected: CheapMatrix4 =
      CheapMatrix4(
        (200, 0, 0, 0),
        (0, -1000, 0, 0),
        (0, 0, 1, 0),
        (100, 500, 0, 1)
      )

    val actual =
      DisplayObjectConversions.nodeToMatrix4(node, Vector3(100.0d, 100.0d, 1.0d))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

  test("create a CheapMatrix4 from a SceneGraphNode.rotation") {

    val node: SceneGraphNode =
      Graphic(100, 100, StandardMaterial.Bitmap(AssetName("test")))
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
      DisplayObjectConversions.nodeToMatrix4(node, Vector3(100.0d, 100.0d, 1.0d))

    assert(clue(actual.toMatrix4) ~== clue(expected.toMatrix4))
  }

  test("create a CheapMatrix4 from a SceneGraphNode.translation with flip") {

    val width: Int  = 100
    val height: Int = 100

    val node: SceneGraphNode =
      Graphic(width, height, StandardMaterial.Bitmap(AssetName("test")))
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
      DisplayObjectConversions.nodeToMatrix4(node, Vector3(width.toDouble, height.toDouble, 1.0d))

    assertEquals(actual.toMatrix4, expected.toMatrix4)
  }

}
