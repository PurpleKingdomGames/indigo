package indigo.shared.display

// import indigo.shared.assets.AssetName

sealed trait CustomShader {
  val id: ShaderId
}
object CustomShader {
  final case class Source(id: ShaderId, vertex: String, fragment: String, lighting: String) extends CustomShader
  // final case class Text(id: ShaderId, vertex: AssetName, fragment: AssetName) extends CustomShader
}
