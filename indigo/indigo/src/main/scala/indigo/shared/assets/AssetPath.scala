package indigo.shared.assets

opaque type AssetPath = String
object AssetPath:
  def apply(value: String): AssetPath = value
