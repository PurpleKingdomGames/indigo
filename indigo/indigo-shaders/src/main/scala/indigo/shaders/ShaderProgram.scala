package indigo.shaders

trait GLSLProgram[Varyings <: VaryingsProduct] {
  val varyings: Varyings
  val vertex: VertexShader[Varyings]
}

final case class VertexShader[Varyings](
    version: GLSLVersion,
    precision: GLSLPrecision,
    uniforms: Uniforms
) {

  def toGLSL(varyings: VaryingsProduct): String =
    s"""${version.toGLSL}
    |
    |${precision.toGLSL}
    |
    |${uniforms.toGLSL}
    |
    |${varyings.toGLSLOut}
    |
    |"""

}

final case class Uniforms(uniforms: List[Uniform[_]]) extends ToGLSL {
  def toGLSL: String =
    uniforms.map(_.toGLSL).mkString("\n")
}

final case class Uniform[T](name: Ref)(implicit t: GLGLTypeName[T]) extends ToGLSL {
  def toGLSL: String =
    s"""uniform ${t.typeName} ${name.name};"""
}

sealed trait VaryingsProduct {
  def toGLSLIn: String
  def toGLSLOut: String
}
final case class Varyings1[P1](v1: Varying[P1]) extends VaryingsProduct {

  def toGLSLIn: String =
    v1.toGLSLIn

  def toGLSLOut: String =
    v1.toGLSLOut

}

final case class Varying[T](name: Ref)(implicit t: GLGLTypeName[T]) {

  def toGLSLIn: String =
    s"""in ${t.typeName} ${name.name};"""

  def toGLSLOut: String =
    s"""out ${t.typeName} ${name.name};"""

}

sealed trait ToGLSL {
  def toGLSL: String
}

sealed trait GLSLVersion extends ToGLSL
object GLSLVersion {
  case object P300 extends GLSLVersion {
    def toGLSL: String =
      "#version 300 es"
  }
}

sealed trait GLSLPrecision extends ToGLSL
object GLSLPrecision {
  case object LOW extends GLSLPrecision {
    def toGLSL: String =
      "precision lowp float;"
  }
  case object MEDIUM extends GLSLPrecision {
    def toGLSL: String =
      "precision mediump float;"
  }
  case object HIGH extends GLSLPrecision {
    def toGLSL: String =
      "precision highp float;"
  }
}
