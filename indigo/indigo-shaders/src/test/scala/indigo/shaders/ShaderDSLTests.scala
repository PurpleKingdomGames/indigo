package indigo.shaders

class ShaderDSLTests extends munit.FunSuite {

  import ShaderDSL._

  test("expression / addition") {
    val actual =
      vec2(1, 2) + (vec3(3, 4, 5) * vec4(6, 7, 8, 9)) / float(10)

    val expected =
      "vec2(1.0, 2.0) + ((vec3(3.0, 4.0, 5.0) * vec4(6.0, 7.0, 8.0, 9.0)) / 10.0)"

    assertEquals(actual.render, expected)
  }

  test("Wat") {

    val glsl1 = vec2(1, 2) + (vec3(3, 4, 5) * vec4(6, 7, 8, 9)) / float(10)
    println(glsl1.render)

    val glsl2 = Abs(Min(float(1), Max(float(0), vec3(1, 2, 3))))
    println(glsl2.render)

    val glsl3 =
      routine("foo", Ref("color"), Ref("time")) {
        vec3(3, 4, 5) * vec4(6, 7, 8, 9)
      }

    println(glsl3.render)

    assert(1 == 2)

  }

  test("a made up program") {

    // Reference
    /*
    vec2 UV;
    vec4 COLOR;

    //<indigo-fragment>
    layout (std140) uniform CustomData {
      float ALPHA;
      vec3 BORDER_COLOR;
    };

    float sdf(vec2 p) {
      float b = 0.45;
      vec2 d = abs(p) - b;
      float dist = length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
      return step(0.0, dist);
    }

    void fragment(){
      float amount = sdf(UV - 0.5);
      COLOR = vec4(BORDER_COLOR * amount, amount) * ALPHA;
    }
    //</indigo-fragment>
     */

    val sdf: vec2 => float = p => {
      val b = float(0.45)
      val d = Abs(p) - b

      val dist = Length(Max(d, float(0))) + Min(Max(d.x, d.y), float(0))
      // val res: float = Step(float(0), dist)
      // res

      float(0)
    }

    println(sdf(vec2(10, 10)).render)

    assert(1 == 2)

  }

}
