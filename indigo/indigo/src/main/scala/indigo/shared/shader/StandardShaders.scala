package indigo.shared.shader

import indigo.shared.shader.library
object StandardShaders {

  def all: Set[Shader] =
    Set(
      Bitmap,
      LitBitmap,
      ImageEffects,
      LitImageEffects,
      BitmapClip,
      LitBitmapClip,
      ImageEffectsClip,
      LitImageEffectsClip,
      ShapeBox,
      LitShapeBox,
      ShapeCircle,
      LitShapeCircle,
      ShapeLine,
      LitShapeLine,
      ShapePolygon,
      LitShapePolygon,
      NormalBlend,
      LightingBlend,
      BlendEffects
    )

  // Entity Shaders

  val Bitmap: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_bitmap]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.Blit.fragment,
        library.NoOp.prepare,
        library.NoOp.light,
        library.NoOp.composite,
        library.Blit.Env.reference
      )
    )

  val LitBitmap: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_lit_bitmap]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.Blit.fragment,
        library.Lighting.prepare,
        library.Lighting.light,
        library.Lighting.composite,
        library.Blit.Env.reference
      )
    )

  val ImageEffects: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_image_effects]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ImageEffects.fragment,
        library.NoOp.prepare,
        library.NoOp.light,
        library.NoOp.composite,
        library.ImageEffects.Env.reference
      )
    )

  val LitImageEffects: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_lit_image_effects]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ImageEffects.fragment,
        library.Lighting.prepare,
        library.Lighting.light,
        library.Lighting.composite,
        library.ImageEffects.Env.reference
      )
    )

  // Clips

  def shaderIdToClipShaderId(id: ShaderId): ShaderId =
    ShaderId(id.toString + "[clip]")

  def makeClipShader(shader: UltravioletShader): UltravioletShader =
    shader.copy(
      id = shaderIdToClipShaderId(shader.id),
      vertex = EntityShader.vertex(
        library.Clip.vertex,
        library.Clip.Env.reference
      )
    )

  val BitmapClip: UltravioletShader          = makeClipShader(Bitmap)
  val LitBitmapClip: UltravioletShader       = makeClipShader(LitBitmap)
  val ImageEffectsClip: UltravioletShader    = makeClipShader(ImageEffects)
  val LitImageEffectsClip: UltravioletShader = makeClipShader(LitImageEffects)

  // Shapes

  val ShapeBox: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_shape_box]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ShapeBox.fragment,
        library.NoOp.prepare,
        library.NoOp.light,
        library.NoOp.composite,
        library.ShapeBox.Env.reference
      )
    )

  val LitShapeBox: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_lit_shape_box]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ShapeBox.fragment,
        library.Lighting.prepare,
        library.Lighting.light,
        library.Lighting.composite,
        library.ShapeBox.Env.reference
      )
    )

  val ShapeCircle: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_shape_circle]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ShapeCircle.fragment,
        library.NoOp.prepare,
        library.NoOp.light,
        library.NoOp.composite,
        library.ShapeCircle.Env.reference
      )
    )

  val LitShapeCircle: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_lit_shape_circle]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ShapeCircle.fragment,
        library.Lighting.prepare,
        library.Lighting.light,
        library.Lighting.composite,
        library.ShapeCircle.Env.reference
      )
    )

  val ShapeLine: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_shape_line]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ShapeLine.fragment,
        library.NoOp.prepare,
        library.NoOp.light,
        library.NoOp.composite,
        library.ShapeLine.Env.reference
      )
    )

  val LitShapeLine: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_lit_shape_line]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ShapeLine.fragment,
        library.Lighting.prepare,
        library.Lighting.light,
        library.Lighting.composite,
        library.ShapeLine.Env.reference
      )
    )

  val ShapePolygon: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_shape_polygon]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ShapePolygon.fragment,
        library.NoOp.prepare,
        library.NoOp.light,
        library.NoOp.composite,
        library.ShapePolygon.Env.reference
      )
    )

  val LitShapePolygon: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_lit_shape_polygon]"),
      EntityShader.vertex(library.NoOp.vertex, ()),
      EntityShader.fragment(
        library.ShapePolygon.fragment,
        library.Lighting.prepare,
        library.Lighting.light,
        library.Lighting.composite,
        library.ShapePolygon.Env.reference
      )
    )

  // Blend Shaders

  val NormalBlend: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_blend_normal]"),
      BlendShader.vertex(library.NoOp.vertex, ()),
      BlendShader.fragment(
        library.NormalBlend.fragment,
        library.NormalBlend.Env.reference
      )
    )

  val LightingBlend: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_blend_lighting]"),
      BlendShader.vertex(library.NoOp.vertex, ()),
      BlendShader.fragment(
        library.LightingBlend.fragment,
        library.LightingBlend.Env.reference
      )
    )

  val BlendEffects: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_blend_effects]"),
      BlendShader.vertex(library.NoOp.vertex, ()),
      BlendShader.fragment(
        library.BlendEffects.fragment,
        library.BlendEffects.Env.reference
      )
    )

}
