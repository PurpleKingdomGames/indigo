package indigo.macroshaders

import ShaderDSL.*

class ShaderASTTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  trait FragEnv {
    val UV: vec2
    var COLOR: vec4
  }

  test("Simple conversion to GLSL") {
    inline def shader =
      Shader { _ =>
        vec4(1.0f, 1.0f, 0.0f, 1.0f)
      }

    val actual =
      ShaderMacros.toAST(shader)

    assert(clue(actual.render) == clue("vec4(1.0,1.0,0.0,1.0);"))
  }

  test("Inlined external val") {

    inline def alpha: Float = 1.0f

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        vec4(1.0f, 1.0f, 0.0f, alpha)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    assert(clue(actual.render) == clue("vec4(1.0,1.0,0.0,1.0);"))
  }

  test("Inlined external val (as def)") {

    inline def zw: vec2 = vec2(0.0f, 1.0f)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        vec4(1.0f, 1.0f, zw)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    assert(clue(actual.render) == clue("vec4(1.0,1.0,vec2(0.0,1.0));"))
  }

  test("Inlined external function") {
    // The argument here will be ignored and inlined. Inlines are weird.
    inline def xy(v: Float): vec2 =
      vec2(v)

    inline def zw: Float => vec2 =
      alpha => vec2(0.0f, alpha)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(xy(1.0f), zw(1.0f))
      }

    val actual =
      ShaderMacros.toAST(fragment)

    assertEquals(
      actual.render,
      s"""
      |vec2 xy(float v0){return vec2(1.0);}
      |vec2 fn0(float alpha){return vec2(0.0,alpha);}
      |vec4(xy(1.0),fn0(1.0));
      |""".stripMargin.trim
    )
  }

  test("Inlined external function N args") {

    // The argument here will be ignored and inlined. Inlines are weird.
    inline def xy(red: Float, green: Float): vec2 =
      vec2(red, green)

    // Is treated like a function
    inline def zw: (Float, Float) => vec2 =
      (blue, alpha) => vec2(blue, alpha)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(xy(1.0f, 0.25f), zw(0.5f, 1.0f))
      }

    val actual =
      ShaderMacros.toAST(fragment)

    assertEquals(
      actual.render,
      s"""
      |vec2 xy(float v0,float v1){return vec2(1.0,0.25);}
      |vec2 fn0(float blue,float alpha){return vec2(blue,alpha);}
      |vec4(xy(1.0,0.25),fn0(0.5,1.0));
      |""".stripMargin.trim
    )
  }

  test("Programs can use an env value like env.UV as UV") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(env.UV, 0.0f, 1.0f)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    assertEquals(
      actual.render,
      s"""
      |vec4(UV,0.0,1.0);
      |""".stripMargin.trim
    )
  }

  test("swizzling") {
    inline def fragment1: Shader[FragEnv, vec4] =
      Shader { _ =>
        vec4(1.0f, 2.0f, 3.0f, 4.0f).wzyx
      }

    val actual1 =
      ShaderMacros.toAST(fragment1)

    assertEquals(
      actual1.render,
      s"""
      |vec4(1.0,2.0,3.0,4.0).wzyx;
      |""".stripMargin.trim
    )

    inline def fragment2: Shader[FragEnv, vec3] =
      Shader { _ =>
        vec3(1.0f, 2.0f, 3.0f).xxy
      }

    val actual2 =
      ShaderMacros.toAST(fragment2)

    assertEquals(
      actual2.render,
      s"""
      |vec3(1.0,2.0,3.0).xxy;
      |""".stripMargin.trim
    )

    inline def fragment3: Shader[FragEnv, vec3] =
      Shader { _ =>
        val fill = vec3(1.0f, 2.0f, 3.0f)
        fill.xyz
      }

    val actual3 =
      ShaderMacros.toAST(fragment3)

    assertEquals(
      actual3.render,
      s"""
      |vec3 fill=vec3(1.0,2.0,3.0);fill.xyz;
      |""".stripMargin.trim
    )
  }

  test("can call a native function") {

    inline def circleSdf(p: vec2, r: Float): Float =
      length(p) - r

    inline def circleShader =
      Shader { env =>
        circleSdf(vec2(1.0, 2.0), 3.0)
      }

    val actual1 =
      ShaderMacros.toAST(circleShader)

    assertEquals(
      actual1.render,
      s"""
      |float circleSdf(vec2 v0,float v1){return (length(v0))-(3.0);}
      |circleSdf(vec2(1.0,2.0),3.0);
      |""".stripMargin.trim
    )

    inline def circleShader2: Shader[FragEnv, Float] =
      Shader { env =>
        def circleSdf(p: vec2, r: Float): Float =
          length(p) - r

        circleSdf(env.UV, 3.0)
      }

    val actual2 =
      ShaderMacros.toAST(circleShader2)

    assertEquals(
      actual2.render,
      s"""
      |float circleSdf(vec2 p,float r){return (length(p))-(r);}
      |circleSdf(UV,3.0);
      |""".stripMargin.trim
    )
  }

  test("can build a multi-statement function") {

    inline def shader: Shader[FragEnv, vec4] =
      Shader { env =>
        def calculateColour(uv: vec2, sdf: Float): vec4 =
          val fill       = vec4(uv, 0.0f, 1.0f)
          val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
          vec4(fill.xyz * fillAmount, fillAmount)

        calculateColour(env.UV, 3.0)
      }

    val actual =
      ShaderMacros.toAST(shader)

    assertEquals(
      actual.render,
      s"""
      |vec4 calculateColour(vec2 uv,float sdf){vec4 fill=vec4(uv,0.0,1.0);float fillAmount=((1.0)-(step(0.0,sdf)))*(fill.w);return vec4((fill.xyz)*(fillAmount),fillAmount);}
      |calculateColour(UV,3.0);
      |""".stripMargin.trim
    )
  }

  test("Small procedural shader") {

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        def circleSdf(p: vec2, r: Float): Float =
          length(p) - r

        def calculateColour(uv: vec2, sdf: Float): vec4 =
          val fill       = vec4(uv, 0.0f, 1.0f)
          val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
          vec4(fill.xyz * fillAmount, fillAmount)

        val sdf = circleSdf(env.UV - 0.5f, 0.5f)

        calculateColour(env.UV, sdf)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    assertEquals(
      actual.render,
      s"""
      |float circleSdf(vec2 p,float r){return (length(p))-(r);}
      |vec4 calculateColour(vec2 uv,float sdf){vec4 fill=vec4(uv,0.0,1.0);float fillAmount=((1.0)-(step(0.0,sdf)))*(fill.w);return vec4((fill.xyz)*(fillAmount),fillAmount);}
      |float sdf=circleSdf((UV)-(0.5),0.5);calculateColour(UV,sdf);
      |""".stripMargin.trim
    )
  }

  test("Output a color / Assign") {

    inline def fragment: Shader[FragEnv, Unit] =
      Shader { env =>
        env.COLOR = vec4(1.0f, 0.0f, 0.0f, 1.0f)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    assertEquals(
      actual.render,
      s"""
      |COLOR=vec4(1.0,0.0,0.0,1.0);
      |""".stripMargin.trim
    )
  }

  test("Small procedural shader with fragment function") {

    inline def fragment: Shader[FragEnv, Unit] =
      Shader { env =>
        def circleSdf(p: vec2, r: Float): Float =
          length(p) - r

        def calculateColour(uv: vec2, sdf: Float): vec4 =
          val fill       = vec4(uv, 0.0f, 1.0f)
          val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
          vec4(fill.xyz * fillAmount, fillAmount)

        def fragment: Unit =
          val sdf = circleSdf(env.UV - 0.5f, 0.5f)
          env.COLOR = calculateColour(env.UV, sdf)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    val expected =
      s"""
      |float circleSdf(vec2 p,float r){return (length(p))-(r);}
      |vec4 calculateColour(vec2 uv,float sdf){vec4 fill=vec4(uv,0.0,1.0);float fillAmount=((1.0)-(step(0.0,sdf)))*(fill.w);return vec4((fill.xyz)*(fillAmount),fillAmount);}
      |void fragment(){float sdf=circleSdf((UV)-(0.5),0.5);COLOR=calculateColour(UV,sdf);}
      |""".stripMargin.trim

    assertEquals(
      actual.render,
      expected
    )
  }

  test("if statements") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        val red    = vec4(1.0, 0.0, 0.0, 1.0)
        val green  = vec4(0.0, 1.0, 0.0, 1.0)
        val blue   = vec4(0.0, 0.0, 1.0, 1.0)
        val x: Int = 1

        if x <= 0 then red
        else if x == 1 then blue
        else green
      }

    val actual =
      fragment.toGLSL

    assertEquals(
      actual,
      s"""
      |vec4 red=vec4(1.0,0.0,0.0,1.0);vec4 green=vec4(0.0,1.0,0.0,1.0);vec4 blue=vec4(0.0,0.0,1.0,1.0);int x=1;if((x)<=(0)){red}else{if((x)==(1)){blue}else{green}};
      |""".stripMargin.trim
    )
  }

  // test("switch statements") {
  //   //
  // }

  // test("casting") {
  //   //
  // }

  // test("for loops") {
  //   //
  // }

  // test("while loops") {
  //   //
  // }

}
