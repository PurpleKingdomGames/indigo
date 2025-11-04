package indigo.shared.assets

opaque type AssetTag = String

object AssetTag:
  inline def apply(value: String): AssetTag          = value
  given CanEqual[AssetTag, AssetTag]                 = CanEqual.derived
  given CanEqual[Option[AssetTag], Option[AssetTag]] = CanEqual.derived
