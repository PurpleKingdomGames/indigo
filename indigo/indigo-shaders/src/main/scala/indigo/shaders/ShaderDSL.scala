package indigo.shaders

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

sealed trait MathFunc extends Value
final case class Abs(value: Value) extends MathFunc {
  def render: String =
    s"abs(${value.render})"
}
final case class Min(value1: Value, value2: Value) extends MathFunc {
  def render: String =
    s"min(${value1.render}, ${value2.render})"
}
final case class Max(value1: Value, value2: Value) extends MathFunc {
  def render: String =
    s"max(${value1.render}, ${value2.render})"
}

final case class Ref(name: String)

final case class routine(name: String, args: Ref*)(expr: Value) extends Value {
  def render: String =
    s"""
    |vec4 $name(${args.map(_.name).mkString(", ")}) {
    |  return ${expr.render};
    |}
    |""".stripMargin
}

sealed trait Value {

  def +(other: Value): Expression =
    Expression(this, other, Plus)

  def -(other: Value): Expression =
    Expression(this, other, Minus)

  def *(other: Value): Expression =
    Expression(this, other, Multiply)

  def /(other: Value): Expression =
    Expression(this, other, Divide)

  def render: String
}

final case class Expression(left: Value, right: Value, op: Operation) extends Value {
  def render: String =
    (left, right) match {
      case (_: Expression, _: Expression) =>
        s"(${left.render}) ${op.render} (${right.render})"
      case (_: Expression, _) =>
        s"(${left.render}) ${op.render} ${right.render}"
      case (_, _: Expression) =>
        s"${left.render} ${op.render} (${right.render})"
      case (_, _) =>
        s"${left.render} ${op.render} ${right.render}"
    }

}

sealed trait Primitive extends Value

final case class float(value: Double) extends Primitive {
  def render: String =
    s"$value"
}
object float {
  implicit val typeName: GLGLTypeName[float] =
    new GLGLTypeName[float] {
      def typeName: String = "float"
    }
}

final case class vec2(x: Double, y: Double) extends Primitive {
  def render: String =
    s"vec2($x, $y)"
}
object vec2 {
  implicit val typeName: GLGLTypeName[vec2] =
    new GLGLTypeName[vec2] {
      def typeName: String = "vec2"
    }
}

final case class vec3(x: Double, y: Double, z: Double) extends Primitive {
  def render: String =
    s"vec3($x, $y, $z)"
}
object vec3 {
  implicit val typeName: GLGLTypeName[vec3] =
    new GLGLTypeName[vec3] {
      def typeName: String = "vec3"
    }
}

final case class vec4(x: Double, y: Double, z: Double, w: Double) extends Primitive {
  def render: String =
    s"vec4($x, $y, $z, $w)"
}
object vec4 {
  implicit val typeName: GLGLTypeName[vec4] =
    new GLGLTypeName[vec4] {
      def typeName: String = "vec4"
    }
}

trait GLGLTypeName[T] {
  def typeName: String
}
