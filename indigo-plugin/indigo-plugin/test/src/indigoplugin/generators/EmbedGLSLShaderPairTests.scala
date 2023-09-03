package indigoplugin.generators

class EmbedGLSLShaderPairTests extends munit.FunSuite {

  test("sanitiseName") {
    assertEquals(EmbedGLSLShaderPair.sanitiseName("Ha_ erm!What?...12!.vert", "vert"), "HaErmWhat12")
  }

  test("template") {
    val actual =
      EmbedGLSLShaderPair.template("MyModule", "org.example", "// code goes here.")

    val expected =
      s"""package org.example
      |
      |object MyModule {
      |
      |// code goes here.
      |
      |}
      """.stripMargin

    assertEquals(actual.trim, expected.trim)
  }

  test("extractShaderCode - vertex") {
    val actual =
      EmbedGLSLShaderPair.extractShaderCode(sampleVertexProgram, "vertex", "MyShader")

    val expected =
      List(
        EmbedGLSLShaderPair.ShaderSnippet(
          "MyShaderVertex",
          "void vertex(){}"
        )
      )

    assertEquals(actual, expected)
  }

  test("extractShaderCode - fragment") {
    val actual =
      EmbedGLSLShaderPair.extractShaderCode(sampleFragmentProgram, "fragment", "MyShader")

    val expected =
      List(
        EmbedGLSLShaderPair.ShaderSnippet(
          "MyShaderFragment",
          """
layout (std140) uniform IndigoBitmapData {
  highp float FILLTYPE;
};

void fragment(){

  // 0 = normal; 1 = stretch; 2 = tile
  int fillType = int(round(FILLTYPE));
  vec4 textureColor;

  switch(fillType) {
    case 0:
      textureColor = CHANNEL_0;
      break;

    case 1:
      vec2 stretchedUVs = CHANNEL_0_POSITION + UV * CHANNEL_0_SIZE;
      textureColor = texture(SRC_CHANNEL, stretchedUVs);
      break;

    case 2:
      vec2 tiledUVs = CHANNEL_0_POSITION + (fract(UV * (SIZE / TEXTURE_SIZE)) * CHANNEL_0_SIZE);
      textureColor = texture(SRC_CHANNEL, tiledUVs);
      break;

    default:
      textureColor = CHANNEL_0;
      break;
  }

  COLOR = textureColor;
}
          """.trim
        )
      )

    assertEquals(actual, expected)
  }

  val sampleVertexProgram: String =
    """
//<indigo-vertex>
void vertex(){}
//</indigo-vertex>
    """

  val sampleFragmentProgram: String =
    """
#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;

vec4 CHANNEL_0;
vec4 COLOR;
vec2 UV;
vec2 TEXTURE_SIZE;
vec2 SIZE;
vec2 CHANNEL_0_POSITION;
vec2 CHANNEL_0_SIZE;

//<indigo-fragment>
layout (std140) uniform IndigoBitmapData {
  highp float FILLTYPE;
};

void fragment(){

  // 0 = normal; 1 = stretch; 2 = tile
  int fillType = int(round(FILLTYPE));
  vec4 textureColor;

  switch(fillType) {
    case 0:
      textureColor = CHANNEL_0;
      break;

    case 1:
      vec2 stretchedUVs = CHANNEL_0_POSITION + UV * CHANNEL_0_SIZE;
      textureColor = texture(SRC_CHANNEL, stretchedUVs);
      break;

    case 2:
      vec2 tiledUVs = CHANNEL_0_POSITION + (fract(UV * (SIZE / TEXTURE_SIZE)) * CHANNEL_0_SIZE);
      textureColor = texture(SRC_CHANNEL, tiledUVs);
      break;

    default:
      textureColor = CHANNEL_0;
      break;
  }

  COLOR = textureColor;
}
//</indigo-fragment>
    """

}
