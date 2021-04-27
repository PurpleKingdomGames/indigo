package indigo.shared.assets

opaque type AssetName = String
object AssetName:
  def apply(name: String): AssetName = name

  given CanEqual[AssetName, AssetName] = CanEqual.derived
