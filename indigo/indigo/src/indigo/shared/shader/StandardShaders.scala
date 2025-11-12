package indigo.shared.shader

object StandardShaders {

  def all: Set[ShaderProgram] =
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

  lazy val Bitmap: UltravioletShader =
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

  lazy val LitBitmap: UltravioletShader =
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

  lazy val ImageEffects: UltravioletShader =
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

  lazy val LitImageEffects: UltravioletShader =
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

  lazy val BitmapClip: UltravioletShader          = makeClipShader(Bitmap)
  lazy val LitBitmapClip: UltravioletShader       = makeClipShader(LitBitmap)
  lazy val ImageEffectsClip: UltravioletShader    = makeClipShader(ImageEffects)
  lazy val LitImageEffectsClip: UltravioletShader = makeClipShader(LitImageEffects)

  // Shapes

  lazy val ShapeBox: UltravioletShader =
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

  lazy val LitShapeBox: UltravioletShader =
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

  lazy val ShapeCircle: UltravioletShader =
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

  lazy val LitShapeCircle: UltravioletShader =
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

  lazy val ShapeLine: UltravioletShader =
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

  lazy val LitShapeLine: UltravioletShader =
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

  lazy val ShapePolygon: UltravioletShader =
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

  lazy val LitShapePolygon: UltravioletShader =
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

  lazy val NormalBlend: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_blend_normal]"),
      BlendShader.vertex(library.NoOp.vertex, ()),
      BlendShader.fragment(
        library.NormalBlend.fragment,
        library.NormalBlend.Env.reference
      )
    )

  lazy val LightingBlend: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_blend_lighting]"),
      BlendShader.vertex(library.NoOp.vertex, ()),
      BlendShader.fragment(
        library.LightingBlend.fragment,
        library.LightingBlend.Env.reference
      )
    )

  lazy val BlendEffects: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_blend_effects]"),
      BlendShader.vertex(library.NoOp.vertex, ()),
      BlendShader.fragment(
        library.BlendEffects.fragment,
        library.BlendEffects.Env.reference
      )
    )

}
