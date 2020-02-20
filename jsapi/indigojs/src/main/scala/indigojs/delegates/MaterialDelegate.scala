package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName

sealed trait MaterialDelegate {
  def toInternal: Material =
    this match {
      case t: TexturedDelegate =>
        Material.Textured(AssetName(t.diffuse))

      case l: LitDelegate =>
        Material.Lit(
          AssetName(l.albedo),
          l.emission.map(AssetName.apply),
          l.normal.map(AssetName.apply),
          l.specular.map(AssetName.apply)
        )
    }
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Textured")
final class TexturedDelegate(_diffuse: String) extends MaterialDelegate {
  @JSExport
  val diffuse: String = _diffuse
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Lit")
final class LitDelegate(
    _albedo: String,
    _emission: Option[String],
    _normal: Option[String],
    _specular: Option[String]
) extends MaterialDelegate {
  @JSExport
  val albedo: String = _albedo

  @JSExport
  val emission: Option[String] = _emission

  @JSExport
  val normal: Option[String] = _normal

  @JSExport
  val specular: Option[String] = _specular
}
