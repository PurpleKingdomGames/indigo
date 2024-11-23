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

    def _mapUVToRegionUV: (vec2, vec4) => vec2 =
      (uv, entityRegionUV) => mapUVToRegionUV(uv, entityRegionUV)

    def _tileUV: (vec2, vec2, vec2) => vec2 =
      (regionalUV, unscaledEntityRegionSize, unscaledTextureRegionSize) =>
        tileUV(regionalUV, unscaledEntityRegionSize, unscaledTextureRegionSize)

    def mapUVToRegionAndTile: (vec2, vec4, vec2, vec2) => vec2 =
      (uv, entityRegionUV, unscaledEntityRegionSize, unscaledTextureRegionSize) =>
        _tileUV(_mapUVToRegionUV(uv, entityRegionUV), unscaledEntityRegionSize, unscaledTextureRegionSize)

    def _tiledUVsToTextureCoords: (vec2, vec4, vec2, vec2) => vec2 =
      (tiledUVs, textureRegion, channelPos, channelSize) =>
        tiledUVsToTextureCoords(tiledUVs, textureRegion, channelPos, channelSize)

    // Scale the coords and the size of the region by the size of the entity/texture
    def _regionToUV: (vec4, vec2) => vec4 = (region, size) => regionToUV(region, size)

    def _regionContainsUV: (vec4, vec2) => Boolean = (region, uv) => regionContainsUV(region, uv)

    // A rectangle inside the texture that defines the center, from which we can work out the other regions.
    // val centerSquare = nineSliceCenter
    val centerSquareX = nineSliceCenter.x
    val centerSquareY = nineSliceCenter.y
    val centerSquareW = nineSliceCenter.z
    val centerSquareH = nineSliceCenter.w

    // Work out all the texture regions in pixels as rectangles (x,y,w,h)
    // format: off
    val textureRegionTL =
      vec4(0.0f, 0.0f, centerSquareX, centerSquareY)
    val textureRegionTM =
      vec4(centerSquareX, 0.0f, centerSquareW, centerSquareY)
    val textureRegionTR =
      vec4(centerSquareX + centerSquareW, 0.0f, textureSize.x - centerSquareW - centerSquareX, centerSquareY)

    val textureRegionML =
      vec4(0.0f, centerSquareY, centerSquareX, centerSquareH)
    val textureRegionMM =
      vec4(centerSquareX, centerSquareY, centerSquareW, centerSquareH)
    val textureRegionMR =
      vec4(centerSquareX + centerSquareW, centerSquareY, textureSize.x - centerSquareW - centerSquareX, centerSquareH)

    val textureRegionBL =
      vec4(0.0f, centerSquareY + centerSquareH, centerSquareX, textureSize.y - centerSquareH - centerSquareY)
    val textureRegionBM =
      vec4(centerSquareX, centerSquareY + centerSquareH, centerSquareW, textureSize.y - centerSquareH - centerSquareY)
    val textureRegionBR =
      vec4(centerSquareX + centerSquareW, centerSquareY + centerSquareH, textureSize.x - centerSquareX - centerSquareW, textureSize.y - centerSquareY - centerSquareH)
    // format: on

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
    val entityCenterX = centerSquareX
    val entityCenterY = centerSquareY
    val entityCenterW = entitySafeSize.x - (textureSize.x - centerSquareW)
    val entityCenterH = entitySafeSize.y - (textureSize.y - centerSquareH)

    // Work out all the entity regions in pixels as rectangles (x,y,w,h)
    // format: off
    val entityRegionTL =
      vec4(0.0f, 0.0f, entityCenterX, entityCenterY)
    val entityRegionTM =
      vec4(entityCenterX, 0.0f, entityCenterW, entityCenterY)
    val entityRegionTR =
      vec4(entityCenterX + entityCenterW, 0.0f, entitySafeSize.x - entityCenterW - entityCenterX, entityCenterY)

    val entityRegionML =
      vec4(0.0f, entityCenterY, entityCenterX, entityCenterH)
    val entityRegionMM =
      vec4(entityCenterX, entityCenterY, entityCenterW, entityCenterH)
    val entityRegionMR =
      vec4(entityCenterX + entityCenterW, entityCenterY, entitySafeSize.x - entityCenterW - entityCenterX, entityCenterH)

    val entityRegionBL =
      vec4(0.0f, entityCenterY + entityCenterH, entityCenterX, entitySafeSize.y - entityCenterH - entityCenterY)
    val entityRegionBM =
      vec4(entityCenterX, entityCenterY + entityCenterH, entityCenterW, entitySafeSize.y - entityCenterH - entityCenterY)
    val entityRegionBR =
      vec4(entityCenterX + entityCenterW, entityCenterY + entityCenterH, entitySafeSize.x - entityCenterX - entityCenterW, entitySafeSize.y - entityCenterY - entityCenterH)
    // format: on

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
          mapUVToRegionAndTile(originalUV, entityRegionTLUV, entityRegionTL.zw, textureRegionTL.zw)
        _tiledUVsToTextureCoords(uv, textureRegionTLUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionTMUV, originalUV) then
        val uv =
          mapUVToRegionAndTile(originalUV, entityRegionTMUV, entityRegionTM.zw, textureRegionTM.zw)
        _tiledUVsToTextureCoords(uv, textureRegionTMUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionTRUV, originalUV) then
        val uv =
          mapUVToRegionAndTile(originalUV, entityRegionTRUV, entityRegionTR.zw, textureRegionTR.zw)
        _tiledUVsToTextureCoords(uv, textureRegionTRUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionMLUV, originalUV) then
        val uv =
          mapUVToRegionAndTile(originalUV, entityRegionMLUV, entityRegionML.zw, textureRegionML.zw)
        _tiledUVsToTextureCoords(uv, textureRegionMLUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionMMUV, originalUV) then
        val uv =
          mapUVToRegionAndTile(originalUV, entityRegionMMUV, entityRegionMM.zw, textureRegionMM.zw)
        _tiledUVsToTextureCoords(uv, textureRegionMMUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionMRUV, originalUV) then
        val uv =
          mapUVToRegionAndTile(originalUV, entityRegionMRUV, entityRegionMR.zw, textureRegionMR.zw)
        _tiledUVsToTextureCoords(uv, textureRegionMRUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionBLUV, originalUV) then
        val uv =
          mapUVToRegionAndTile(originalUV, entityRegionBLUV, entityRegionBL.zw, textureRegionBL.zw)
        _tiledUVsToTextureCoords(uv, textureRegionBLUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionBMUV, originalUV) then
        val uv =
          mapUVToRegionAndTile(originalUV, entityRegionBMUV, entityRegionBM.zw, textureRegionBM.zw)
        _tiledUVsToTextureCoords(uv, textureRegionBMUV, channelPos, channelSize)
      else if _regionContainsUV(entityRegionBRUV, originalUV) then
        val uv =
          mapUVToRegionAndTile(originalUV, entityRegionBRUV, entityRegionBR.zw, textureRegionBR.zw)
        _tiledUVsToTextureCoords(uv, textureRegionBRUV, channelPos, channelSize)
      else vec2(0.0f)

    coords

  // Helper functions

  // Map the original UV to a UV in the region of the entity, so that 0.0 is the start of the region and 1.0 is the end
  inline def mapUVToRegionUV(
      uv: vec2,
      entityRegionUV: vec4
  ): vec2 =
    (uv - entityRegionUV.xy) / entityRegionUV.zw

  // Tile the UVs in the region of the entity so that the texture tiles correctly
  inline def tileUV(
      regionalUV: vec2,
      unscaledEntityRegionSize: vec2,
      unscaledTextureRegionSize: vec2
  ): vec2 =
    fract(regionalUV * (unscaledEntityRegionSize / unscaledTextureRegionSize))

  /** Convert tiled UVs to texture coords
    *
    * This is deceptively complicated. At this point we have UVs that are tiling from 0.0 to 1.0 within the region, at
    * the correct ratio for the segment of the nine slice we care about. We happens here is that those 0-1 UVs are
    * translated to the UVs in the texture for the region we care about, then, the channel position and size are used to
    * convert that to the correct texture coords.
    */
  inline def tiledUVsToTextureCoords(
      tiledUVs: vec2,
      textureRegion: vec4,
      channelPos: vec2,
      channelSize: vec2
  ): vec2 =
    channelPos + ((textureRegion.xy + (tiledUVs * textureRegion.zw)) * channelSize)

  // Scale the coords and the size of the region by the size of the entity/texture, to get the region in UV space
  inline def regionToUV(region: vec4, size: vec2): vec4 =
    region / vec4(size, size)

  // Check if a UV is inside a region where the region is in UV space
  inline def regionContainsUV(region: vec4, uv: vec2): Boolean =
    uv.x >= region.x && uv.x < region.x + region.z && uv.y >= region.y && uv.y < region.y + region.w

// -- Nine Slice End --
