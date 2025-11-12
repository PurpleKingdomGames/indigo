package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.datatypes.RGBA
import indigo.shared.materials.BlendMaterial

class LayerTests extends munit.FunSuite {

  test("Can add a blend material with no Blending instance in place") {
    val layer =
      Layer.empty.withBlendMaterial(BlendMaterial.Lighting(RGBA.Red))

    layer.blending match
      case Some(Blending(entity, layer, BlendMaterial.Lighting(color), clearColor)) =>
        assertEquals(color, RGBA.Red)

      case _ =>
        fail("match failed")
  }

  test("Can modify blending with no Blending instance in place") {
    val layer =
      Layer.empty.modifyBlending(_.withClearColor(RGBA.Green))

    layer.blending match
      case Some(Blending(entity, layer, blendMaterial, Some(clearColor))) =>
        assertEquals(clearColor, RGBA.Green)

      case _ =>
        fail("match failed")
  }

  test("Can add an entity blend mode with no Blending instance in place") {
    val layer =
      Layer.empty.withEntityBlend(Blend.LightingEntity)

    layer.blending match
      case Some(Blending(entity, layer, blendMaterial, clearColor)) =>
        assertEquals(entity, Blend.LightingEntity)

      case _ =>
        fail("match failed")
  }

  test("Can add a layer blend mode with no Blending instance in place") {
    val layer =
      Layer.empty.withLayerBlend(Blend.LightingEntity)

    layer.blending match
      case Some(Blending(entity, layer, blendMaterial, clearColor)) =>
        assertEquals(layer, Blend.LightingEntity)

      case _ =>
        fail("match failed")
  }

  test("Layer.Stack combine ops") {
    assertEquals(
      Layer.Stack(Layer.Content.empty).combine(Layer.Stack(Layer.Content.empty)),
      Layer.Stack(Layer.Content.empty, Layer.Content.empty)
    )
    assertEquals(
      Layer.Stack(Layer.Content.empty) ++ Layer.Stack(Layer.Content.empty),
      Layer.Stack(Layer.Content.empty, Layer.Content.empty)
    )
    assertEquals(
      Layer.Stack(Layer.Content.empty) ++ Batch(Layer.Content.empty),
      Layer.Stack(Layer.Content.empty, Layer.Content.empty)
    )
    assertEquals(
      Layer.Stack(Layer.Content.empty) :+ Layer.Content.empty,
      Layer.Stack(Layer.Content.empty, Layer.Content.empty)
    )
    assertEquals(
      Layer.Content.empty +: Layer.Stack(Layer.Content.empty),
      Layer.Stack(Layer.Content.empty, Layer.Content.empty)
    )
    assertEquals(
      Layer.Content.empty :: Layer.Stack(Layer.Content.empty),
      Layer.Stack(Layer.Content.empty, Layer.Content.empty)
    )
  }

  test("modify - Content layer") {
    val l = Layer.Content.empty.withMagnification(1)

    val actual   = l.modify { case l: Layer.Content => l.withMagnification(2) }
    val expected = Layer.Content.empty.withMagnification(2)

    assertEquals(actual, expected)
  }

  test("modify - Stack layer") {
    val l = Layer.Stack(Layer.Content.empty, Layer.Content.empty, Layer.Content.empty)

    val actual   = l.modify { case ll: Layer.Stack => Layer.Stack(ll.layers.take(1)) }
    val expected = Layer.Stack(Layer.Content.empty)

    assertEquals(actual, expected)
  }

  test("modify - perform a deep modification") {
    val l = Layer.Stack(
      Layer.Stack(
        Layer.Content.empty.withMagnification(1),
        Layer.Content.empty.withMagnification(1),
        Layer.Content.empty.withMagnification(1)
      ),
      Layer.Content.empty.withMagnification(1),
      Layer.Content.empty.withMagnification(1),
      Layer.Content.empty.withMagnification(1)
    )

    val actual = l.modify { case l: Layer.Content => l.withMagnification(2) }
    val expected = Layer.Stack(
      Layer.Stack(
        Layer.Content.empty.withMagnification(2),
        Layer.Content.empty.withMagnification(2),
        Layer.Content.empty.withMagnification(2)
      ),
      Layer.Content.empty.withMagnification(2),
      Layer.Content.empty.withMagnification(2),
      Layer.Content.empty.withMagnification(2)
    )

    assertEquals(actual, expected)
  }

  test("Layer.Stack cons") {
    val l = Layer.Stack(Layer.Content.empty, Layer.Content.empty, Layer.Content.empty)

    val actual = Layer.Content.empty.withMagnification(2) :: l

    assertEquals(actual.layers.length, 4)

    actual.layers.head match
      case Layer.Content(nodes, lights, magnification, visible, blending, cloneBlanks, camera) =>
        assertEquals(
          magnification,
          Some(2)
        )

      case _ =>
        fail("match failed")
  }

}
