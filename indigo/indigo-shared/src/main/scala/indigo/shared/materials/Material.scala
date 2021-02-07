package indigo.shared.materials

trait Material {
  def hash: String
  def toGLSLShader: GLSLShader
}
