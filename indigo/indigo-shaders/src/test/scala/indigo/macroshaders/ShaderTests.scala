package indigo.macroshaders

import ShaderDSL.*
import ShaderDSL.glsl.*
import ShaderMacros.*

class ShaderTests extends munit.FunSuite {

  test("build an Indigo compatible fragment shader") {
    val expected: String =
      """
      |//<indigo-fragment>
      |layout (std140) uniform CustomData {
      |  float ALPHA;
      |  vec3 BORDER_COLOR;
      |};
      |
      |float sdf(vec2 p) {
      |  float b = 0.45;
      |  vec2 d = abs(p) - b;
      |  float dist = length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
      |  return step(0.0, dist);
      |}
      |
      |void fragment() {
      |  float amount = sdf(UV - 0.5);
      |  COLOR = vec4(BORDER_COLOR * amount, amount) * ALPHA;
      |}
      |//</indigo-fragment>
      |""".stripMargin

    // Constants must be inline'd
    inline def outColor = rgba(1.0f, 1.0f, 0.0f, 1.0f)

    val actual =
      ShaderMacros.toGLSL(IndigoFrag(_ => outColor))

    assertEquals(actual, expected)
  }

}
