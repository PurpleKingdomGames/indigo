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
import indigo.shared.scenegraph.Falloff
import indigo.shared.scenegraph.CloneId
import indigo.shared.datatypes.Depth
import indigo.shared.QuickCache
import indigo.shared.scenegraph.CloneBlank

final class SceneProcessor(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
) {
  private val displayObjectConverter: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)
  private val displayObjectConverterClone: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)

  implicit private val uniformsCache: QuickCache[Array[Float]]             = QuickCache.empty
  implicit private val staticCloneCache: QuickCache[Option[DisplayObject]] = QuickCache.empty

  def purgeCaches(): Unit = {
    displayObjectConverter.purgeCaches()
    displayObjectConverterClone.purgeCaches()
    uniformsCache.purgeAllNow()
  }

  def processScene(
      gameTime: GameTime,
      scene: SceneUpdateFragment,
      assetMapping: AssetMapping
  ): ProcessedSceneData = {

    def cloneBlankToDisplayObject(blank: CloneBlank): Option[DisplayObject] =
      blank.cloneable() match
        case s: Shape =>
          Some(displayObjectConverterClone.shapeToDisplayObject(s))

        case g: Graphic[_] =>
          Some(displayObjectConverterClone.graphicToDisplayObject(g, assetMapping))

        case s: Sprite[_] =>
          animationsRegister
            .fetchAnimationForSprite(
              gameTime,
              s.bindingKey,
              s.animationKey,
              s.animationActions
            )
            .map { anim =>
              displayObjectConverterClone.spriteToDisplayObject(
                boundaryLocator,
                s,
                assetMapping,
                anim
              )
            }

        case _ =>
          None

    val cloneBlankDisplayObjects =
      scene.cloneBlanks.foldLeft(Map.empty[CloneId, DisplayObject]) { (acc, blank) =>
        val maybeDO =
          if blank.isStatic then
            QuickCache(blank.id.toString) {
              cloneBlankToDisplayObject(blank)
            }
          else cloneBlankToDisplayObject(blank)

        maybeDO match
          case None                => acc
          case Some(displayObject) => acc + (blank.id -> displayObject)
      }

    val displayLayers: List[DisplayLayer] =
      scene.layers
        .filter(l => l.visible.getOrElse(true))
        .zipWithIndex
        .map { case (l, i) =>
          val blending   = l.blending.getOrElse(Blending.Normal)
          val shaderData = blending.blendMaterial.toShaderData

          DisplayLayer(
            displayObjectConverter
              .sceneNodesToDisplayObjects(l.nodes, gameTime, assetMapping, cloneBlankDisplayObjects),
            SceneProcessor.makeLightsData(scene.lights ++ l.lights),
            blending.clearColor.getOrElse(RGBA.Zero),
            l.magnification,
            l.depth.getOrElse(Depth(i)),
            blending.entity,
            blending.layer,
            shaderData.shaderId,
            SceneProcessor.mergeShaderToUniformData(shaderData),
            l.camera
          )
        }
        .sortBy(_.depth.toInt)

    val sceneBlend = scene.blendMaterial.getOrElse(BlendMaterial.Normal).toShaderData

    new ProcessedSceneData(
      displayLayers,
      cloneBlankDisplayObjects,
      sceneBlend.shaderId,
      SceneProcessor.mergeShaderToUniformData(sceneBlend),
      scene.camera
    )
  }

}

object SceneProcessor {

  val MaxLights: Int = 8

  private val bareLightData: LightData =
    LightData(
      Array[Float](),
      Array[Float](),
      Array[Float](),
      Array[Float](),
      Array[Float]()
    )

  private val missingLightData: Map[Int, List[LightData]] =
    (0 to 8).map { i =>
      (i -> List.fill(i)(LightData.empty))
    }.toMap

  def makeLightsData(lights: List[Light]): Array[Float] = {
    val limitedLights = lights.take(MaxLights)
    val count         = limitedLights.length
    val fullLights    = limitedLights.map(makeLightData) ++ missingLightData(MaxLights - count)

    Array[Float](count.toFloat, 0.0f, 0.0f, 0.0f) ++ fullLights.foldLeft(bareLightData)(_ + _).toArray
  }

