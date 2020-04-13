package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Texture
import indigo.shared.datatypes.Material.Lit
import indigo.shared.datatypes.Material.Textured

sealed trait MaterialDelegate {
  def toInternal: Material =
    this match {
      case t: TexturedDelegate =>
        new Material.Textured(AssetName(t.diffuse), t.isLit)

      case l: LitDelegate =>
        Material.Lit(
          AssetName(l.albedo),
          l.emissive.map(_.toInternal),
          l.normal.map(_.toInternal),
          l.specular.map(_.toInternal)
        )
    }
}
object MaterialDelegate {
  def fromInternal(m: Material): MaterialDelegate =
    m match {
      case t: Textured =>
        new TexturedDelegate(t.diffuse.value, t.isLit)

      case l: Lit =>
        new LitDelegate(
          l.albedo.value,
          l.emissive.map(t => new TextureDelegate(t.assetName.value, t.amount)),
          l.normal.map(t => new TextureDelegate(t.assetName.value, t.amount)),
          l.specular.map(t => new TextureDelegate(t.assetName.value, t.amount))
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
    _emissive: Option[TextureDelegate],
    _normal: Option[TextureDelegate],
    _specular: Option[TextureDelegate]
) extends MaterialDelegate {
  @JSExport
  val albedo: String = _albedo

  @JSExport
  val emissive: Option[TextureDelegate] = _emissive

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
