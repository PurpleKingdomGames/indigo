package indigoextras.effectmaterials

import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle
import indigo.shared.materials.FillType
import indigo.shared.materials.Material
import indigo.shared.shader.EntityShader
import indigo.shared.shader.ShaderData
import indigo.shared.shader.ShaderId
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.shader.ShaderPrimitive.rawJSArray
import indigo.shared.shader.UltravioletShader
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.UniformBlockName
import indigo.shared.shader.library.IndigoUV.VertexEnv
import indigoextras.effectmaterials.shaders.LegacyEffectsShaders

final case class LegacyEffects(
    diffuse: AssetName,
    alpha: Double,
    tint: RGBA,
    overlay: Fill,
    saturation: Double,
    border: Border,
    glow: Glow,
    fillType: FillType
) extends Material derives CanEqual:

  def withAlpha(newAlpha: Double): LegacyEffects =
    this.copy(alpha = newAlpha)

  def withTint(newTint: RGBA): LegacyEffects =
    this.copy(tint = newTint)
  def withTint(newTint: RGB): LegacyEffects =
    this.copy(tint = newTint.toRGBA)

  def withOverlay(newOverlay: Fill): LegacyEffects =
    this.copy(overlay = newOverlay)

  def withSaturation(newSaturation: Double): LegacyEffects =
    this.copy(saturation = newSaturation)

  def withBorder(newBorder: Border): LegacyEffects =
    this.copy(border = newBorder)

  def withGlow(newGlow: Glow): LegacyEffects =
    this.copy(glow = newGlow)

  def withFillType(newFillType: FillType): LegacyEffects =
    this.copy(fillType = newFillType)
  def normal: LegacyEffects =
    withFillType(FillType.Normal)
  def stretch: LegacyEffects =
    withFillType(FillType.Stretch)
  def tile: LegacyEffects =
    withFillType(FillType.Tile)
  def nineSlice(center: Rectangle): LegacyEffects =
    withFillType(FillType.NineSlice(center))
  def nineSlice(top: Int, right: Int, bottom: Int, left: Int): LegacyEffects =
    withFillType(FillType.NineSlice(top, right, bottom, left))

  lazy val toShaderData: ShaderData =
    val overlayType: Float =
      overlay match
        case _: Fill.Color          => 0.0
        case _: Fill.LinearGradient => 1.0
        case _: Fill.RadialGradient => 2.0

    val imageFillType: Float =
      fillType match
        case FillType.Normal       => 0.0
        case FillType.Stretch      => 1.0
        case FillType.Tile         => 2.0
        case FillType.NineSlice(_) => 3.0

    val nineSliceCenter: scalajs.js.Array[Float] =
      fillType match
        case FillType.NineSlice(center) =>
          scalajs.js.Array(
            center.x.toFloat,
            center.y.toFloat,
            center.width.toFloat,
            center.height.toFloat
          )

        case _ =>
          scalajs.js.Array(0.0f, 0.0f, 0.0f, 0.0f)

    ShaderData(
      LegacyEffects.entityShader.id,
      Batch(
        UniformBlock(
          UniformBlockName("IndigoLegacyEffectsData"),
          Batch(
            // ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE (vec4), TINT (vec4)
            Uniform("LegacyEffects_DATA") -> rawJSArray(
              scalajs.js.Array(
                alpha.toFloat,
                saturation.toFloat,
                overlayType,
                imageFillType
              ) ++
                nineSliceCenter ++
                scalajs.js.Array(
                  tint.r.toFloat,
                  tint.g.toFloat,
                  tint.b.toFloat,
                  tint.a.toFloat
                )
            )
          ) ++ overlay.toUniformData("LegacyEffects") ++
            // BORDER_COLOR (vec4), GLOW_COLOR (vec4), EFFECT_AMOUNTS (vec4)
            Batch(
              Uniform("LegacyEffects_EFFECTS") -> rawJSArray(
                scalajs.js.Array(
                  border.color.r.toFloat,
                  border.color.g.toFloat,
                  border.color.b.toFloat,
                  border.color.a.toFloat,
                  glow.color.r.toFloat,
                  glow.color.g.toFloat,
                  glow.color.b.toFloat,
                  glow.color.a.toFloat,
                  border.outerThickness.toInt.toFloat,
                  border.innerThickness.toInt.toFloat,
                  glow.outerGlowAmount.toFloat,
                  glow.innerGlowAmount.toFloat
                )
              )
            )
        )
      ),
      Some(diffuse),
      None,
      None,
      None
    )

object LegacyEffects:

  val entityShader: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigoextras_engine_legacy_effects]"),
      EntityShader.vertex(LegacyEffectsShaders.vertex, VertexEnv.reference),
      EntityShader.fragment(
        LegacyEffectsShaders.fragment,
        LegacyEffectsShaders.Env.reference
      )
    )

  def apply(diffuse: AssetName): LegacyEffects =
    LegacyEffects(diffuse, 1.0, RGBA.None, Fill.Color.default, 1.0, Border.default, Glow.default, FillType.Normal)

  def apply(diffuse: AssetName, alpha: Double): LegacyEffects =
    LegacyEffects(diffuse, alpha, RGBA.None, Fill.Color.default, 1.0, Border.default, Glow.default, FillType.Normal)

final case class Border(color: RGBA, innerThickness: Thickness, outerThickness: Thickness) derives CanEqual:

  def withColor(newColor: RGBA): Border =
    this.copy(color = newColor)

  def withInnerThickness(thickness: Thickness): Border =
    this.copy(innerThickness = thickness)

  def withOuterThickness(thickness: Thickness): Border =
    this.copy(outerThickness = thickness)

object Border:
  def inside(color: RGBA): Border =
    Border(color, Thickness.Thin, Thickness.None)

  def outside(color: RGBA): Border =
    Border(color, Thickness.None, Thickness.Thin)

  val default: Border =
    Border(RGBA.Zero, Thickness.None, Thickness.None)

enum Thickness derives CanEqual:
  case None, Thin, Thick

object Thickness:
  extension (t: Thickness)
    def toInt: Int =
      t match
        case Thickness.None  => 0
        case Thickness.Thin  => 1
        case Thickness.Thick => 2

final case class Glow(color: RGBA, innerGlowAmount: Double, outerGlowAmount: Double) derives CanEqual:
  def withColor(newColor: RGBA): Glow =
    this.copy(color = newColor)

  def withInnerGlowAmount(amount: Double): Glow =
    this.copy(innerGlowAmount = Math.max(0, amount))

  def withOuterGlowAmount(amount: Double): Glow =
    this.copy(outerGlowAmount = Math.max(0, amount))

object Glow:
  def inside(color: RGBA): Glow =
    Glow(color, 1d, 0d)

  def outside(color: RGBA): Glow =
    Glow(color, 0d, 1d)

  val default: Glow =
    Glow(RGBA.Zero, 0d, 0d)
