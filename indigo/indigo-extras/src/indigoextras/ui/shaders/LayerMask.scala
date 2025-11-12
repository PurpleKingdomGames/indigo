package indigoextras.ui.shaders

import indigo.*
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.UniformBlockName
import ultraviolet.syntax.*

import scala.annotation.nowarn

final case class LayerMask(mask: Rectangle) extends BlendMaterial:
  lazy val toShaderData: ShaderData =
    ShaderData(
      LayerMask.shader.id,
      Batch(
        UniformBlock(
          UniformBlockName("MaskBounds"),
          Batch(
            Uniform("MASK_BOUNDS") -> ShaderPrimitive.vec4.fromRectangle(mask)
          )
        )
      )
    )

object LayerMask:
  val shader: UltravioletShader =
    UltravioletShader.blendFragment(
      ShaderId("[indigo]-ui-masked-layer"),
      BlendShader.fragment(
        fragment,
        Env.ref
      )
    )

  final case class Env(
      MASK_BOUNDS: vec4
  ) extends BlendFragmentEnvReference

  object Env:
    val ref =
      Env(
        vec4(1.0f)
      )

  final case class MaskBounds(
      MASK_BOUNDS: vec4
  )

  @nowarn("msg=unused")
  inline def fragment =
    Shader[Env] { env =>

      ubo[MaskBounds]

      def fragment(color: vec4): vec4 =
        val x = env.MASK_BOUNDS.x / env.SIZE.x
        val y = env.MASK_BOUNDS.y / env.SIZE.y
        val w = env.MASK_BOUNDS.z / env.SIZE.x
        val h = env.MASK_BOUNDS.w / env.SIZE.y

        if env.UV.x > x && env.UV.x < x + w && env.UV.y > y && env.UV.y < y + h then env.SRC
        else vec4(0.0f)
    }
