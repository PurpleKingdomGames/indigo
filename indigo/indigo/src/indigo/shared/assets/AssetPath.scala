package indigo.shared.assets

opaque type AssetPath = String
object AssetPath:
  inline def apply(value: String): AssetPath        = value
  extension (ap: AssetPath) inline def show: String = ap
  given CanEqual[AssetPath, AssetPath]              = CanEqual.derived
