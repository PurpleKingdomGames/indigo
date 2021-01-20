package indigo.facades.worker

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
import indigo.facades.worker.ProcessedSceneData
import indigo.shared.platform.DisplayObjectConversions

import scala.scalajs.js
import scalajs.js.JSConverters._

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
      orthographicProjectionMatrix: js.Array[Double]
  ): ProcessedSceneData = {

    val gameProjection =
      scene.gameLayer.magnification
        .map { m =>
          calculateProjectionMatrix(screenWidth, screenHeight, m.toDouble).mat.toJSArray
        }
        .getOrElse(orthographicProjectionMatrix)

    val lightingProjection =
      scene.lightingLayer.magnification
        .map { m =>
          calculateProjectionMatrix(screenWidth, screenHeight, m.toDouble).mat.toJSArray
        }
        .getOrElse(orthographicProjectionMatrix)

    val uiProjection =
      scene.uiLayer.magnification
        .map { m =>
          calculateProjectionMatrix(screenWidth, screenHeight, m.toDouble).mat.toJSArray
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
      displayObjectConverter.sceneNodesToDisplayObjects(scene.gameLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)

    val lightingLayerDisplayObjects =
      displayObjectConverter.sceneNodesToDisplayObjects(scene.lightingLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)

    val distortionLayerDisplayObjects =
      displayObjectConverter.sceneNodesToDisplayObjects(scene.distortionLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)

    val uiLayerDisplayObjects =
      displayObjectConverter.sceneNodesToDisplayObjects(scene.uiLayer.nodes, gameTime, assetMapping, cloneBlankDisplayObjects)

    new ProcessedSceneData(
      gameProjection,
      lightingProjection,
      uiProjection,
      gameLayerDisplayObjects.toJSArray,
      lightingLayerDisplayObjects.toJSArray,
      distortionLayerDisplayObjects.toJSArray,
      uiLayerDisplayObjects.toJSArray,
      cloneBlankDisplayObjects.toJSMap,
      scene.lights.map(SceneUpdateFragmentConversion.LightConversion.toJS).toJSArray,
      SceneUpdateFragmentConversion.RGBAConversion.toJS(scene.ambientLight),
      SceneUpdateFragmentConversion.RGBAConversion.toJS(scene.screenEffects.gameColorOverlay),
      SceneUpdateFragmentConversion.RGBAConversion.toJS(scene.screenEffects.uiColorOverlay),
      SceneUpdateFragmentConversion.RGBAConversion.toJS(scene.gameLayer.tint),
      SceneUpdateFragmentConversion.RGBAConversion.toJS(scene.lightingLayer.tint),
      SceneUpdateFragmentConversion.RGBAConversion.toJS(scene.uiLayer.tint),
      scene.gameLayer.saturation,
      scene.lightingLayer.saturation,
      scene.uiLayer.saturation
    )
  }

  def calculateProjectionMatrix(width: Double, height: Double, magnification: Double): CheapMatrix4 =
    CheapMatrix4.orthographic(width / magnification.toDouble, height / magnification.toDouble)
}
