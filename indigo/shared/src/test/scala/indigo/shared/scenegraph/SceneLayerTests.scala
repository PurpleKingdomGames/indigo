package indigo.shared.scenegraph

import utest._
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName

object SceneLayerTests extends TestSuite {
  
  val dummyGraphic: Graphic =
    Graphic(Rectangle.zero, 0, Material.Textured(AssetName("foo")))

  val tests: Tests =
    Tests {

      "can provide a scene node count for an empty scene" - {

        val nodes: List[SceneGraphNode] =
          Nil

        val layer: SceneLayer =
          new SceneLayer(nodes, RGBA.None, 1.0, None)

        layer.visibleNodeCount ==> 0
      }

      "can provide a scene node count for flat scene" - {

        val nodes: List[SceneGraphNode] =
          List(
            dummyGraphic,
            dummyGraphic,
            dummyGraphic
          )

        val layer: SceneLayer =
          new SceneLayer(nodes, RGBA.None, 1.0, None)

        layer.visibleNodeCount ==> 3
      }

      "can provide a scene node count for grouped scene" - {

        val nodes: List[SceneGraphNode] =
          List(
            dummyGraphic,
            dummyGraphic,
            dummyGraphic,
            Group(
              dummyGraphic
            ),
            dummyGraphic,
            Group(
              dummyGraphic,
              dummyGraphic,
              Group(
                dummyGraphic,
                dummyGraphic
              ),
              dummyGraphic
            ),
            dummyGraphic
          )

        val layer: SceneLayer =
          new SceneLayer(nodes, RGBA.None, 1.0, None)

        layer.visibleNodeCount ==> 11
      }

    }

}
