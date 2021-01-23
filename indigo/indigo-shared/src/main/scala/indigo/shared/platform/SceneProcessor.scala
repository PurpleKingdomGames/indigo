package indigo.shared.platform

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.platform.AssetMapping
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.display.DisplayObject
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Sprite
import indigo.shared.platform.ProcessedSceneData

final class SceneProcessor(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
) {

  private val displayObjectConverter: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)

  def purgeTextureAtlasCaches(): Unit =
    displayObjectConverter.purgeTextureAtlasCaches()

  def processScene(
      gameTime: GameTime,
      scene: SceneUpdateFragment,
      assetMapping: AssetMapping,
      screenWidth: Double,
      screenHeight: Double,
      orthographicProjectionMatrix: CheapMatrix4
  ): ProcessedSceneData = {

    val gameProjection =
      scene.gameLayer.magnification
        .map { m =>
          calculateProjectionMatrix(screenWidth, screenHeight, m.toDouble)
        }
        .getOrElse(orthographicProjectionMatrix)

    val lightingProjection =
      scene.lightingLayer.magnification
        .map { m =>
          calculateProjectionMatrix(screenWidth, screenHeight, m.toDouble)
        }
        .getOrElse(orthographicProjectionMatrix)

    val uiProjection =
      scene.uiLayer.magnification
        .map { m =>
          calculateProjectionMatrix(screenWidth, screenHeight, m.toDouble)
        }
        .getOrElse(orthographicProjectionMatrix)

    val cloneBlankDisplayObjects =
      scene.cloneBlanks.foldLeft(Map.empty[String, DisplayObject]) { (acc, blank) =>
        blank.cloneable match {
          case g: Graphic =>
            acc + (blank.id.value -> displayObjectConverter.graphicToDisplayObject(g, assetMapping))

          case s: Sprite =>
            animationsRegister.fetchAnimationForSprite(gameTime, s.bindingKey, s.animationKey, s.animationActions) match {
              case None =>
                acc

              case Some(anim) =>
                acc + (blank.id.value -> displayObjectConverter.spriteToDisplayObject(boundaryLocator, s, assetMapping, anim))
            }
        }
      }

    val gameLayerDisplayObjects =
      displayObjectConverter
        .sceneNodesToDisplayObjects(scene.gameLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)
        .sortWith((d1, d2) => d1.z > d2.z)

    val lightingLayerDisplayObjects =
      displayObjectConverter
        .sceneNodesToDisplayObjects(scene.lightingLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)
        .sortWith((d1, d2) => d1.z > d2.z)

    val distortionLayerDisplayObjects =
      displayObjectConverter
        .sceneNodesToDisplayObjects(scene.distortionLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)
        .sortWith((d1, d2) => d1.z > d2.z)

    val uiLayerDisplayObjects =
      displayObjectConverter
        .sceneNodesToDisplayObjects(scene.uiLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)
        .sortWith((d1, d2) => d1.z > d2.z)

    new ProcessedSceneData(
      gameProjection,
      lightingProjection,
      uiProjection,
      gameLayerDisplayObjects.toList,
      lightingLayerDisplayObjects.toList,
      distortionLayerDisplayObjects.toList,
      uiLayerDisplayObjects.toList,
      cloneBlankDisplayObjects,
      scene.lights,
      scene.ambientLight,
      scene.screenEffects.gameColorOverlay,
      scene.screenEffects.uiColorOverlay,
      scene.gameLayer.tint,
      scene.lightingLayer.tint,
      scene.uiLayer.tint,
      scene.gameLayer.saturation,
      scene.lightingLayer.saturation,
      scene.uiLayer.saturation
    )
  }

  def calculateProjectionMatrix(width: Double, height: Double, magnification: Double): CheapMatrix4 =
    CheapMatrix4.orthographic(width / magnification.toDouble, height / magnification.toDouble)
}
