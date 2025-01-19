package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.RGBA
import indigo.shared.materials.BlendMaterial

class SceneUpdateFragmentTests extends munit.FunSuite {

  test("Able to add a batch of layers from a constructor") {

    val actual =
      SceneUpdateFragment(Batch(BindingKey("key A") -> Layer.empty, BindingKey("key B") -> Layer.empty))

    val expected =
      SceneUpdateFragment.empty.addLayers(
        Batch(LayerEntry(BindingKey("key A"), Layer.empty), LayerEntry(BindingKey("key B"), Layer.empty))
      )

    assertEquals(actual, expected)
  }

  test("Able to add an optional layer from a constructor (Some)") {

    val actual =
      SceneUpdateFragment(Option(BindingKey("key A") -> Layer.empty))

    val expected =
      SceneUpdateFragment.empty.addLayers(Batch(LayerEntry(BindingKey("key A"), Layer.empty)))

    assertEquals(actual, expected)

  }

  test("Able to add an optional layer from a constructor (None)") {

    val actual =
      SceneUpdateFragment(Option.empty[Layer])

    val expected =
      SceneUpdateFragment.empty

    assertEquals(actual, expected)

  }

  test("Adding a layer with an existing key merges magnification down (none, none)") {

    val scene =
      SceneUpdateFragment.empty.addLayer(LayerEntry(BindingKey("key A"), Layer.empty))

    val actual =
      scene.addLayer(BindingKey("key A") -> Layer.empty)

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.flatMap(_.toBatch).head.magnification, None)

  }

  test("Adding a layer with an existing key merges magnification down (some, some)") {

    val scene =
      SceneUpdateFragment.empty.addLayer(BindingKey("key A") -> Layer.empty.withMagnification(2))

    val actual =
      scene.addLayer(LayerEntry(BindingKey("key A"), Layer.empty.withMagnification(1)))

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.flatMap(_.toBatch).head.magnification, Some(2))

  }

  test("Adding a layer with an existing key merges magnification down (none, some)") {

    val scene =
      SceneUpdateFragment.empty.addLayer(BindingKey("key A") -> Layer.empty)

    val actual =
      scene.addLayer(BindingKey("key A") -> Layer.empty.withMagnification(1))

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.flatMap(_.toBatch).head.magnification, Some(1))

  }

  test("Adding a layer with an existing key merges magnification down (some, none)") {

    val scene =
      SceneUpdateFragment.empty.addLayer(BindingKey("key A") -> Layer.empty.withMagnification(2))

    val actual =
      scene.addLayer(BindingKey("key A") -> Layer.empty)

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.flatMap(_.toBatch).head.magnification, Some(2))

  }

  test("Replace layers using withLayers") {

    val scene =
      SceneUpdateFragment.empty.addLayer(BindingKey("key A") -> Layer.empty)

    val actual =
      scene.withLayers(BindingKey("key B") -> Layer.empty)

    assert(actual.layers.length == 1)

    actual.layers.head match
      case LayerEntry.Untagged(_) => fail("Should have been a tagged layer entry")
      case LayerEntry.Tagged(key, _) =>
        assertEquals(key, BindingKey("key B"))

  }

  test("SUF append preseves layer keys") {

    val sceneA: SceneUpdateFragment =
      SceneUpdateFragment.empty.addLayer(BindingKey("key A") -> Layer.empty.withMagnification(2))

    val sceneB: SceneUpdateFragment =
      SceneUpdateFragment.empty.addLayer(BindingKey("key A") -> Layer.empty.withMagnification(3))

    val actual: SceneUpdateFragment =
      sceneA |+| sceneB

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.flatMap(_.toBatch).head.magnification, Some(2))

  }

  test("Can add a blend material with no Blending instance in place") {
    val scene =
      SceneUpdateFragment.empty.withBlendMaterial(BlendMaterial.Lighting(RGBA.Red))

    scene.blendMaterial match
      case Some(BlendMaterial.Lighting(color)) =>
        assertEquals(color, RGBA.Red)

      case _ =>
        fail("match failed")
  }

  test("Can modify blending with no Blending instance in place") {
    val scene =
      SceneUpdateFragment.empty.modifyBlendMaterial(_ => BlendMaterial.Lighting(RGBA.Red))

    scene.blendMaterial match
      case Some(BlendMaterial.Lighting(color)) =>
        assertEquals(color, RGBA.Red)

      case _ =>
        fail("match failed")
  }

  test("Map over layers") {
    val scene =
      SceneUpdateFragment.empty
        .addLayer(BindingKey("key A") -> Layer.empty.withMagnification(1))
        .addLayer(BindingKey("key B") -> Layer.empty.withMagnification(1))

    val actual =
      scene.mapLayers {
        case LayerEntry.Untagged(_) =>
          fail("Should have been a tagged layer entry")

        case l @ LayerEntry.Tagged(key, layer) =>
          l.withTag(BindingKey(key.toString + "?")).withMagnificationForAll(2)
      }

    assert(actual.layers.flatMap(_.toBatch).length == 2)

    assertEquals(
      actual.layers.map(_.giveTag.get).toList,
      List(BindingKey("key A?"), BindingKey("key B?"))
    )
    assertEquals(actual.layers.flatMap(_.toBatch).map(_.magnification.get).toList, List(2, 2))
  }

  test("Merging SUF's with layer stacks") {

    val sceneA =
      SceneUpdateFragment.empty
        .addLayers(
          BindingKey("a") -> Layer.Content.empty,
          BindingKey("b") -> Layer.Stack(
            Layer.Content.empty,
            Layer.Stack(
              Layer.Content.empty
            ),
            Layer.Content.empty
          ),
          BindingKey("c") -> Layer.Content.empty
        )

    val sceneB =
      SceneUpdateFragment.empty
        .addLayers(
          BindingKey("a") -> Layer.Content.empty,
          BindingKey("b") -> Layer.Content.empty,
          BindingKey("c") -> Layer.Stack(
            Layer.Content.empty
          )
        )
        .addLayer(Layer.empty)

    val actual =
      sceneA |+| sceneB

    val expected =
      SceneUpdateFragment.empty
        .addLayers(
          BindingKey("a") -> Layer.Content.empty,
          BindingKey("b") -> Layer.Stack(
            Layer.Content.empty,
            Layer.Stack(
              Layer.Content.empty
            ),
            Layer.Content.empty,
            Layer.Content.empty
          ),
          BindingKey("c") -> Layer.Stack(
            Layer.Content.empty,
            Layer.Content.empty
          )
        )
        .addLayer(Layer.empty)

    assertEquals(actual, expected)

    val actualBatch =
      actual.layers.flatMap(_.toBatch)

    val expectedBatch =
      Batch(
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty
      )

    assertEquals(actualBatch.length, expectedBatch.length)
    assertEquals(actualBatch, expectedBatch)

  }

}
