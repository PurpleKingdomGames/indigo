package indigo.shared.materials

import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.RGBA
import indigo.shared.materials.LightingModel.Lit
import indigo.shared.materials.LightingModel.Unlit
import indigo.shared.shader.ShaderId
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.shader.ShaderPrimitive.rawJSArray
import indigo.shared.shader.StandardShaders
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock

trait Material {
  def toShaderData: ShaderData
}
object Material {

  final case class Bitmap(diffuse: AssetName, lighting: LightingModel, shaderId: Option[ShaderId], fillType: FillType)
      extends Material derives CanEqual {

    def withDiffuse(newDiffuse: AssetName): Bitmap =
      this.copy(diffuse = newDiffuse)

    def withLighting(newLighting: LightingModel): Bitmap =
      this.copy(lighting = newLighting)
    def modifyLighting(modifier: LightingModel => LightingModel): Bitmap =
      this.copy(lighting = modifier(lighting))

    def withShaderId(newShaderId: ShaderId): Bitmap =
      this.copy(shaderId = Option(newShaderId))

    def withFillType(newFillType: FillType): Bitmap =
      this.copy(fillType = newFillType)
    def normal: Bitmap =
      withFillType(FillType.Normal)
    def stretch: Bitmap =
      withFillType(FillType.Stretch)
    def tile: Bitmap =
      withFillType(FillType.Tile)

    def toImageEffects: Material.ImageEffects =
      Material.ImageEffects(diffuse, lighting, shaderId)

    lazy val toShaderData: ShaderData = {

      val imageFillType: Float =
        fillType match {
          case FillType.Normal  => 0.0
          case FillType.Stretch => 1.0
          case FillType.Tile    => 2.0
        }

      val uniformBlock: UniformBlock =
        UniformBlock(
          "IndigoBitmapData",
          List(
            Uniform("Bitmap_FILLTYPE") -> rawJSArray(scalajs.js.Array(imageFillType))
          )
        )

      lighting match {
        case Unlit =>
          ShaderData(
            shaderId.getOrElse(StandardShaders.Bitmap.id),
            List(uniformBlock),
            Some(diffuse),
            None,
            None,
            None
          )

        case l: Lit =>
          l.toShaderData(shaderId.getOrElse(StandardShaders.LitBitmap.id), Some(diffuse), List(uniformBlock))
      }
    }
  }
  object Bitmap {
    def apply(diffuse: AssetName): Bitmap =
      Bitmap(diffuse, LightingModel.Unlit, None, FillType.Normal)

    def apply(diffuse: AssetName, lighting: LightingModel): Bitmap =
      Bitmap(diffuse, lighting, None, FillType.Normal)
  }

  final case class ImageEffects(
      diffuse: AssetName,
      alpha: Double,
      tint: RGBA,
      overlay: Fill,
      saturation: Double,
      lighting: LightingModel,
      shaderId: Option[ShaderId],
      fillType: FillType
  ) extends Material
      derives CanEqual {

    def withDiffuse(newDiffuse: AssetName): ImageEffects =
      this.copy(diffuse = newDiffuse)

    def withAlpha(newAlpha: Double): ImageEffects =
      this.copy(alpha = newAlpha)

    def withTint(newTint: RGBA): ImageEffects =
      this.copy(tint = newTint)
    def withTint(newTint: RGB): ImageEffects =
      this.copy(tint = newTint.toRGBA)

    def withOverlay(newOverlay: Fill): ImageEffects =
      this.copy(overlay = newOverlay)

    def withSaturation(newSaturation: Double): ImageEffects =
      this.copy(saturation = newSaturation)

    def withLighting(newLighting: LightingModel): ImageEffects =
      this.copy(lighting = newLighting)
    def modifyLighting(modifier: LightingModel => LightingModel): ImageEffects =
      this.copy(lighting = modifier(lighting))

    def withShaderId(newShaderId: ShaderId): ImageEffects =
      this.copy(shaderId = Option(newShaderId))

    def withFillType(newFillType: FillType): ImageEffects =
      this.copy(fillType = newFillType)
    def normal: ImageEffects =
      withFillType(FillType.Normal)
    def stretch: ImageEffects =
      withFillType(FillType.Stretch)
    def tile: ImageEffects =
      withFillType(FillType.Tile)

    def toBitmap: Material.Bitmap =
      Material.Bitmap(diffuse, lighting, shaderId, fillType)

    lazy val toShaderData: ShaderData = {
      val overlayType: Float =
        overlay match {
          case _: Fill.Color          => 0.0
          case _: Fill.LinearGradient => 1.0
          case _: Fill.RadialGradient => 2.0
        }

      val imageFillType: Float =
        fillType match {
          case FillType.Normal  => 0.0
          case FillType.Stretch => 1.0
          case FillType.Tile    => 2.0
        }

      // ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE (vec4), TINT (vec4)
      val effectsUniformBlock: UniformBlock =
        UniformBlock(
          "IndigoImageEffectsData",
          List(
            Uniform("ImageEffects_DATA") -> rawJSArray(
              scalajs.js.Array(
                alpha.toFloat,
                saturation.toFloat,
                overlayType,
                imageFillType,
                tint.r.toFloat,
                tint.g.toFloat,
                tint.b.toFloat,
                tint.a.toFloat
              )
            )
          ) ++ overlay.toUniformData("ImageEffects")
        )

      lighting match {
        case Unlit =>
          ShaderData(
            shaderId.getOrElse(StandardShaders.ImageEffects.id),
            List(effectsUniformBlock),
            Some(diffuse),
            None,
            None,
            None
          )

        case l: Lit =>
          l.toShaderData(
            shaderId.getOrElse(StandardShaders.LitImageEffects.id),
            Some(diffuse),
            List(effectsUniformBlock)
          )
      }
    }
  }
  object ImageEffects {
    def apply(diffuse: AssetName): ImageEffects =
      ImageEffects(diffuse, 1.0, RGBA.None, Fill.Color.default, 1.0, LightingModel.Unlit, None, FillType.Normal)

    def apply(diffuse: AssetName, alpha: Double): ImageEffects =
      ImageEffects(diffuse, alpha, RGBA.None, Fill.Color.default, 1.0, LightingModel.Unlit, None, FillType.Normal)

    def apply(diffuse: AssetName, lighting: LightingModel): ImageEffects =
      ImageEffects(diffuse, 1.0, RGBA.None, Fill.Color.default, 1.0, lighting, None, FillType.Normal)

    def apply(diffuse: AssetName, lighting: LightingModel, shaderId: Option[ShaderId]): ImageEffects =
      ImageEffects(diffuse, 1.0, RGBA.None, Fill.Color.default, 1.0, lighting, shaderId, FillType.Normal)
  }

}

enum FillType derives CanEqual:
  case Normal, Stretch, Tile