  def makeLightData(light: Light): LightData =
    light match {
      case l: AmbientLight =>
        LightData(
          lightFlags = Array[Float](1.0f, 0.0f, 0.0f, 0.0f),
          lightColor = Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular = Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
          lightPositionRotation = Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
          lightNearFarAngleIntensity = Array[Float](0.0f, 0.0f, 0.0f, 0.0f)
        )

      case l: DirectionLight =>
        LightData(
          lightFlags = Array[Float](1.0f, 1.0f, 0.0f, 0.0f),
          lightColor = Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular =
            Array[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specular.a.toFloat),
          lightPositionRotation = Array[Float](0.0f, 0.0f, l.rotation.toFloat, 0.0f),
          lightNearFarAngleIntensity = Array[Float](0.0f, 0.0f, 0.0f, 0.0f)
        )

      case l: PointLight =>
        val useFarCuttOff: Float =
          l.falloff match {
            case Falloff.None(_, far)      => if (far.isDefined) 1.0f else 0.0f
            case Falloff.Linear(_, far)    => if (far.isDefined) 1.0f else 0.0f
            case Falloff.Quadratic(_, far) => if (far.isDefined) 1.0f else 0.0f
            case _                         => 1.0f
          }

        val falloffType: Float =
          l.falloff match {
            case _: Falloff.None            => 0.0f
            case _: Falloff.SmoothLinear    => 1.0f
            case _: Falloff.SmoothQuadratic => 2.0f
            case _: Falloff.Linear          => 3.0f
            case _: Falloff.Quadratic       => 4.0f
          }

        val near: Float =
          l.falloff match {
            case Falloff.None(near, _)            => near.toFloat
            case Falloff.SmoothLinear(near, _)    => near.toFloat
            case Falloff.SmoothQuadratic(near, _) => near.toFloat
            case Falloff.Linear(near, _)          => near.toFloat
            case Falloff.Quadratic(near, _)       => near.toFloat
          }

        val far: Float =
          l.falloff match {
            case Falloff.None(_, far)            => far.map(_.toFloat).getOrElse(10000.0f)
            case Falloff.SmoothLinear(_, far)    => far.toFloat
            case Falloff.SmoothQuadratic(_, far) => far.toFloat
            case Falloff.Linear(_, far)          => far.map(_.toFloat).getOrElse(10000.0f)
            case Falloff.Quadratic(_, far)       => far.map(_.toFloat).getOrElse(10000.0f)
          }

        LightData(
          lightFlags = Array[Float](1.0f, 2.0f, useFarCuttOff, falloffType),
          lightColor = Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular =
            Array[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specular.a.toFloat),
          lightPositionRotation = Array[Float](l.position.x.toFloat, l.position.y.toFloat, 0.0f, 0.0f),
          lightNearFarAngleIntensity = Array[Float](near, far, 0.0f, l.intensity.toFloat)
        )

      case l: SpotLight =>
        val useFarCuttOff: Float =
          l.falloff match {
            case Falloff.None(_, far)      => if (far.isDefined) 1.0f else 0.0f
            case Falloff.Linear(_, far)    => if (far.isDefined) 1.0f else 0.0f
            case Falloff.Quadratic(_, far) => if (far.isDefined) 1.0f else 0.0f
            case _                         => 1.0f
          }

        val falloffType: Float =
          l.falloff match {
            case _: Falloff.None            => 0.0f
            case _: Falloff.SmoothLinear    => 1.0f
            case _: Falloff.SmoothQuadratic => 2.0f
            case _: Falloff.Linear          => 3.0f
            case _: Falloff.Quadratic       => 4.0f
          }

        val near: Float =
          l.falloff match {
            case Falloff.None(near, _)            => near.toFloat
            case Falloff.SmoothLinear(near, _)    => near.toFloat
            case Falloff.SmoothQuadratic(near, _) => near.toFloat
            case Falloff.Linear(near, _)          => near.toFloat
            case Falloff.Quadratic(near, _)       => near.toFloat
          }

        val far: Float =
          l.falloff match {
            case Falloff.None(_, far)            => far.map(_.toFloat).getOrElse(10000.0f)
            case Falloff.SmoothLinear(_, far)    => far.toFloat
            case Falloff.SmoothQuadratic(_, far) => far.toFloat
            case Falloff.Linear(_, far)          => far.map(_.toFloat).getOrElse(10000.0f)
            case Falloff.Quadratic(_, far)       => far.map(_.toFloat).getOrElse(10000.0f)
          }

        LightData(
          lightFlags = Array[Float](1.0f, 3.0f, useFarCuttOff, falloffType),
          lightColor = Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular =
            Array[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specular.a.toFloat),
          lightPositionRotation = Array[Float](l.position.x.toFloat, l.position.y.toFloat, l.rotation.toFloat, 0.0f),
          lightNearFarAngleIntensity = Array[Float](near, far, l.angle.toFloat, l.intensity.toFloat)
        )
    }

  def mergeShaderToUniformData(
      shaderData: BlendShaderData
  )(using QuickCache[Array[Float]]): List[DisplayObjectUniformData] =
    shaderData.uniformBlocks.map { ub =>
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
    lightNearFarAngleIntensity: Array[Float]
) derives CanEqual {
  def +(other: LightData): LightData =
    this.copy(
      lightFlags = lightFlags ++ other.lightFlags,
      lightColor = lightColor ++ other.lightColor,
      lightSpecular = lightSpecular ++ other.lightSpecular,
      lightPositionRotation = lightPositionRotation ++ other.lightPositionRotation,
      lightNearFarAngleIntensity = lightNearFarAngleIntensity ++ other.lightNearFarAngleIntensity
    )

  def toArray: Array[Float] =
    lightFlags ++
      lightColor ++
      lightSpecular ++
      lightPositionRotation ++
      lightNearFarAngleIntensity
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
