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

final case class Assign[T <: ShaderType](name: String, expression: Expression[T]) extends ShaderAST {
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

sealed trait Expression[T <: ShaderType] extends ShaderAST

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

final case class ShaderRef[T <: ShaderType](name: String) extends Expression[T] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(name)

  def +(other: ShaderRef[T]): add[T] =
    add(this, other)
  def -(other: ShaderRef[T]): substract[T] =
    substract(this, other)
  def *(other: ShaderRef[T]): multiply[T] =
    multiply(this, other)
  def /(other: ShaderRef[T]): divide[T] =
    divide(this, other)
}
object ShaderRef {

  implicit class ShaderRefVec2(ref: ShaderRef[ShaderType.Vec2]) {
    def x: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".x")
    def y: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".y")
    def r: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".r")
    def g: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".g")
  }

  implicit class ShaderRefVec3(ref: ShaderRef[ShaderType.Vec3]) {
    def x: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".x")
    def y: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".y")
    def z: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".z")
    def r: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".r")
    def g: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".g")
    def b: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".b")
  }

  implicit class ShaderRefVec4(ref: ShaderRef[ShaderType.Vec4]) {
    def x: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".x")
    def y: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".y")
    def z: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".z")
    def w: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".w")
    def r: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".r")
    def g: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".g")
    def b: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".b")
    def a: ShaderRef[ShaderType.Float] = ShaderRef(ref.name + ".a")
  }

}

final case class Literal(value: Float) extends Expression[ShaderType.Float] {

  def +(other: Literal): add[ShaderType.Float] =
    add(this, other)
  def -(other: Literal): substract[ShaderType.Float] =
    substract(this, other)
  def *(other: Literal): multiply[ShaderType.Float] =
    multiply(this, other)
  def /(other: Literal): divide[ShaderType.Float] =
    divide(this, other)

  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"${value.toString()}f"
    )
}
sealed trait ShaderValue[T <: ShaderType] extends Expression[T] {
  def asGLSL(indent: Int): String

  def +(other: ShaderValue[T]): add[T] =
    add(this, other)
  def -(other: ShaderValue[T]): substract[T] =
    substract(this, other)
  def *(other: ShaderValue[T]): multiply[T] =
    multiply(this, other)
  def /(other: ShaderValue[T]): divide[T] =
    divide(this, other)
}
final case class float(value: Expression[ShaderType.Float]) extends ShaderValue[ShaderType.Float] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"${value.asGLSL(0)}"
    )
}
object float {
  def apply(value: Float): float =
    float(Literal(value))
}
final case class vec2(x: Expression[ShaderType.Float], y: Expression[ShaderType.Float]) extends ShaderValue[ShaderType.Vec2] {

  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"vec4(${x.asGLSL(0)}, ${y.asGLSL(0)})"
    )
}
object vec2 {
  def apply(x: Float, y: Float): vec2 =
    vec2(float(x), float(y))
}
final case class vec3(x: Expression[ShaderType.Float], y: Expression[ShaderType.Float], z: Expression[ShaderType.Float]) extends ShaderValue[ShaderType.Vec3] {

  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"vec3(${x.asGLSL(0)}, ${y.asGLSL(0)}, ${z.asGLSL(0)})"
    )
}
object vec3 {
  def apply(x: Float, y: Float, z: Float): vec3 =
    vec3(float(x), float(y), float(z))
}
final case class vec4(x: Expression[ShaderType.Float], y: Expression[ShaderType.Float], z: Expression[ShaderType.Float], w: Expression[ShaderType.Float]) extends ShaderValue[ShaderType.Vec4] {

  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"vec4(${x.asGLSL(0)}, ${y.asGLSL(0)}, ${z.asGLSL(0)}, ${w.asGLSL(0)})"
    )
}
object vec4 {
  def apply(x: Float, y: Float, z: Float, w: Float): vec4 =
    vec4(float(x), float(y), float(z), float(w))
}

sealed trait ShaderOp[T <: ShaderType] extends Expression[T] {
  def asGLSL(indent: Int): String
}
final case class add[T <: ShaderType](a: Expression[T], b: Expression[T]) extends ShaderOp[T] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"(${a.asGLSL(0)} + ${b.asGLSL(0)})"
    )
}
final case class substract[T <: ShaderType](a: Expression[T], b: Expression[T]) extends ShaderOp[T] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"(${a.asGLSL(0)} - ${b.asGLSL(0)})"
    )
}
final case class multiply[T <: ShaderType](a: Expression[T], b: Expression[T]) extends ShaderOp[T] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"(${a.asGLSL(0)} * ${b.asGLSL(0)})"
    )
}
final case class divide[T <: ShaderType](a: Expression[T], b: Expression[T]) extends ShaderOp[T] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"(${a.asGLSL(0)} / ${b.asGLSL(0)})"
    )
}
final case class min(a: Expression[ShaderType.Float], b: Expression[ShaderType.Float]) extends ShaderOp[ShaderType.Float] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"min(${a.asGLSL(0)}, ${b.asGLSL(0)})"
    )
}
final case class max(a: Expression[ShaderType.Float], b: Expression[ShaderType.Float]) extends ShaderOp[ShaderType.Float] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"max(${a.asGLSL(0)}, ${b.asGLSL(0)})"
    )
}
final case class texture2D(textureSample: ShaderRef[ShaderType.Sampler2D], coords: Expression[ShaderType.Vec2]) extends ShaderOp[ShaderType.Vec4] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"texture2D(${textureSample.asGLSL(0)}, ${coords.asGLSL(0)})"
    )
}
final case class mix(a: Expression[ShaderType.Vec4], b: Expression[ShaderType.Vec4], amount: Expression[ShaderType.Float]) extends ShaderOp[ShaderType.Vec4] {
  def asGLSL(indent: Int): String =
    ShaderAST.addIndent(indent)(
      s"mix(${a.asGLSL(0)}, ${b.asGLSL(0)}, ${amount.asGLSL(0)})"
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
