package indigo.shared.shader.library

import ultraviolet.syntax.*

object TileAndStretch:

  inline def stretchedUVs(uv: vec2, channelPos: vec2, channelSize: vec2): vec2 =
    channelPos + uv * channelSize

  inline def tiledUVs(uv: vec2, channelPos: vec2, channelSize: vec2, entitySize: vec2, textureSize: vec2): vec2 =
    channelPos + (fract(uv * (entitySize / textureSize)) * channelSize)

  inline def tileAndStretchChannel =
    (
        fillType: Int,
        fallback: vec4,
        srcChannel: sampler2D.type,
        channelPos: vec2,
        channelSize: vec2,
        uv: vec2,
        entitySize: vec2,
        textureSize: vec2
    ) =>
      fillType match
        case 1 =>
          texture2D(
            srcChannel,
            stretchedUVs(uv, channelPos, channelSize)
          )

        case 2 =>
          texture2D(
            srcChannel,
            tiledUVs(uv, channelPos, channelSize, entitySize, textureSize)
          )

        case _ =>
          fallback
