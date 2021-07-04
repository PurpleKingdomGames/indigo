package indigo.shared.platform

import indigo.shared.datatypes.Vector2
import indigo.platform.assets.AtlasId

final case class TextureRefAndOffset(atlasName: AtlasId, atlasSize: Vector2, offset: Vector2, size: Vector2)
