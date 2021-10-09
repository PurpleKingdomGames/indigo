package indigo.shared.platform

import indigo.platform.assets.AtlasId
import indigo.shared.datatypes.Vector2

final case class TextureRefAndOffset(atlasName: AtlasId, atlasSize: Vector2, offset: Vector2, size: Vector2)
