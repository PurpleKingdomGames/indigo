package indigo.shared.shader

/** The point of these tests is purely to exercise the process of compiling and validating the standard shaders. If we
  * get no exceptions we're at least in basically good shape, i.e. They may not do as intended but there's nothing
  * structurally wrong with them, like a stray forward reference for example.
  */
class StandardShadersTests extends munit.FunSuite {

  test("Bitmap is valid") {
    assert(StandardShaders.Bitmap.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.Bitmap.fragment.toOutput.code.nonEmpty)
  }

  test("LitBitmap is valid") {
    assert(StandardShaders.LitBitmap.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.LitBitmap.fragment.toOutput.code.nonEmpty)
  }

  test("ImageEffects is valid") {
    assert(StandardShaders.ImageEffects.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.ImageEffects.fragment.toOutput.code.nonEmpty)
  }

  test("LitImageEffects is valid") {
    assert(StandardShaders.LitImageEffects.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.LitImageEffects.fragment.toOutput.code.nonEmpty)
  }

  test("BitmapClip is valid") {
    assert(StandardShaders.BitmapClip.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.BitmapClip.fragment.toOutput.code.nonEmpty)
  }

  test("LitBitmapClip is valid") {
    assert(StandardShaders.LitBitmapClip.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.LitBitmapClip.fragment.toOutput.code.nonEmpty)
  }

  test("ImageEffectsClip is valid") {
    assert(StandardShaders.ImageEffectsClip.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.ImageEffectsClip.fragment.toOutput.code.nonEmpty)
  }

  test("LitImageEffectsClip is valid") {
    assert(StandardShaders.LitImageEffectsClip.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.LitImageEffectsClip.fragment.toOutput.code.nonEmpty)
  }

  test("ShapeBox is valid") {
    assert(StandardShaders.ShapeBox.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.ShapeBox.fragment.toOutput.code.nonEmpty)
  }

  test("LitShapeBox is valid") {
    assert(StandardShaders.LitShapeBox.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.LitShapeBox.fragment.toOutput.code.nonEmpty)
  }

  test("ShapeCircle is valid") {
    assert(StandardShaders.ShapeCircle.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.ShapeCircle.fragment.toOutput.code.nonEmpty)
  }

  test("LitShapeCircle is valid") {
    assert(StandardShaders.LitShapeCircle.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.LitShapeCircle.fragment.toOutput.code.nonEmpty)
  }

  test("ShapeLine is valid") {
    assert(StandardShaders.ShapeLine.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.ShapeLine.fragment.toOutput.code.nonEmpty)
  }

  test("LitShapeLine is valid") {
    assert(StandardShaders.LitShapeLine.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.LitShapeLine.fragment.toOutput.code.nonEmpty)
  }

  test("ShapePolygon is valid") {
    assert(StandardShaders.ShapePolygon.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.ShapePolygon.fragment.toOutput.code.nonEmpty)
  }

  test("LitShapePolygon is valid") {
    assert(StandardShaders.LitShapePolygon.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.LitShapePolygon.fragment.toOutput.code.nonEmpty)
  }

  test("NormalBlend is valid") {
    assert(StandardShaders.NormalBlend.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.NormalBlend.fragment.toOutput.code.nonEmpty)
  }

  test("LightingBlend is valid") {
    assert(StandardShaders.LightingBlend.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.LightingBlend.fragment.toOutput.code.nonEmpty)
  }

  test("BlendEffects is valid") {
    assert(StandardShaders.BlendEffects.vertex.toOutput.code.nonEmpty)
    assert(StandardShaders.BlendEffects.fragment.toOutput.code.nonEmpty)
  }
}
