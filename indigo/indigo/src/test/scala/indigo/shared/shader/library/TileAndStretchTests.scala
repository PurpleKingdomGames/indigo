package indigo.shared.shader.library

import ultraviolet.syntax.*

class TileAndStretchTests extends munit.FunSuite {

  test("should correctly render tile code") {

    inline def fragment =
      Shader {
        import TileAndStretch.*

        val uv: vec2          = vec2(1.0)
        val channelPos: vec2  = vec2(2.0)
        val channelSize: vec2 = vec2(3.0)
        val entitySize: vec2  = vec2(4.0)
        val textureSize: vec2 = vec2(5.0)

        tiledUVs(
          uv,          // env.UV,
          channelPos,  // env.CHANNEL_0_POSITION,
          channelSize, // env.CHANNEL_0_SIZE,
          entitySize,  // env.SIZE,
          textureSize  // env.TEXTURE_SIZE
        )
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 uv=vec2(1.0);
      |vec2 channelPos=vec2(2.0);
      |vec2 channelSize=vec2(3.0);
      |vec2 entitySize=vec2(4.0);
      |vec2 textureSize=vec2(5.0);
      |channelPos+((fract(uv*(entitySize/textureSize)))*channelSize);
      |""".stripMargin.trim
    )

  }

  test("should correctly render stretch code") {

    inline def fragment =
      Shader {
        import TileAndStretch.*

        val uv: vec2          = vec2(1.0)
        val channelPos: vec2  = vec2(2.0)
        val channelSize: vec2 = vec2(3.0)

        stretchedUVs(
          uv,         // env.UV,
          channelPos, // env.CHANNEL_0_POSITION,
          channelSize // env.CHANNEL_0_SIZE,
        )
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 uv=vec2(1.0);
      |vec2 channelPos=vec2(2.0);
      |vec2 channelSize=vec2(3.0);
      |channelPos+(uv*channelSize);
      |""".stripMargin.trim
    )

  }

  test("should correctly render tile and stretch code") {

    inline def fragment =
      Shader {
        import TileAndStretch.*

        // Delegates
        val _tileAndStretchChannel: (Int, vec4, sampler2D.type, vec2, vec2, vec2, vec2, vec2) => vec4 =
          tileAndStretchChannel

        val fillType: Int              = 0
        val fallback: vec4             = vec4(1.0)
        val srcChannel: sampler2D.type = sampler2D
        val channelPos: vec2           = vec2(2.0)
        val channelSize: vec2          = vec2(3.0)
        val uv: vec2                   = vec2(4.0)
        val entitySize: vec2           = vec2(5.0)
        val textureSize: vec2          = vec2(6.0)

        _tileAndStretchChannel(
          fillType,    // env.FILLTYPE.toInt,
          fallback,    // env.CHANNEL_0,
          srcChannel,  // env.SRC_CHANNEL,
          channelPos,  // env.CHANNEL_0_POSITION,
          channelSize, // env.CHANNEL_0_SIZE,
          uv,          // env.UV,
          entitySize,  // env.SIZE,
          textureSize  // env.TEXTURE_SIZE
        )
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec4 def0(in int fillType,in vec4 fallback,in sampler2D srcChannel,in vec2 channelPos,in vec2 channelSize,in vec2 uv,in vec2 entitySize,in vec2 textureSize){
      |  vec4 val0;
      |  switch(fillType){
      |    case 1:
      |      val0=texture(srcChannel,channelPos+(uv*channelSize));
      |      break;
      |    case 2:
      |      val0=texture(srcChannel,channelPos+((fract(uv*(entitySize/textureSize)))*channelSize));
      |      break;
      |    case 3:
      |      // maddnes ensues
      |      break;
      |    default:
      |      val0=fallback;
      |      break;
      |  }
      |  return val0;
      |}
      |int fillType=0;
      |vec4 fallback=vec4(1.0);
      |sampler2D srcChannel=sampler2D;
      |vec2 channelPos=vec2(2.0);
      |vec2 channelSize=vec2(3.0);
      |vec2 uv=vec2(4.0);
      |vec2 entitySize=vec2(5.0);
      |vec2 textureSize=vec2(6.0);
      |def0(fillType,fallback,srcChannel,channelPos,channelSize,uv,entitySize,textureSize);
      |""".stripMargin.trim
    )

  }

  test("should correctly render nineslice code") {

    inline def fragment =
      Shader {
        import TileAndStretch.*

        val uv: vec2          = vec2(1.0)
        val channelPos: vec2  = vec2(2.0)
        val channelSize: vec2 = vec2(3.0)
        val entitySize: vec2  = vec2(128.0)
        val textureSize: vec2 = vec2(64.0)

        def doNineSlice(): vec2 =
          nineSliceUVs(
            uv,          // env.UV,
            channelPos,  // env.CHANNEL_0_POSITION,
            channelSize, // env.CHANNEL_0_SIZE,
            entitySize,  // env.SIZE,
            textureSize  // env.TEXTURE_SIZE
          )

        doNineSlice()
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // println(actual)

    assertEquals(
      actual,
      s"""
      |fish
      |""".stripMargin.trim
    )

  }

}
