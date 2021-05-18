// package indigo.shaders

// object ShaderDSL2:

//   final case class vec2(x: Float, y: Float) {
//     def -(value: Float): vec2 =
//       vec2(x - value, y - value)

//     def render: String =
//       s"vec2($x, $y)"
//   }
//   object vec2 {
//     def apply(xy: Float): vec2 =
//       vec2(xy, xy)
//   }

//   final case class vec3(x: Float, y: Float, z: Float) {
//     def render: String =
//       s"vec3($x, $y, $z)"
//   }
//   object vec3 {
//     def apply(xyz: Float): vec3 =
//       vec3(xyz, xyz, xyz)
    
//     def apply(x: Float, yz: vec2): vec3 =
//       vec3(x, yz.x, yz.y)
    
//     def apply(xy: vec2, z: Float): vec3 =
//       vec3(xy.x, xy.y, z)
//   }

//   final case class vec4(x: Float, y: Float, z: Float, w: Float) {
//     def render: String =
//       s"vec4($x, $y, $z, $w)"
//   }
//   object vec4 {
//     def apply(xyz: Float): vec4 =
//       vec4(xyz, xyz, xyz, xyz)
    
//     def apply(xy: vec2, zw: vec2): vec4 =
//       vec4(xy.x, xy.y, zw.x, zw.y)
    
//     def apply(x: Float, y: Float, zw: vec2): vec4 =
//       vec4(x, y, zw.x, zw.y)
    
//     def apply(xy: vec2, z: Float, w: Float): vec4 =
//       vec4(xy.x, xy.y, z, w)
    
//     def apply(x: Float, yzw: vec3): vec4 =
//       vec4(x, yzw.x, yzw.y, yzw.z)
    
//     def apply(xyz: vec3, w: Float): vec4 =
//       vec4(xyz.x, xyz.y, xyz.z, w)
//   }

// end ShaderDSL2
