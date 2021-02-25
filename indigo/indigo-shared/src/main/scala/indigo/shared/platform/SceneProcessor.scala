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
import indigo.shared.scenegraph.Shape

final class SceneProcessor(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
) {
  private val displayObjectConverter: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)
  private val displayObjectConverterClone: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)

  def purgeCaches(): Unit = {
    displayObjectConverter.purgeCaches()
    displayObjectConverterClone.purgeCaches()
  }

  def processScene(
      gameTime: GameTime,
      scene: SceneUpdateFragment,
      assetMapping: AssetMapping
  ): ProcessedSceneData = {

    val cloneBlankDisplayObjects =
      scene.cloneBlanks.foldLeft(Map.empty[String, DisplayObject]) { (acc, blank) =>
        blank.cloneable match {
          case s: Shape =>
            acc + (blank.id.value -> displayObjectConverterClone.shapeToDisplayObject(s))

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
      scene.layers
        .filter(_.visible)
        .zipWithIndex
        .map {
          case (l, i) =>
            DisplayLayer(
              displayObjectConverter.sceneNodesToDisplayObjects(l.nodes, gameTime, assetMapping, cloneBlankDisplayObjects),
              l.magnification,
              l.depth.map(_.zIndex).getOrElse(i),
              l.blending.entity,
              l.blending.layer
            )
        }
        .sortBy(_.depth)

    new ProcessedSceneData(
      displayLayers,
      cloneBlankDisplayObjects,
      scene.lights,
      scene.ambientLight
    )
  }

  def calculateProjectionMatrix(width: Double, height: Double, magnification: Double): CheapMatrix4 =
    CheapMatrix4.orthographic(width / magnification.toDouble, height / magnification.toDouble)
}
