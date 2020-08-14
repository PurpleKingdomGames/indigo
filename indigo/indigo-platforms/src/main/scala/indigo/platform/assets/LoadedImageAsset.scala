package indigo.platform.assets

import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetTag

final class LoadedImageAsset(val name: AssetName, val data: AssetDataFormats.ImageDataFormat, val tag: Option[AssetTag])
