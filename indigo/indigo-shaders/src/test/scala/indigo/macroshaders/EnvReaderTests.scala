package indigo.macroshaders

import ShaderDSL.*

class EnvReaderTests extends munit.FunSuite {

  test("Read UBO (case class)") {

    case class FragEnv(
        alpha: Float,
        count: Int,
        UV: vec2,
        pos: vec3,
        COLOR: vec4
    )

    val actual =
      EnvReader.readUBO[FragEnv]

    val expected =
      List(
        EnvReader.UBOField("alpha", "float"),
        EnvReader.UBOField("count", "int"),
        EnvReader.UBOField("UV", "vec2"),
        EnvReader.UBOField("pos", "vec3"),
        EnvReader.UBOField("COLOR", "vec4")
      )

    assertEquals(actual, expected)
  }

}
