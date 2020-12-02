package indigo.shared.platform

import indigo.shared.scenegraph.Graphic
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.BoundaryLocator
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Point
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayClone
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.scenegraph.Group
import indigo.shared.datatypes.Depth

class DisplayObjectConversionsTests extends munit.FunSuite {

  val graphic: Graphic =
    Graphic(Rectangle(10, 20, 200, 100), 2, Material.Textured(AssetName("texture")))

  val animationRegister          = new AnimationsRegister
  val fontRegister               = new FontRegister
  val boundaryLocator            = new BoundaryLocator(animationRegister, fontRegister)
  val texture                    = new TextureRefAndOffset("texture", Vector2(100, 100), Point.zero)
  val assetMapping: AssetMapping = new AssetMapping(Map("texture" -> texture))

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
        assetMapping
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

    assertEquals(actual.x, 10.0f)
    assertEquals(actual.y, 20.0f)
    assertEquals(actual.z, 2.0f)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
    assertEquals(actual.scaleX, 1.0f)
    assertEquals(actual.scaleY, 1.0f)
    assertEquals(actual.rotation, 0.0f)
  }

  test("convert a group with a graphic in it") {
    val actual: DisplayObject =
      convert(
        Group(graphic)
          .moveBy(5, 15)
          .withDepth(Depth(100))
      )

    assertEquals(actual.x, 15.0f)
    assertEquals(actual.y, 35.0f)
    assertEquals(actual.z, 102.0f)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
    assertEquals(actual.scaleX, 1.0f)
    assertEquals(actual.scaleY, 1.0f)
    assertEquals(actual.rotation, 0.0f)
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

    assertEquals(actual.x, 10.0f)
    assertEquals(actual.y, 20.0f)
    assertEquals(actual.z, 2.0f)
    assertEquals(actual.width, 200.0f)
    assertEquals(actual.height, 100.0f)
    assertEquals(actual.scaleX, 1.0f)
    assertEquals(actual.scaleY, 1.0f)
    assertEquals(actual.rotation, 0.0f)
  }

}
