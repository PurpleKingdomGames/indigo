package indigo.shared.shader.library

import ultraviolet.syntax.*

object TileAndStretch:

  inline def tileAndStretchChannel: (Int, vec4, sampler2D.type, vec2, vec2, vec2, vec2, vec2, vec4) => vec4 =
    (
        fillType: Int,
        fallback: vec4,
        srcChannel: sampler2D.type,
        channelPos: vec2,
        channelSize: vec2,
        uv: vec2,
        entitySize: vec2,
        textureSize: vec2,
        nineSliceCenter: vec4
    ) =>
      def _nineSliceUVs: (vec2, vec2, vec2, vec2, vec2, vec4) => vec2 =
        (originalUV, channelPos, channelSize, entitySize, textureSize, nineSliceCenter) =>
          nineSliceUVs(originalUV, channelPos, channelSize, entitySize, textureSize, nineSliceCenter)

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

        case 3 =>
          texture2D(
            srcChannel,
            _nineSliceUVs(uv, channelPos, channelSize, entitySize, textureSize, nineSliceCenter)
          )

        case _ =>
          fallback

  inline def stretchedUVs(uv: vec2, channelPos: vec2, channelSize: vec2): vec2 =
    channelPos + uv * channelSize

  inline def tiledUVs(uv: vec2, channelPos: vec2, channelSize: vec2, entitySize: vec2, textureSize: vec2): vec2 =
    channelPos + (fract(uv * (entitySize / textureSize)) * channelSize)

  // -- Nine Slice --

  // main
  inline def nineSliceUVs(
      originalUV: vec2,
      channelPos: vec2,
      channelSize: vec2,
      entitySize: vec2,
      textureSize: vec2,
      nineSliceCenter: vec4
  ): vec2 =
    // Delegates

    def _mapUVToRegionAndTile: (vec2, vec4, vec4, vec2, vec2) => vec2 =
      (uv, entityRegion, textureRegion, unscaledEntityRegionSize, unscaledTextureRegionSize) =>
        mapUVToRegionAndTile(uv, entityRegion, textureRegion, unscaledEntityRegionSize, unscaledTextureRegionSize)

    def _normalisedUVsToTextureCoords: (vec2, vec4, vec2, vec2) => vec2 =
      (uv, textureRegion, channelPos, channelSize) =>
        normalisedUVsToTextureCoords(uv, textureRegion, channelPos, channelSize)

    // Scale the coords and the size of the region by the size of the entity/texture
    def _regionToUV: (vec4, vec2) => vec4 = (region, size) => regionToUV(region, size)

    def _regionContainsUV: (vec4, vec2) => Boolean = (region, uv) => regionContainsUV(region, uv)

    // A rectangle inside the texture that defines the center, from which we can work out the other regions.
    val centerSquare = nineSliceCenter

    // Work out all the texture regions in pixels as rectangles (x,y,w,h)
    val textureRegionTL =
      vec4(0.0f, 0.0f, centerSquare.x, centerSquare.y)
    val textureRegionTM =
      vec4(centerSquare.x, 0.0f, centerSquare.z - centerSquare.x, centerSquare.y)
    val textureRegionTR =
      vec4(centerSquare.z, 0.0f, textureSize.x - centerSquare.z, centerSquare.y)

    val textureRegionML =
      vec4(0.0f, centerSquare.y, centerSquare.x, centerSquare.w - centerSquare.y)
    val textureRegionMM =
      vec4(centerSquare.xy, centerSquare.zw - centerSquare.xy)
    val textureRegionMR =
      vec4(centerSquare.z, centerSquare.y, textureSize.x - centerSquare.z, centerSquare.w - centerSquare.y)

    val textureRegionBL =
      vec4(0.0f, centerSquare.w, centerSquare.x, textureSize.y - centerSquare.w)
    val textureRegionBM =
      vec4(centerSquare.x, centerSquare.w, centerSquare.z - centerSquare.x, textureSize.y - centerSquare.w)
    val textureRegionBR =
      vec4(centerSquare.z, centerSquare.w, textureSize.x - centerSquare.z, textureSize.y - centerSquare.w)

    val textureRegionTLUV = _regionToUV(textureRegionTL, textureSize)
    val textureRegionTMUV = _regionToUV(textureRegionTM, textureSize)
    val textureRegionTRUV = _regionToUV(textureRegionTR, textureSize)
    val textureRegionMLUV = _regionToUV(textureRegionML, textureSize)
    val textureRegionMMUV = _regionToUV(textureRegionMM, textureSize)
    val textureRegionMRUV = _regionToUV(textureRegionMR, textureSize)
    val textureRegionBLUV = _regionToUV(textureRegionBL, textureSize)
    val textureRegionBMUV = _regionToUV(textureRegionBM, textureSize)
    val textureRegionBRUV = _regionToUV(textureRegionBR, textureSize)

    // The size of the entity in pixels that is safe for nine-slicing, i.e. the size of the top left + bottom right regions
    val minSize        = textureRegionTL.zw + textureRegionBR.zw
    val entitySafeSize = max(entitySize, minSize)

    // A rectangle in pixels inside the entity that defines the center, based on the texture regions, from which we can work out the other regions.
    val entityCenterSquare = vec4(
      textureRegionTL.zw,
      entitySafeSize - textureRegionBR.zw
    )

    // Work out all the entity regions in pixels as rectangles (x,y,w,h)
    val entityRegionTL =
      vec4(
        0.0f,
        0.0f,
        entityCenterSquare.x,
        entityCenterSquare.y
      )
    val entityRegionTM =
      vec4(
        entityCenterSquare.x,
        0.0f,
        entityCenterSquare.z - entityCenterSquare.x,
        entityCenterSquare.y
      )
    val entityRegionTR =
      vec4(
        entityCenterSquare.z,
        0.0f,
        entitySafeSize.x - entityCenterSquare.z,
        entityCenterSquare.y
      )
    val entityRegionML =
      vec4(
        0.0f,
        entityCenterSquare.y,
        entityCenterSquare.x,
        entityCenterSquare.w - entityCenterSquare.y
      )
    val entityRegionMM =
      vec4(
        entityCenterSquare.xy,
        entityCenterSquare.zw - entityCenterSquare.xy
      )
    val entityRegionMR =
      vec4(
        entityCenterSquare.z,
        entityCenterSquare.y,
        entitySafeSize.x - entityCenterSquare.z,
        entityCenterSquare.w - entityCenterSquare.y
      )
    val entityRegionBL =
      vec4(0.0f, entityCenterSquare.w, entityCenterSquare.x, entitySafeSize.y - entityCenterSquare.w)
    val entityRegionBM =
      vec4(
        entityCenterSquare.x,
        entityCenterSquare.w,
        entityCenterSquare.z - entityCenterSquare.x,
        entitySafeSize.y - entityCenterSquare.w
      )
    val entityRegionBR =
      vec4(
        entityCenterSquare.z,
        entityCenterSquare.w,
        entitySafeSize.x - entityCenterSquare.z,
        entitySafeSize.y - entityCenterSquare.w
      )

    val entityRegionTLUV = _regionToUV(entityRegionTL, entitySize)
    val entityRegionTMUV = _regionToUV(entityRegionTM, entitySize)
    val entityRegionTRUV = _regionToUV(entityRegionTR, entitySize)
    val entityRegionMLUV = _regionToUV(entityRegionML, entitySize)
    val entityRegionMMUV = _regionToUV(entityRegionMM, entitySize)
    val entityRegionMRUV = _regionToUV(entityRegionMR, entitySize)
    val entityRegionBLUV = _regionToUV(entityRegionBL, entitySize)
    val entityRegionBMUV = _regionToUV(entityRegionBM, entitySize)
    val entityRegionBRUV = _regionToUV(entityRegionBR, entitySize)

    val coords: vec2 =
      if _regionContainsUV(entityRegionTLUV, originalUV) then
        val uv =
          _mapUVToRegionAndTile(originalUV, entityRegionTLUV, textureRegionTLUV, entityRegionTL.zw, textureRegionTL.zw)
        _normalisedUVsToTextureCoords(uv, textureRegionTLUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionTMUV, originalUV) then
        val uv =
          _mapUVToRegionAndTile(originalUV, entityRegionTMUV, textureRegionTMUV, entityRegionTM.zw, textureRegionTM.zw)
        _normalisedUVsToTextureCoords(uv, textureRegionTMUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionTRUV, originalUV) then
        val uv =
          _mapUVToRegionAndTile(originalUV, entityRegionTRUV, textureRegionTRUV, entityRegionTR.zw, textureRegionTR.zw)
        _normalisedUVsToTextureCoords(uv, textureRegionTRUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionMLUV, originalUV) then
        val uv =
          _mapUVToRegionAndTile(originalUV, entityRegionMLUV, textureRegionMLUV, entityRegionML.zw, textureRegionML.zw)
        _normalisedUVsToTextureCoords(uv, textureRegionMLUV, channelPos, channelSize)
      else if regionContainsUV(entityRegionMMUV, originalUV) then
        val uv =
          _mapUVToRegionAndTile(originalUV, entityRegionMMUV, textureRegionMMUV, entityRegionMM.zw, textureRegionMM.zw)
        _normalisedUVsToTextureCoords(uv, textureRegionMMUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionMRUV, originalUV) then
        val uv =
          _mapUVToRegionAndTile(originalUV, entityRegionMRUV, textureRegionMRUV, entityRegionMR.zw, textureRegionMR.zw)
        _normalisedUVsToTextureCoords(uv, textureRegionMRUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionBLUV, originalUV) then
        val uv =
          _mapUVToRegionAndTile(originalUV, entityRegionBLUV, textureRegionBLUV, entityRegionBL.zw, textureRegionBL.zw)
        _normalisedUVsToTextureCoords(uv, textureRegionBLUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionBMUV, originalUV) then
        val uv =
          _mapUVToRegionAndTile(originalUV, entityRegionBMUV, textureRegionBMUV, entityRegionBM.zw, textureRegionBM.zw)
        _normalisedUVsToTextureCoords(uv, textureRegionBMUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionBRUV, originalUV) then
        val uv =
          _mapUVToRegionAndTile(originalUV, entityRegionBRUV, textureRegionBRUV, entityRegionBR.zw, textureRegionBR.zw)
        _normalisedUVsToTextureCoords(uv, textureRegionBRUV, channelPos, channelSize)
      else vec2(0.0f)

    coords

  // Helper functions

  inline def mapUVToRegionAndTile(
      uv: vec2,
      entityRegion: vec4,
      textureRegion: vec4,
      unscaledEntityRegionSize: vec2,
      unscaledTextureRegionSize: vec2
  ): vec2 =
    val regionalUV = (uv - entityRegion.xy) / entityRegion.zw
    // scaleToEntityRegion(uv, entityRegion)
    val tiledUV = fract(regionalUV * (unscaledEntityRegionSize / unscaledTextureRegionSize))
    // tiledUVs(regionalUV, unscaledEntityRegionSize, unscaledTextureRegionSize)

    tiledUV

  inline def normalisedUVsToTextureCoords(uv: vec2, textureRegion: vec4, channelPos: vec2, channelSize: vec2): vec2 =
    channelPos + ((uv * textureRegion.zw + textureRegion.xy) * channelSize)

  // Scale the coords and the size of the region by the size of the entity/texture
  inline def regionToUV(region: vec4, size: vec2): vec4 =
    region / vec4(size, size)

  inline def regionContainsUV(region: vec4, uv: vec2): Boolean =
    uv.x >= region.x && uv.x < region.x + region.z && uv.y >= region.y && uv.y < region.y + region.w

// -- Nine Slice End --
