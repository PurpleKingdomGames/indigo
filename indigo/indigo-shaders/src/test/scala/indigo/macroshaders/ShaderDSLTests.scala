package indigo.macroshaders

import ShaderDSL.*

class ShaderDSLTests extends munit.FunSuite {

  import ShaderAST.*

  test("Fragment: apply & run") {
    val f = Fragment[String, Int](_.length)
    assert(f.run("count me") == 8)
  }

  test("Fragment: pure & run") {
    val f = Fragment.pure[String, Int](10)
    assert(f.run("") == 10)
  }

  test("Fragment: map") {
    val f = Fragment.pure[String, Int](10).map(_ * 10)
    assert(f.run("") == 100)
  }

  test("Fragment: ap") {
    val f = Fragment.pure[String, Int](10).ap(Fragment.pure[String, Int => String](_.toString))
    assert(f.run("") == "10")
  }

  test("Fragment: flatten") {
    val f = Fragment.pure[String, Fragment[String, Int]](
      Fragment.pure[String, Int](10)
    )
    assert(Fragment.join(f).run("") == 10)
    assert(f.flatten.run("") == 10)
  }

  test("Fragment: flatMap") {
    val f = Fragment.pure[String, Int](10).flatMap(i => Fragment.pure[String, Int](i * 10))
    assert(f.run("") == 100)
  }

  test("Fragment: ask") {
    val f = Fragment.pure[String, Int](10)
    assert(f.ask.run("hello") == "hello")
  }

  test("Fragment: asks") {
    val f = Fragment.pure[String, Int](10)
    assert(f.asks((str: String) => str.length()).run("hello") == 5)
  }

  test("Just trying stuff") {
    trait Env:
      def UV: vec2

    inline def circleSdf(p: vec2, r: Float): Float =
      length(p) - r

    val frag: Fragment[Env, rgba] =
      for {
        sdf  <- Fragment((env: Env) => circleSdf(env.UV - 0.5, 0.5))
        fill <- Fragment((env: Env) => rgba(env.UV, 0.0f, 1.0f))
        fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
      } yield rgba(fill.rgb * fillAmount, fillAmount)

    val frag2: Fragment[Env, rgba] =
      for {
        env <- Fragment.ask[Env]
        sdf        = circleSdf(env.UV - 0.5, 0.5)
        fill       = rgba(env.UV, 0.0f, 1.0f)
        fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
      } yield rgba(fill.rgb * fillAmount, fillAmount)

    val frag3: Fragment[Env, rgba] =
      Fragment.ask[Env] |> FragmentFunction[Env, Env, vec2](_.UV)
        >>> (
          FragmentFunction[Env, vec2, Float](uv => circleSdf(uv - 0.5, 0.5)) &&&
            FragmentFunction[Env, vec2, rgba](uv => rgba(uv, 0.0f, 1.0f))
        ) >>>
        FragmentFunction { case (sdf, fill) =>
          val fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
          rgba(fill.rgb * fillAmount, fillAmount)
        }

    val frag4: Fragment[Env, rgba] =
      Fragment.ask[Env].map { env =>
        val sdf        = circleSdf(env.UV - 0.5, 0.5)
        val fill       = rgba(env.UV, 0.0f, 1.0f)
        val fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
        rgba(fill.rgb * fillAmount, fillAmount)
      }

    val frag5: Fragment[Env, rgba] =
      Fragment { env =>
        val sdf        = circleSdf(env.UV - 0.5, 0.5)
        val fill       = rgba(env.UV, 0.0f, 1.0f)
        val fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
        rgba(fill.rgb * fillAmount, fillAmount)
      }

    assert(1 == 2)
  }

}
