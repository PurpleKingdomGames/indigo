package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey
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

}
