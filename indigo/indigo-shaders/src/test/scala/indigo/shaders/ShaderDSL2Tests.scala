// package indigo.shaders

// import annotation.targetName

// class ShaderDSLTests extends munit.FunSuite {

//   test("a made up program") {

//     import ShaderDSL2._

//     // Reference
//     /*
//     vec2 UV;
//     vec4 COLOR;

//     //<indigo-fragment>
//     layout (std140) uniform CustomData {
//       float ALPHA;
//       vec3 BORDER_COLOR;
//     };

//     float sdf(vec2 p) {
//       float b = 0.45;
//       vec2 d = abs(p) - b;
//       float dist = length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
//       return step(0.0, dist);
//     }

//     void fragment(){
//       float amount = sdf(UV - 0.5);
//       COLOR = vec4(BORDER_COLOR * amount, amount) * ALPHA;
//     }
//     //</indigo-fragment>
//      */

//     def abs(v: vec2): vec2 =
//       vec2(Math.abs(v.x), Math.abs(v.y)) // TODO: Should be @glsl.native

//     def length(v: vec2): Float =
//       Math.sqrt(Math.abs(Math.pow(v.x, 2) + Math.pow(v.y, 2))).toFloat

//     def max(f1: Float, f2: Float): Float =
//       Math.max(f1, f2)
//     @targetName("vec2_max")
//     def max(v: vec2, f: Float): vec2 =
//       vec2(Math.max(v.x, f), Math.max(v.y, f))
      

//     def min(f1: Float, f2: Float): Float =
//       Math.max(f1, f2)

//     val sdf: vec2 => Float = p => {
//       val b    = 0.45f
//       val d    = abs(p) - b
//       val dist = length(max(d, 0f)) + min(max(d.x, d.y), 0f)
//       step(0f, dist)
//     }

//     println(sdf(vec2(10)).render)

//     assert(1 == 2)

//   }

// }
