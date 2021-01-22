package indigo.shared.platform

import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Point

final case class TextureRefAndOffset(atlasName: String, atlasSize: Vector2, offset: Point)
