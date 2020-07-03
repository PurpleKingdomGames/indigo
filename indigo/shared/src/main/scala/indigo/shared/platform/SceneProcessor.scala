package indigo.shared.platform

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.platform.AssetMapping
import indigo.shared.datatypes.Matrix4
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

  private val displayObjectConverterGame: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)
  private val displayObjectConverterLighting: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)
  private val displayObjectConverterDistortion: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)
  private val displayObjectConverterUi: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)
  private val displayObjectConverterClone: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)

  def purgeCaches(): Unit = {
    displayObjectConverterGame.purgeCaches()
    displayObjectConverterLighting.purgeCaches()
    displayObjectConverterDistortion.purgeCaches()
    displayObjectConverterUi.purgeCaches()
    displayObjectConverterClone.purgeCaches()
  }

  def processScene(
      gameTime: GameTime,
      scene: SceneUpdateFragment,
      assetMapping: AssetMapping,
      screenWidth: Double,
      screenHeight: Double,
      orthographicProjectionMatrix: Matrix4
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

    val gameLayerDisplayObjects =
      displayObjectConverterGame.sceneNodesToDisplayObjects(scene.gameLayer.nodes, gameTime, assetMapping)

    val lightingLayerDisplayObjects =
      displayObjectConverterLighting.sceneNodesToDisplayObjects(scene.lightingLayer.nodes, gameTime, assetMapping)

    val distortionLayerDisplayObjects =
      displayObjectConverterDistortion.sceneNodesToDisplayObjects(scene.distortionLayer.nodes, gameTime, assetMapping)

    val uiLayerDisplayObjects =
      displayObjectConverterUi.sceneNodesToDisplayObjects(scene.uiLayer.nodes, gameTime, assetMapping)

    val cloneBlankDisplayObjects =
      scene.cloneBlanks.foldLeft(Map.empty[String, DisplayObject]) { (acc, blank) =>
        blank.cloneable match {
          case g: Graphic =>
            acc + (blank.id.value -> displayObjectConverterClone.graphicToDisplayObject(g, assetMapping))

          case s: Sprite =>
            animationsRegister.fetchAnimationForSprite(gameTime, s.bindingKey, s.animationKey, s.animationActions) match {
              case None =>
                acc

              case Some(anim) =>
                acc + (blank.id.value -> displayObjectConverterClone.spriteToDisplayObject(boundaryLocator, s, assetMapping, anim))
            }
        }
      }

    new ProcessedSceneData(
      gameProjection,
      lightingProjection,
      uiProjection,
      gameLayerDisplayObjects,
      lightingLayerDisplayObjects,
      distortionLayerDisplayObjects,
      uiLayerDisplayObjects,
      cloneBlankDisplayObjects,
      scene.lights,
      scene.ambientLight.toClearColor,
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

  def calculateProjectionMatrix(width: Double, height: Double, magnification: Double): Matrix4 =
    Matrix4.orthographic(width / magnification.toDouble, height / magnification.toDouble)
}
