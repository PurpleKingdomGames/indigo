package indigo.platform.renderer.shared

import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.mutable.CheapMatrix4

object CameraHelper:

  def calculateCameraMatrix(
      screenWidth: Double,
      screenHeight: Double,
      renderMagnification: Double,
      actualMagnification: Double,
      cameraX: Double,
      cameraY: Double,
      cameraZoom: Double,
      flipY: Boolean,
      cameraRotation: Radians,
      isLookAt: Boolean
  ): CheapMatrix4 =
    val newWidth  = screenWidth / renderMagnification
    val newHeight = screenHeight / renderMagnification

    val bounds: (Float, Float, Float, Float) =
      if isLookAt then zoom(0, 0, newWidth.toFloat, newHeight.toFloat, cameraZoom.toFloat)
      else zoom(cameraX.toFloat, cameraY.toFloat, newWidth.toFloat, newHeight.toFloat, cameraZoom.toFloat)

    val mat =
      if isLookAt then
        val m1 =
          CheapMatrix4.identity
            .translate(-cameraX.toFloat, -cameraY.toFloat, 1.0f)
            .rotate(cameraRotation.toFloat)
            .translate(
              Math.floor(newWidth.toFloat / (2.0f * actualMagnification.toFloat)).toFloat,
              Math.floor(newHeight.toFloat / (2.0f * actualMagnification.toFloat)).toFloat,
              1.0f
            )

        m1 * CheapMatrix4
          .orthographic(
            bounds._1,
            bounds._2,
            bounds._3,
            bounds._4
          )
      else
        CheapMatrix4.identity
          .rotate(cameraRotation.toFloat) *
          CheapMatrix4
            .orthographic(
              bounds._1,
              bounds._2,
              bounds._3,
              bounds._4
            )

    if flipY then mat.scale(1.0, -1.0, 1.0) else mat

  def zoom(x: Float, y: Float, width: Float, height: Float, zoom: Float): (Float, Float, Float, Float) =
    val newWidth: Float  = width / zoom
    val newHeight: Float = height / zoom
    val amountH: Float   = (width - (width / zoom)) / 2
    val amountV: Float   = (height - (height / zoom)) / 2

    (
      x + amountH,
      y + amountV,
      newWidth,
      newHeight
    )
