package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Texture

sealed trait MaterialDelegate {
  def toInternal: Material =
    this match {
      case t: TexturedDelegate =>
        new Material.Textured(AssetName(t.diffuse), t.isLit)

      case l: LitDelegate =>
        Material.Lit(
          AssetName(l.albedo),
          l.emission.map(_.toInternal),
          l.normal.map(_.toInternal),
          l.specular.map(_.toInternal)
        )
    }
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Textured")
final class TexturedDelegate(_diffuse: String, _isLit: Boolean) extends MaterialDelegate {
  @JSExport
  val diffuse: String = _diffuse
  @JSExport
  val isLit: Boolean = _isLit

  def lit: TexturedDelegate =
    new TexturedDelegate(diffuse, true)

  def unlit: TexturedDelegate =
    new TexturedDelegate(diffuse, false)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Lit")
final class LitDelegate(
    _albedo: String,
    _emission: Option[TextureDelegate],
    _normal: Option[TextureDelegate],
    _specular: Option[TextureDelegate]
) extends MaterialDelegate {
  @JSExport
  val albedo: String = _albedo

  @JSExport
  val emission: Option[TextureDelegate] = _emission

  @JSExport
  val normal: Option[TextureDelegate] = _normal

  @JSExport
  val specular: Option[TextureDelegate] = _specular
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Texture")
final class TextureDelegate(_assetName: String, _amount: Double) {

  @JSExport
  val assetName: String = _assetName
  @JSExport
  val amount: Double = _amount

  def toInternal: Texture =
    new Texture(AssetName(assetName), amount)
}
