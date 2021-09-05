package indigo.platform.renderer.shared

import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.datatypes.Radians

object CameraHelper:

  def calculateCameraMatrix(
      screenWidth: Double,
      screenHeight: Double,
      magnification: Double,
      cameraX: Double,
      cameraY: Double,
      cameraZoom: Double,
      flipY: Boolean,
      cameraRotation: Radians,
      isLookAt: Boolean
  ): CheapMatrix4 =
    val newWidth  = screenWidth / magnification
    val newHeight = screenHeight / magnification

    val bounds: (Double, Double, Double, Double) =
      if isLookAt then zoom(0, 0, newWidth, newHeight, cameraZoom)
      else zoom(cameraX, cameraY, newWidth, newHeight, cameraZoom)

    val mat =
      if isLookAt then
        CheapMatrix4.identity
          .translate(-cameraX, -cameraY, 1.0)
          .rotate(cameraRotation)
          .translate(newWidth / 2.0, newHeight / 2.0, 1.0) *
          CheapMatrix4
            .orthographic(
              bounds._1,
              bounds._2,
              bounds._3,
              bounds._4
            )
      else
        CheapMatrix4.identity
          .rotate(cameraRotation) *
          CheapMatrix4
            .orthographic(
              bounds._1,
              bounds._2,
              bounds._3,
              bounds._4
            )

    if flipY then mat.scale(1.0, -1.0, 1.0) else mat

  def zoom(x: Double, y: Double, width: Double, height: Double, zoom: Double): (Double, Double, Double, Double) =
    val newWidth: Double  = width / zoom
    val newHeight: Double = height / zoom
    val amountH: Double   = (width - (width / zoom)) / 2
    val amountV: Double   = (height - (height / zoom)) / 2

    (
      x + amountH,
      y + amountV,
      newWidth,
      newHeight
    )
