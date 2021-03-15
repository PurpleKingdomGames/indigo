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
import indigo.shared.display.DisplayObjectUniformData
import indigo.shared.materials.BlendShaderData
import indigo.shared.materials.BlendMaterial
import indigo.shared.scenegraph.Blending
import indigo.shared.datatypes.RGBA

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
        .filter(l => l.visible.getOrElse(true) && l.nodes.nonEmpty)
        .zipWithIndex
        .map {
          case (l, i) =>
            val blending   = l.blending.getOrElse(Blending.Normal)
            val shaderData = blending.blendMaterial.toShaderData

            DisplayLayer(
              displayObjectConverter.sceneNodesToDisplayObjects(l.nodes, gameTime, assetMapping, cloneBlankDisplayObjects),
              l.backgroundColor.getOrElse(RGBA.Zero),
              l.magnification,
              l.depth.map(_.zIndex).getOrElse(i),
              blending.entity,
              blending.layer,
              shaderData.shaderId,
              mergeShaderToUniformData(shaderData)
            )
        }
        .sortBy(_.depth)

    val sceneBlend = scene.blendMaterial.getOrElse(BlendMaterial.Normal).toShaderData

    new ProcessedSceneData(
      displayLayers,
      cloneBlankDisplayObjects,
      scene.lights,
      sceneBlend.shaderId,
      mergeShaderToUniformData(sceneBlend)
    )
  }

  def calculateProjectionMatrix(width: Double, height: Double, magnification: Double): CheapMatrix4 =
    CheapMatrix4.orthographic(width / magnification.toDouble, height / magnification.toDouble)

  def mergeShaderToUniformData(shaderData: BlendShaderData): Option[DisplayObjectUniformData] =
    shaderData.uniformBlock.map { ub =>
      DisplayObjectUniformData(
        uniformHash = ub.uniformHash,
        blockName = ub.blockName,
        data = DisplayObjectConversions.packUBO(ub.uniforms)
      )
    }

}
