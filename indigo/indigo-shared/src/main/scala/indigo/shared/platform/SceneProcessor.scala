package indigo.shared.platform

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.platform.AssetMapping
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayLayer
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

    val displayLayers: List[DisplayLayer] =
      scene.layers.map(l => DisplayLayer(displayObjectConverter.sceneNodesToDisplayObjects(l.nodes, gameTime, assetMapping, cloneBlankDisplayObjects), l.magnification))

    val gameLayerDisplayObjects =
      displayObjectConverterGame.sceneNodesToDisplayObjects(scene.gameLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)

    val lightingLayerDisplayObjects =
      displayObjectConverterLighting.sceneNodesToDisplayObjects(scene.lightingLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)

    val distortionLayerDisplayObjects =
      displayObjectConverterDistortion.sceneNodesToDisplayObjects(scene.distortionLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)

    val uiLayerDisplayObjects =
      displayObjectConverterUi.sceneNodesToDisplayObjects(scene.uiLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)

    new ProcessedSceneData(
      displayLayers,
      gameProjection,
      lightingProjection,
      uiProjection,
      gameLayerDisplayObjects,
      lightingLayerDisplayObjects,
      distortionLayerDisplayObjects,
      uiLayerDisplayObjects,
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
