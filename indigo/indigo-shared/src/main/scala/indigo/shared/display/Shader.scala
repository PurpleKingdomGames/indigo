package indigo.shared.display

trait Shader {
  val id: ShaderId
  val vertex: String
  val fragment: String
}
