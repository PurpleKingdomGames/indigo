package indigo.shared.display

import indigo.shared.assets.AssetName

sealed trait CustomShader {
  val id: ShaderId
}
object CustomShader {
  final case class Source(id: ShaderId, vertex: String, fragment: String, light: String) extends CustomShader
  final case class External(id: ShaderId, vertex: AssetName, fragment: AssetName, light: AssetName) extends CustomShader
}
