package indigo.shared.assets

opaque type AssetName = String
object AssetName:
  inline def apply(name: String): AssetName         = name
  extension (an: AssetName) inline def show: String = an

  given CanEqual[AssetName, AssetName]                 = CanEqual.derived
  given CanEqual[Option[AssetName], Option[AssetName]] = CanEqual.derived
