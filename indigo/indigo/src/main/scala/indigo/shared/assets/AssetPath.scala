package indigo.shared.assets

opaque type AssetPath = String
object AssetPath:
  inline def apply(value: String): AssetPath = value
  given CanEqual[AssetPath, AssetPath]       = CanEqual.derived
