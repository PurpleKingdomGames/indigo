package indigo.shared.assets

opaque type AssetTag = String

object AssetTag:
  def apply(value: String): AssetTag = value
