package indigo.shared.platform

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.platform.AssetMapping
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
import indigo.shared.scenegraph.Light
import indigo.shared.scenegraph.AmbientLight
import indigo.shared.scenegraph.DirectionLight
import indigo.shared.scenegraph.PointLight
import indigo.shared.scenegraph.SpotLight

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
        .filter(l => l.visible.getOrElse(true))
        .zipWithIndex
        .map {
          case (l, i) =>
            val blending   = l.blending.getOrElse(Blending.Normal)
            val shaderData = blending.blendMaterial.toShaderData

            DisplayLayer(
              displayObjectConverter.sceneNodesToDisplayObjects(l.nodes, gameTime, assetMapping, cloneBlankDisplayObjects),
              SceneProcessor.makeLightsData(scene.lights ++ l.lights),
              blending.clearColor.getOrElse(RGBA.Zero),
              l.magnification,
              l.depth.map(_.zIndex).getOrElse(i),
              blending.entity,
              blending.layer,
              shaderData.shaderId,
              SceneProcessor.mergeShaderToUniformData(shaderData)
            )
        }
        .sortBy(_.depth)

    val sceneBlend = scene.blendMaterial.getOrElse(BlendMaterial.Normal).toShaderData

    new ProcessedSceneData(
      displayLayers,
      cloneBlankDisplayObjects,
      sceneBlend.shaderId,
      SceneProcessor.mergeShaderToUniformData(sceneBlend)
    )
  }

}

object SceneProcessor {

  val MaxLights: Int = 8

  /*
    layout (std140) uniform IndigoDynamicLightingData {
      float numOfLights;
      vec4 lightFlags[8]; // vec4(active, type, ???, ???)
      vec4 lightColor[8];
      vec4 lightSpecular[8];
      vec4 lightPositionRotation[8];
      vec4 lightNearFarAngleAttenuation[8];
    };
   */
  def makeLightsData(lights: List[Light]): Array[Float] = {
    val ls = lights.take(MaxLights)
    Array[Float](ls.length.toFloat) ++ ls.foldLeft(LightData.empty)(_ + makeLightData(_)).toArray
  }

  def makeLightData(light: Light): LightData =
    light match {
      case l: AmbientLight =>
        LightData(
          lightFlags = Array[Float](1.0f, 0.0f, 0.0f, 0.0f),
          lightColor = Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.power.toFloat),
          lightSpecular = Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
          lightPositionRotation = Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
          lightNearFarAngleAttenuation = Array[Float](0.0f, 0.0f, 0.0f, 0.0f)
        )

      case l: DirectionLight =>
        LightData(
          lightFlags = Array[Float](1.0f, 1.0f, 0.0f, 0.0f),
          lightColor = Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.power.toFloat),
          lightSpecular = Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
          lightPositionRotation = Array[Float](0.0f, 0.0f, l.height.toFloat, l.rotation.value.toFloat),
          lightNearFarAngleAttenuation = Array[Float](0.0f, 0.0f, 0.0f, 0.0f)
        )

      case l: PointLight =>
        LightData(
          lightFlags = Array[Float](1.0f, 2.0f, 0.0f, 0.0f),
          lightColor = Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.power.toFloat),
          lightSpecular = Array[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specularPower.toFloat),
          lightPositionRotation = Array[Float](l.position.x.toFloat, l.position.y.toFloat, l.height.toFloat, 0.0f),
          lightNearFarAngleAttenuation = Array[Float](l.near.toFloat, l.far.toFloat, 0.0f, l.attenuation.toFloat)
        )

      case l: SpotLight =>
        LightData(
          lightFlags = Array[Float](1.0f, 3.0f, 0.0f, 0.0f),
          lightColor = Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.power.toFloat),
          lightSpecular = Array[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specularPower.toFloat),
          lightPositionRotation = Array[Float](l.position.x.toFloat, l.position.y.toFloat, l.height.toFloat, l.rotation.value.toFloat),
          lightNearFarAngleAttenuation = Array[Float](l.near.toFloat, l.far.toFloat, l.angle.value.toFloat, l.attenuation.toFloat)
        )

      case _ =>
        LightData.empty
    }

  def mergeShaderToUniformData(shaderData: BlendShaderData): Option[DisplayObjectUniformData] =
    shaderData.uniformBlock.map { ub =>
      DisplayObjectUniformData(
        uniformHash = ub.uniformHash,
        blockName = ub.blockName,
        data = DisplayObjectConversions.packUBO(ub.uniforms)
      )
    }
}

final case class LightData(
    lightFlags: Array[Float],
    lightColor: Array[Float],
    lightSpecular: Array[Float],
    lightPositionRotation: Array[Float],
    lightNearFarAngleAttenuation: Array[Float]
) {
  def +(other: LightData): LightData =
    this.copy(
      lightFlags = lightFlags ++ other.lightFlags,
      lightColor = lightColor ++ other.lightColor,
      lightSpecular = lightSpecular ++ other.lightSpecular,
      lightPositionRotation = lightPositionRotation ++ other.lightPositionRotation,
      lightNearFarAngleAttenuation = lightNearFarAngleAttenuation ++ other.lightNearFarAngleAttenuation
    )

  def toArray: Array[Float] =
    lightFlags ++
      lightColor ++
      lightSpecular ++
      lightPositionRotation ++
      lightNearFarAngleAttenuation
}
object LightData {
  val empty: LightData =
    LightData(
      Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
      Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
      Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
      Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
      Array[Float](0.0f, 0.0f, 0.0f, 0.0f)
    )

  val emptyData: Array[Float] =
    empty.toArray
}
