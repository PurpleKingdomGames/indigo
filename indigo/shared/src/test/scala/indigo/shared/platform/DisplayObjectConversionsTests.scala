package indigo.shared.platform

import utest._
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

object DisplayObjectConversionsTests extends TestSuite {

  val tests: Tests =
    Tests {

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

      "convert a graphic to a display object" - {
        val actual: DisplayObject =
          convert(graphic)

        actual.x ==> 10.0f
        actual.y ==> 20.0f
        actual.z ==> 2.0f
        actual.width ==> 200.0f
        actual.height ==> 100.0f
        actual.scaleX ==> 1.0f
        actual.scaleY ==> 1.0f
        actual.rotation ==> 0.0f
      }

      "convert a group with a graphic in it" - {
        val actual: DisplayObject =
          convert(
            Group(graphic)
              .moveBy(5, 15)
              .withDepth(Depth(100))
          )

        actual.x ==> 15.0f
        actual.y ==> 35.0f
        actual.z ==> 102.0f
        actual.width ==> 200.0f
        actual.height ==> 100.0f
        actual.scaleX ==> 1.0f
        actual.scaleY ==> 1.0f
        actual.rotation ==> 0.0f
      }

      "convert a group of a group with a graphic in it" - {
        val actual: DisplayObject =
          convert(
            Group(
              Group(
                graphic
              )
            )
          )

        actual.x ==> 10.0f
        actual.y ==> 20.0f
        actual.z ==> 2.0f
        actual.width ==> 200.0f
        actual.height ==> 100.0f
        actual.scaleX ==> 1.0f
        actual.scaleY ==> 1.0f
        actual.rotation ==> 0.0f
      }

    }

}
