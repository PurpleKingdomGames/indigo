package indigo.macroshaders

object ShaderDSL:

  sealed trait Operation {
    def render: String
  }
  case object Plus extends Operation {
    def render: String = "+"
  }
  case object Minus extends Operation {
    def render: String = "-"
  }
  case object Multiply extends Operation {
    def render: String = "*"
  }
  case object Divide extends Operation {
    def render: String = "/"
  }

  sealed trait MathFunc[T] extends Value[T]
  final case class Abs(value: Value[_]) extends MathFunc[value.Out] {
    def render: String =
      s"abs(${value.render})"
  }
  final case class Min(value1: Value[_], value2: Value[_]) extends MathFunc[value1.Out] {
    def render: String =
      s"min(${value1.render}, ${value2.render})"
  }
  final case class Max(value1: Value[_], value2: Value[_]) extends MathFunc[value1.Out] {
    def render: String =
      s"max(${value1.render}, ${value2.render})"
  }
  final case class Length(value: Value[_]) extends MathFunc[float.type] {
    def render: String =
      s"length(${value.render})"
  }

  final case class Ref(name: String)

  final case class routine(name: String, args: Ref*)(val expr: Value[_]) extends Value[expr.Out] {
    def render: String =
      s"""
      |vec4 $name(${args.map(_.name).mkString(", ")}) {
      |  return ${expr.render};
      |}
      |""".stripMargin
  }

  sealed trait Value[T] {
    type Out = T

    def +(other: Value[_]): Expression[Out] =
      Expression(this, other, Plus)

    def -(other: Value[_]): Expression[Out] =
      Expression(this, other, Minus)

    def *(other: Value[_]): Expression[Out] =
      Expression(this, other, Multiply)

    def /(other: Value[_]): Expression[Out] =
      Expression(this, other, Divide)

    def render: String
  }

  final case class Expression[T](left: Value[T], right: Value[_], op: Operation) extends Value[T] {
    type Out = left.Out

    def render: String =
      (left, right) match {
        case (_: Expression[_], _: Expression[_]) =>
          s"(${left.render}) ${op.render} (${right.render})"
        case (_: Expression[_], _) =>
          s"(${left.render}) ${op.render} ${right.render}"
        case (_, _: Expression[_]) =>
          s"${left.render} ${op.render} (${right.render})"
        case (_, _) =>
          s"${left.render} ${op.render} ${right.render}"
      }

    //def value: T = left

  }
  // object Expression:
  //   extension (e: Expression[vec2.type])
  //     def x: Float = e.value.x

  sealed trait Primitive[T] extends Value[T]

  final case class float(value: Double) extends Primitive[float.type] {
    type Out = float.type

    def render: String =
      s"$value"
  }
  object float {
    implicit val typeName: GLGLTypeName[float] =
      new GLGLTypeName[float] {
        def typeName: String = "float"
      }
  }

  final case class vec2(x: Double, y: Double) extends Primitive[vec2.type] {
    type Out = vec2.type

    def render: String =
      s"vec2($x, $y)"
  }
  object vec2 {
    implicit val typeName: GLGLTypeName[vec2] =
      new GLGLTypeName[vec2] {
        def typeName: String = "vec2"
      }
    
    def apply(xy: Double): vec2 =
      vec2(xy, xy)
  }

  final case class vec3(x: Double, y: Double, z: Double) extends Primitive[vec3.type] {
    type Out = vec3.type

    def render: String =
      s"vec3($x, $y, $z)"
  }
  object vec3 {
    implicit val typeName: GLGLTypeName[vec3] =
      new GLGLTypeName[vec3] {
        def typeName: String = "vec3"
      }
    
    def apply(xyz: Double): vec3 =
      vec3(xyz, xyz, xyz)
    
    def apply(x: Double, yz: vec2): vec3 =
      vec3(x, yz.x, yz.y)
    
    def apply(xy: vec2, z: Double): vec3 =
      vec3(xy.x, xy.y, z)
  }

  final case class vec4(x: Double, y: Double, z: Double, w: Double) extends Primitive[vec4.type] {
    type Out = vec4.type

    def render: String =
      s"vec4($x, $y, $z, $w)"
  }
  object vec4 {
    implicit val typeName: GLGLTypeName[vec4] =
      new GLGLTypeName[vec4] {
        def typeName: String = "vec4"
      }
    
    def apply(xyz: Double): vec4 =
      vec4(xyz, xyz, xyz, xyz)
    
    def apply(xy: vec2, zw: vec2): vec4 =
      vec4(xy.x, xy.y, zw.x, zw.y)
    
    def apply(x: Double, y: Double, zw: vec2): vec4 =
      vec4(x, y, zw.x, zw.y)
    
    def apply(xy: vec2, z: Double, w: Double): vec4 =
      vec4(xy.x, xy.y, z, w)
    
    def apply(x: Double, yzw: vec3): vec4 =
      vec4(x, yzw.x, yzw.y, yzw.z)
    
    def apply(xyz: vec3, w: Double): vec4 =
      vec4(xyz.x, xyz.y, xyz.z, w)
  }

  trait GLGLTypeName[T] {
    def typeName: String
  }

end ShaderDSL
