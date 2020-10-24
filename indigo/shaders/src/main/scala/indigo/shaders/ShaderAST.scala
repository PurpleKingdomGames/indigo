package indigo.shaders

sealed trait ShaderAST {
  def asGLSL(indent: Int): String

  def addIndent(indent: Int)(str: String): String =
    ShaderAST.addIndent(indent)(str)
}
object ShaderAST {
  def addIndent(indent: Int)(str: String): String =
    List.fill(indent)("  ").mkString + str
}

final case class Statement(entry: ShaderAST, next: Option[Statement]) extends ShaderAST {

  def +(other: Statement): Statement =
    this.copy(
      next = next match {
        case Some(value) =>
          Option(value + other)

        case None =>
          Option(other)
      }
    )

  def asGLSL: String =
    entry.asGLSL(0) + "\n" + next.map(_.asGLSL(0)).getOrElse("")

  def asGLSL(indent: Int): String =
    addIndent(indent)(
      entry.asGLSL(0) + "\n" + next.map(_.asGLSL(indent)).getOrElse("")
    )
}
object Statement {
  def apply(entry: ShaderAST): Statement =
    Statement(entry, None)
}

sealed trait Declare extends ShaderAST
object Declare {
  object Precision {
    case object Low extends ShaderAST {
      def asGLSL(indent: Int): String =
        addIndent(indent)(
          s"precision low float;"
        )
    }
    case object Medium extends ShaderAST {
      def asGLSL(indent: Int): String =
        addIndent(indent)(
          s"precision mediump float;"
        )
    }
    case object High extends ShaderAST {
      def asGLSL(indent: Int): String =
        addIndent(indent)(
          s"precision highp float;"
        )
    }
  }
  final case class Uniform[T <: ShaderType](name: String)(implicit glsl: AsGLSL[T]) extends ShaderAST {
    def asGLSL(indent: Int): String =
      addIndent(indent)(
        s"uniform ${glsl.typeName} $name;"
      )
  }
  final case class Varying[T <: ShaderType](name: String)(implicit glsl: AsGLSL[T]) extends ShaderAST {
    def asGLSL(indent: Int): String =
      addIndent(indent)(
        s"varying ${glsl.typeName} $name;"
      )
  }
  final case class Variable[T <: ShaderType](name: String)(implicit glsl: AsGLSL[T]) extends ShaderAST {
    def asGLSL(indent: Int): String =
      addIndent(indent)(
        s"${glsl.typeName} $name;"
      )
  }
}

final case class Assign(name: String, expression: Expression) extends ShaderAST {
  def asGLSL(indent: Int): String =
    addIndent(indent)(
      s"$name = ${expression.asGLSL(0)};"
    )
}

final case class ShaderFunction[T <: ShaderType](name: String)(statement: Statement)(implicit glsl: AsGLSL[T]) extends ShaderAST {
  def asGLSL(indent: Int): String =
    addIndent(indent)(
      s"${glsl.typeName} $name(void) {\n" + statement.asGLSL(indent + 1) + "}\n"
    )
}

sealed trait Expression extends ShaderAST

sealed trait ShaderType {
  def asGLSL(indent: Int): String
}
object ShaderType {
  sealed trait Void      extends ShaderType
  sealed trait Float     extends ShaderType
  sealed trait Vec2      extends ShaderType
  sealed trait Vec3      extends ShaderType
  sealed trait Vec4      extends ShaderType
  sealed trait Sampler2D extends ShaderType
}

final case class ShaderRef(name: String) extends Expression { // Needs to align types with values
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(name)
}
final case class Literal(value: Float) extends Expression {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"${value.toString()}f"
    )
}
sealed trait ShaderValue extends Expression {
  def asGLSL(indent: Int): String
}
final case class float(value: Expression) extends ShaderValue with ShaderType.Float {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"${value.asGLSL(0)}"
    )
}
object float {
  def apply(value: Float): float =
    float(Literal(value))
}
final case class vec2(x: Expression, y: Expression) extends ShaderValue with ShaderType.Vec2 {

  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"vec4(${x.asGLSL(0)}f, ${y.asGLSL(0)}f)"
    )
}
object vec2 {
  def apply(x: Float, y: Float): vec2 =
    vec2(float(x), float(y))
}
final case class vec3(x: Expression, y: Expression, z: Expression) extends ShaderValue with ShaderType.Vec3 {

  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"vec3(${x.asGLSL(0)}f, ${y.asGLSL(0)}f, ${z.asGLSL(0)}f)"
    )
}
object vec3 {
  def apply(x: Float, y: Float, z: Float): vec3 =
    vec3(float(x), float(y), float(z))
}
final case class vec4(x: Expression, y: Expression, z: Expression, w: Expression) extends ShaderValue with ShaderType.Vec4 {

  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"vec4(${x.asGLSL(0)}f, ${y.asGLSL(0)}f, ${z.asGLSL(0)}f, ${w.asGLSL(0)}f)"
    )
}
object vec4 {
  def apply(x: Float, y: Float, z: Float, w: Float): vec4 =
    vec4(float(x), float(y), float(z), float(w))
}

sealed trait ShaderOp extends Expression {
  def asGLSL(indent: Int): String
}
final case class min(a: Expression, b: Expression) extends ShaderOp {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"min(${a.asGLSL(0)}, ${b.asGLSL(0)})"
    )
}
final case class max(a: Expression, b: Expression) extends ShaderOp {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"max(${a.asGLSL(0)}, ${b.asGLSL(0)})"
    )
}
final case class texture2D(textureSample: ShaderRef, coords: Expression) extends ShaderOp {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"texture2D(${textureSample.asGLSL(0)}, ${coords.asGLSL(0)})"
    )
}
final case class mix(a: Expression, b: Expression, amount: Expression) extends ShaderOp {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"min(${a.asGLSL(0)}, ${b.asGLSL(0)})"
    )
}

sealed trait AsGLSL[T] {
  def typeName: String
}

object AsGLSL {

  implicit val voidAsGLSL: AsGLSL[ShaderType.Void] =
    new AsGLSL[ShaderType.Void] {
      def typeName: String =
        "void"
    }

  implicit val floatAsGLSL: AsGLSL[ShaderType.Float] =
    new AsGLSL[ShaderType.Float] {
      def typeName: String =
        "float"
    }

  implicit val vec2AsGLSL: AsGLSL[ShaderType.Vec2] =
    new AsGLSL[ShaderType.Vec2] {
      def typeName: String =
        "vec2"
    }

  implicit val vec3AsGLSL: AsGLSL[ShaderType.Vec3] =
    new AsGLSL[ShaderType.Vec3] {
      def typeName: String =
        "vec3"
    }

  implicit val vec4AsGLSL: AsGLSL[ShaderType.Vec4] =
    new AsGLSL[ShaderType.Vec4] {
      def typeName: String =
        "vec4"
    }

  implicit val sampler2dAsGLSL: AsGLSL[ShaderType.Sampler2D] =
    new AsGLSL[ShaderType.Sampler2D] {
      def typeName: String =
        "sampler2D"
    }

}

/*
// Inputs
// - uniforms
texture : sampler2D
tint: vec4

// Outputs
gl_FragColor : vec4

// - varyings
tex-coordinate: vec2
alpha: float

// types
vec2
vec4
float

//operations
mix
max
texture2D
 */
