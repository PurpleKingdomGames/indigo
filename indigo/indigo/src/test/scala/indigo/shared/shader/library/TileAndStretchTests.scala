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

  test("regionToUV") {
    val actual =
      TileAndStretch.regionToUV(vec4(vec2(16.0f), vec2(32.0f)), vec2(64.0f))

    val expected =
      vec4(vec2(0.25f), vec2(0.5f))

    assertEquals(actual, expected)
  }

  test("regionContainsUV") {
    val uvRegion = TileAndStretch.regionToUV(vec4(vec2(16.0f), vec2(32.0f)), vec2(64.0f))

    assert(clue(!TileAndStretch.regionContainsUV(uvRegion, vec2(0.0f, 0.0f))))
    assert(clue(!TileAndStretch.regionContainsUV(uvRegion, vec2(0.24f, 0.24f))))
    assert(clue(TileAndStretch.regionContainsUV(uvRegion, vec2(0.25f, 0.25f))))
    assert(clue(TileAndStretch.regionContainsUV(uvRegion, vec2(0.5f, 0.5f))))
    assert(clue(TileAndStretch.regionContainsUV(uvRegion, vec2(0.74f, 0.74f))))
    assert(clue(!TileAndStretch.regionContainsUV(uvRegion, vec2(0.75f, 0.75f))))
    assert(clue(!TileAndStretch.regionContainsUV(uvRegion, vec2(1.0f, 1.0f))))
  }

  test("mapUVToRegionUV") {
    val entityRegionUV = TileAndStretch.regionToUV(vec4(vec2(16.0f), vec2(32.0f)), vec2(64.0f))

    def forUV(uv: vec2): vec2 =
      TileAndStretch.mapUVToRegionUV(uv, entityRegionUV)

    assertEquals(clue(forUV(vec2(0.0f))), clue(vec2(-0.5f)))
    assertEquals(clue(forUV(vec2(0.25f))), clue(vec2(0.0f)))
    assertEquals(clue(forUV(vec2(0.5f))), clue(vec2(0.5f)))
    assertEquals(clue(forUV(vec2(0.75f))), clue(vec2(1.0f)))
    assertEquals(clue(forUV(vec2(1.0f))), clue(vec2(1.5f)))
  }

  test("tileUV") {
    val unscaledEntityRegionSize  = vec2(64.0f)
    val unscaledTextureRegionSize = vec2(32.0f)

    // Maybe split this function in two?

    def forUV(uv: vec2): vec2 =
      TileAndStretch.tileUV(uv, unscaledEntityRegionSize, unscaledTextureRegionSize)

    assertEquals(clue(forUV(vec2(0.0f))), clue(vec2(0.0f)))
    assertEquals(clue(forUV(vec2(0.25f))), clue(vec2(0.5f)))
    assertEquals(clue(forUV(vec2(0.5f))), clue(vec2(0.0f)))
    assertEquals(clue(forUV(vec2(0.75f))), clue(vec2(0.5f)))
    // We never see 1.0 because that's the wrap point. In practice this doesnt matter.
    assertEquals(clue(forUV(vec2(1.0f))), clue(vec2(0.0f)))
  }

  test("tiledUVsToTextureCoords") {
    val channelSize   = vec2(64.0f)
    val textureRegion = TileAndStretch.regionToUV(vec4(vec2(16.0f), vec2(32.0f)), channelSize)
    val channelPos    = vec2(10.0f)

    def forUV(tileUV: vec2): vec2 =
      TileAndStretch.tiledUVsToTextureCoords(tileUV, textureRegion, channelPos, channelSize)

    assertEquals(clue(forUV(vec2(0.0f))), clue(vec2(16.0f) + vec2(10.0f)))
    assertEquals(clue(forUV(vec2(0.25f))), clue(vec2(24.0f) + vec2(10.0f)))
    assertEquals(clue(forUV(vec2(0.5f))), clue(vec2(32.0f) + vec2(10.0f)))
    assertEquals(clue(forUV(vec2(0.75f))), clue(vec2(40.0f) + vec2(10.0f)))
    assertEquals(clue(forUV(vec2(1.0f))), clue(vec2(48.0f) + vec2(10.0f)))
  }

}
