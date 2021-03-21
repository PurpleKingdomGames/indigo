package indigoextras.materials

import indigo.shared.assets.AssetName
import indigo.shared.materials.Material
import indigo.shared.materials.ShaderData
import indigoextras.shaders.ExtrasShaderLibrary
import indigo.shared.shader.ShaderPrimitive.float
import indigo.shared.scenegraph.Blending
import indigo.shared.scenegraph.Blend
import indigo.shared.materials.BlendMaterial
import indigo.shared.datatypes.RGBA
import indigo.shared.materials.BlendShaderData
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.Uniform
import indigo.shared.shader.EntityShader
import indigo.shaders.ShaderLibrary
import indigo.shared.shader.BlendShader
import indigo.shared.shader.ShaderId
import indigo.shared.shader.Shader

object Refraction {

  val NormalMinusBlueShader: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigoextras_engine_normal_minus_blue]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ExtrasShaderLibrary.NormalMinusBlueFragment,
      light = ShaderLibrary.NoOpLight
    )

  val RefractionBlendShader: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigoextras_engine_blend_refraction]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ExtrasShaderLibrary.RefractionBlendFragment
    )

  val shaders: Set[Shader] =
    Set(NormalMinusBlueShader, RefractionBlendShader)

  final case class RefractionEntity(diffuse: AssetName) extends Material {
    def toShaderData: ShaderData =
      ShaderData(
        NormalMinusBlueShader.id,
        None,
        Some(diffuse),
        None,
        None,
        None
      )
  }

  final case class RefractionBlend(multiplier: Double) extends BlendMaterial {
    def toShaderData: BlendShaderData =
      BlendShaderData(
        RefractionBlendShader.id,
        Some(
          UniformBlock(
            "IndigoRefractionBlendData",
            List(
              Uniform("REFRACTION_AMOUNT") -> float(multiplier)
            )
          )
        )
      )
  }

  /**
    * Replicates Indigo's original refraction/distortion layer behaviour
    *
    * The problem with this method is that we have no "entity blend shader"
    * capability to allow use to control how individual entities blend onto
    * the layer below. As a result we have to use the same sort of mechanism
    * we use for lighting to combine the entities - but this results in a
    * weaker effect than we would like.
    * 
    * @param distance Max distance in pixels
    */
  def RefractionBlending(distance: Double): Blending =
    Blending(Blend.Normal, Blend.Normal, RefractionBlend(distance), Option(RGBA.Zero))

}
