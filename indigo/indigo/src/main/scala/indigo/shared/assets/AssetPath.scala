package indigo.shared.assets

opaque type AssetPath = String
object AssetPath:
  inline def apply(value: String): AssetPath            = value
  extension (ap: AssetPath) inline def toString: String = ap
  given CanEqual[AssetPath, AssetPath]                  = CanEqual.derived
