package indigo.shared.platform

import indigo.shared.assets.AssetName

import scala.collection.immutable.HashMap

final class AssetMapping(val mappings: HashMap[AssetName, TextureRefAndOffset])
