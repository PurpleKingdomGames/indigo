package indigo.shared.scenegraph

import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName

class SceneLayerTests extends munit.FunSuite {

  val dummyGraphic: Graphic =
    Graphic(Rectangle.zero, 0, Material.Basic(AssetName("foo")))

  test("can provide a scene node count for an empty scene") {

    val nodes: List[SceneGraphNode] =
      Nil

    val layer: SceneLayer =
      new SceneLayer(nodes, RGBA.None, 1.0, None)

    assertEquals(layer.visibleNodeCount, 0)
  }

  test("can provide a scene node count for flat scene") {

    val nodes: List[SceneGraphNode] =
      List(
        dummyGraphic,
        dummyGraphic,
        dummyGraphic
      )

    val layer: SceneLayer =
      new SceneLayer(nodes, RGBA.None, 1.0, None)

    assertEquals(layer.visibleNodeCount, 3)
  }

  test("can provide a scene node count for grouped scene") {

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

    assertEquals(layer.visibleNodeCount, 11)
  }

}
