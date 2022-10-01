package indigo.shaders

class ShaderASTTests extends munit.FunSuite {

  test("test") {

    /*
precision mediump float;

uniform sampler2D u_texture;
uniform vec4 u_tint;

varying vec2 v_texcoord;
varying float v_alpha;

void main(void) {
vec4 textureColor = texture2D(u_texture, v_texcoord);

vec4 withAlpha = vec4(textureColor.r, textureColor.g, textureColor.b, textureColor.a * v_alpha);

vec4 tintedVersion = vec4(withAlpha.r * u_tint.r, withAlpha.g * u_tint.g, withAlpha.b * u_tint.b, withAlpha.a);

gl_FragColor = mix(withAlpha, tintedVersion, max(0.0, u_tint.a));
}
     */

    val frag =
      Statement(Declare.Precision.Medium) +
        Statement(Declare.Uniform[ShaderType.Sampler2D]("u_texture")) +
        Statement(Declare.Uniform[ShaderType.Vec4]("u_tint")) +
        Statement(Declare.Varying[ShaderType.Vec2]("v_texcoord")) +
        Statement(Declare.Varying[ShaderType.Float]("v_alpha")) +
        Statement(
          ShaderFunction[ShaderType.Void]("main")(
            Statement(Declare.Variable[ShaderType.Vec4]("textureColor")) +
              Statement(Declare.Variable[ShaderType.Vec4]("withAlpha")) +
              Statement(Declare.Variable[ShaderType.Vec4]("tintedVersion")) +
              Statement(
                Assign[ShaderType.Vec4](
                  "textureColor",
                  texture2D(ShaderRef("u_texture"), ShaderRef("v_texcoord"))
                )
              ) +
              Statement(
                Assign[ShaderType.Vec4](
                  "withAlpha", {
                    val textureColor: ShaderRef[ShaderType.Vec4] = ShaderRef("textureColor")
                    val vAlpha: ShaderRef[ShaderType.Float]      = ShaderRef("v_alpha")
                    vec4(textureColor.r, textureColor.g, textureColor.b, textureColor.a * vAlpha)
                  }
                )
              ) +
              Statement(
                Assign[ShaderType.Vec4](
                  "tintedVersion", {
                    val withAlpha: ShaderRef[ShaderType.Vec4] = ShaderRef("withAlpha")
                    val uTint: ShaderRef[ShaderType.Vec4]     = ShaderRef("u_tint")
                    vec4(withAlpha.r * uTint.r, withAlpha.g * uTint.g, withAlpha.b * uTint.b, withAlpha.a)
                  }
                )
              ) +
              Statement(
                Assign[ShaderType.Vec4](
                  "gl_FragColor",
                  mix(
                    ShaderRef("withAlpha"),
                    ShaderRef("tintedVersion"),
                    max(Literal(0), ShaderRef[ShaderType.Vec4]("u_tint").a)
                  )
                )
              )
          )
        )

    val out: String = frag.asGLSL

    println(out)

    assert(out.contains("precision mediump float;"))

  }

}
