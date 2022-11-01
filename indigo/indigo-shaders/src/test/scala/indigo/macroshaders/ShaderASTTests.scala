package indigo.macroshaders

import ShaderDSL.*

class ShaderASTTests extends munit.FunSuite {

  trait FragEnv {
    val UV: vec2
  }

  test("Build an Indigo compatible fragment shader AST") {
    import ShaderAST.*
    import ShaderAST.DataTypes.*

    val expected: ProceduralShader =
      ProceduralShader(
        List(
          Function(
            "fn0",
            List("env"),
            Block(
              List(
                vec4(List(float(1), float(1), float(0), float(1)))
              )
            ),
            Some(ident("vec4"))
          )
        ),
        ShaderBlock(
          List(
            Block(List(Block(List(CallFunction("fn0", Nil, List("env"), Some(ident("vec4")))))))
          )
        )
      )

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(1.0f, 1.0f, 0.0f, 1.0f)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    // println(">>>>>>")
    // println(actual)
    // println("----")
    // println(actual.render)
    // println("<<<<<<")

    assertEquals(actual, expected)
  }

  test("Inlined external val") {

    inline def alpha: Float = 1.0f

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(1.0f, 1.0f, 0.0f, alpha)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    // println(">>>>>>")
    // println(actual)
    // println("----")
    // println(actual.render)
    // println("<<<<<<")

    val p: Boolean = {
      import ShaderAST.*
      import ShaderAST.DataTypes.*

      actual.exists(_ == vec4(List(float(1), float(1), float(0), float(1))))
    }

    assert(clue(p))
  }

  test("Inlined external val (as def)") {

    inline def zw: vec2 = vec2(0.0f, 1.0f)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(1.0f, 1.0f, zw)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    // println(">>>>>>")
    // println(actual)
    // println("----")
    // println(actual.render)
    // println("<<<<<<")

    val p: Boolean = {
      import ShaderAST.*
      import ShaderAST.DataTypes.*

      actual.exists(_ == vec4(List(float(1), float(1), vec2(0.0f, 1.0f))))
    }

    assert(clue(p))
  }

  test("Inlined external function") {

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

    // println(">>>>>>")
    // println(actual)
    // println("----")
    // println(actual.render)
    // println("<<<<<<")

    val p: Boolean = {
      import ShaderAST.*
      import ShaderAST.DataTypes.*

      val expected =
        Function(
          "fn1",
          List("float alpha"),
          Block(List(vec2(List(float(0), ident("alpha"))))),
          Some(ShaderAST.DataTypes.ident("vec2"))
        )

      actual.exists(_ == expected)
    }

    assert(clue(p))
  }

  test("Inlined external function N args") {

    // Is inlined, which might not be what we want?
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

    // println(">>>>>>")
    // println(actual)
    // println("----")
    // println(actual.render)
    // println("<<<<<<")

    val p: Boolean = {
      import ShaderAST.*
      import ShaderAST.DataTypes.*

      val expected1 =
        Function(
          "fn1",
          List("float blue", "float alpha"),
          Block(List(vec2(List(ident("blue"), ident("alpha"))))),
          Some(ShaderAST.DataTypes.ident("vec2"))
        )

      val expected2 =
        vec2(1.0f, 0.25f)

      actual.exists(_ == expected1) && actual.exists(_ == expected2)
    }

    assert(p)
  }

  test("Programs can use an env value like env.UV as UV") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(env.UV, 0.0f, 1.0f)
      }

    val actual =
      ShaderMacros.toAST(fragment)

    // println(">>>>>>")
    // println(actual)
    // println("----")
    // println(actual.render)
    // println("<<<<<<")

    assert(clue(actual.render).contains("vec4(UV,0.0,1.0)"))
  }

  test("swizzling") {
    inline def fragment1: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(1.0f, 2.0f, 3.0f, 4.0f).wzyx
      }

    val actual1 =
      ShaderMacros.toAST(fragment1)

    // println(">>>>>>")
    // println(actual1)
    // println("----")
    // println(actual1.render)
    // println("<<<<<<")

    assert(clue(actual1.render).contains("vec4(1.0,2.0,3.0,4.0).wzyx"))

    inline def fragment2: Shader[FragEnv, vec3] =
      Shader { env =>
        vec3(1.0f, 2.0f, 3.0f).xxy
      }

    val actual2 =
      ShaderMacros.toAST(fragment2)

    // println(">>>>>>")
    // println(actual2)
    // println("----")
    // println(actual2.render)
    // println("<<<<<<")

    assert(clue(actual2.render).contains("vec3(1.0,2.0,3.0).xxy"))
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

    // println(">>>>>>")
    // println(actual1)
    // println("----")
    // println(circleShader.toGLSL)
    // println("<<<<<<")

    assert(clue(actual1.render).contains("float circleSdf(vec2 v0,float v1){return length(v0)-(3.0);}"))
  }

//   test("can build a multi-statement function") {

//     inline def calculateColour(uv: vec2, sdf: Float): vec4 =
//       val fill       = vec4(uv, 0.0f, 1.0f)
//       val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
//       vec4(fill.xyz * fillAmount, fillAmount)

//     inline def shader =
//       Shader { env =>
//         calculateColour(vec2(1.0, 2.0), 3.0)
//       }

//     val actual1 =
//       ShaderMacros.toAST(shader)

//     // println(">>>>>>")
//     // println(actual1)
//     // println("----")
//     // println(circleShader.toGLSL)
//     // println("<<<<<<")

//     /*
// vec3 rgb(fill v2){return vec3(fill,fill,fill);}
// vec4 calculateColour(vec2 v0, float v1){
//   vec4 fill=vec4(uv$proxy2,0.0,1.0);
//   void fillAmount=step(0.0,3.0)*(fill.a);
//   return vec4(rgb(fill)*(fillAmount),fillAmount);
// }
// vec4 fn0(){return calculateColour(vec2(1.0,2.0),3.0);}
// void fragment(){COLOR=fn0();}
//      */

//     assert(clue(actual1.render).contains("float circleSdf"))
//   }

//   test("Small procedural shader") {

//     inline def circleSdf(p: vec2, r: Float): Float =
//       length(p) - r

//     inline def calculateColour(uv: vec2, sdf: Float): vec4 =
//       val fill       = vec4(uv, 0.0f, 1.0f)
//       val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
//       vec4(fill.xyz * fillAmount, fillAmount)

//     inline def fragment: Shader[FragEnv, vec4] =
//       Shader { env =>
//         val sdf = circleSdf(env.UV - 0.5f, 0.5f)
//         calculateColour(env.UV, sdf)
//       }
//     /*
// void circleSdf(){length(0.5)}
// vec3 rgb(){fill;fill;return fill;}
// vec4 calculateColour(){vec4 fill=vec4(UV,0.0,1.0);void fillAmount=step(fill.a);return vec4(rgb(fillAmount),fillAmount);}
// void fn0(){void sdf=circleSdf();calculateColour();}
// void fragment(){COLOR=fn0();}
//      */
//     val actual =
//       ShaderMacros.toAST(fragment)

//     // println(">>>>>>")
//     // println(actual)
//     // println("----")
//     // println(actual.render)
//     // println("<<<<<<")

//     val p: Boolean = {
//       import ShaderAST.*
//       import ShaderAST.DataTypes.*

//       actual.exists(_ == vec4(List(float(1), float(1), vec2(0.0f, 1.0f))))
//     }

//     assert(clue(p))
//   }

}
