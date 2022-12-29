package indigoextras.effectmaterials

import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.datatypes.RGBA
import indigo.shared.materials.BlendMaterial
import indigo.shared.materials.BlendShaderData
import indigo.shared.materials.FillType
import indigo.shared.materials.Material
import indigo.shared.materials.ShaderData
import indigo.shared.scenegraph.Blend
import indigo.shared.scenegraph.Blending
import indigo.shared.shader.BlendShader
import indigo.shared.shader.EntityShader
import indigo.shared.shader.Shader
import indigo.shared.shader.ShaderId
import indigo.shared.shader.ShaderPrimitive.float
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.library.NoOp
import indigoextras.effectmaterials.shaders.RefractionShaders

object Refraction:

  val entityShader: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigoextras_engine_normal_minus_blue]"),
      vertex = NoOp.vertex.output.code,
      fragment = RefractionShaders.normalMinusBlue.output.code,
      prepare = NoOp.prepare.output.code,
      light = NoOp.light.output.code,
      composite = NoOp.composite.output.code
    )

  val blendShader: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigoextras_engine_blend_refraction]"),
      vertex = NoOp.vertex.output.code,
      fragment = RefractionShaders.refractionFragment.output.code
    )

  val shaders: Set[Shader] =
    Set(entityShader, blendShader)

  /** Replicates Indigo's original refraction/distortion layer behaviour
    *
    * The problem with this method is that we have no "entity blend shader" capability to allow use to control how
    * individual entities blend onto the layer below. As a result we have to use the same sort of mechanism we use for
    * lighting to combine the entities - but this results in a weaker effect than we would like.
    *
    * @param distance
    *   Max distance in pixels
    */
  def blending(distance: Double): Blending =
    Blending(Blend.Normal, Blend.Normal, RefractionBlend(distance), Option(RGBA.Zero))

final case class RefractionEntity(diffuse: AssetName, fillType: FillType) extends Material derives CanEqual:

  def withDiffuse(newDiffuse: AssetName): RefractionEntity =
    this.copy(diffuse = newDiffuse)

  def withFillType(newFillType: FillType): RefractionEntity =
    this.copy(fillType = newFillType)
  def normal: RefractionEntity =
    withFillType(FillType.Normal)
  def stretch: RefractionEntity =
    withFillType(FillType.Stretch)
  def tile: RefractionEntity =
    withFillType(FillType.Tile)

  lazy val toShaderData: ShaderData = {
    val imageFillType: Double =
      fillType match {
        case FillType.Normal  => 0.0
        case FillType.Stretch => 1.0
        case FillType.Tile    => 2.0
      }

    val uniformBlock: UniformBlock =
      UniformBlock(
        "IndigoBitmapData",
        Batch(
          Uniform("FILLTYPE") -> float(imageFillType)
        )
      )

    ShaderData(
      Refraction.entityShader.id,
      Batch(uniformBlock),
      Some(diffuse),
      None,
      None,
      None
    )
  }

object RefractionEntity:
  def apply(diffuse: AssetName): RefractionEntity =
    RefractionEntity(diffuse, FillType.Normal)

final case class RefractionBlend(multiplier: Double) extends BlendMaterial derives CanEqual:
  lazy val toShaderData: BlendShaderData =
    BlendShaderData(
      Refraction.blendShader.id,
      Batch(
        UniformBlock(
          "IndigoRefractionBlendData",
          Batch(
            Uniform("REFRACTION_AMOUNT") -> float(multiplier)
          )
        )
      )
    )
