package indigo.shared.platform

import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister
import indigo.shared.QuickCache
import indigo.shared.config.RenderingTechnology
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.RGBA
import indigo.shared.display.DisplayLayer
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayObjectUniformData
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.BlendMaterial
import indigo.shared.materials.BlendShaderData
import indigo.shared.platform.AssetMapping
import indigo.shared.platform.ProcessedSceneData
import indigo.shared.scenegraph.AmbientLight
import indigo.shared.scenegraph.Blending
import indigo.shared.scenegraph.CloneBlank
import indigo.shared.scenegraph.CloneId
import indigo.shared.scenegraph.DirectionLight
import indigo.shared.scenegraph.EntityNode
import indigo.shared.scenegraph.Falloff
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Light
import indigo.shared.scenegraph.PointLight
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.SpotLight
import indigo.shared.scenegraph.Sprite
import indigo.shared.time.GameTime

import scala.collection.immutable.HashMap
import scala.scalajs.js.JSConverters._

final class SceneProcessor(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
) {
  private val displayObjectConverter: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)
  private val displayObjectConverterClone: DisplayObjectConversions =
    new DisplayObjectConversions(boundaryLocator, animationsRegister, fontRegister)

  implicit private val uniformsCache: QuickCache[scalajs.js.Array[Float]]  = QuickCache.empty
  implicit private val staticCloneCache: QuickCache[Option[DisplayObject]] = QuickCache.empty

  // Called on asset load/reload to account for atlas rebuilding etc.
  def purgeCaches(): Unit = {
    displayObjectConverter.purgeCaches()
    displayObjectConverterClone.purgeCaches()
    uniformsCache.purgeAllNow()
  }

  def processScene(
      gameTime: GameTime,
      scene: SceneUpdateFragment,
      assetMapping: AssetMapping,
      renderingTechnology: RenderingTechnology,
      maxBatchSize: Int,
      inputEvents: => scalajs.js.Array[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  ): ProcessedSceneData = {

    def cloneBlankToDisplayObject(blank: CloneBlank): Option[DisplayObject] =
      blank.cloneable() match
        case s: Shape[_] =>
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

        case e: EntityNode[_] =>
          Some(displayObjectConverterClone.sceneEntityToDisplayObject(e, assetMapping))

        case _ =>
          None

    val cloneBlankDisplayObjects: HashMap[CloneId, DisplayObject] =
      scene.cloneBlanks.foldLeft(HashMap.empty[CloneId, DisplayObject]) { (acc, blank) =>
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

    val displayLayers: scalajs.js.Array[(DisplayLayer, scalajs.js.Array[(CloneId, DisplayObject)])] =
      scene.layers.toJSArray
        .filter(l => l.visible.getOrElse(true))
        .zipWithIndex
        .map { case (l, i) =>
          val blending   = l.blending.getOrElse(Blending.Normal)
          val shaderData = blending.blendMaterial.toShaderData

          val conversionResults = displayObjectConverter
            .processSceneNodes(
              l.nodes.toJSArray,
              gameTime,
              assetMapping,
              cloneBlankDisplayObjects,
              renderingTechnology,
              maxBatchSize,
              inputEvents,
              sendEvent
            )

          val layer = DisplayLayer(
            conversionResults._1,
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

          (layer, conversionResults._2)
        }
        .sortBy(_._1.depth.toInt)

    val sceneBlend = scene.blendMaterial.getOrElse(BlendMaterial.Normal).toShaderData

    new ProcessedSceneData(
      displayLayers.map(_._1),
      cloneBlankDisplayObjects.concat(displayLayers.flatMap(_._2)),
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
      scalajs.js.Array[Float](),
      scalajs.js.Array[Float](),
      scalajs.js.Array[Float](),
      scalajs.js.Array[Float](),
      scalajs.js.Array[Float]()
    )

  private val missingLightData: HashMap[Int, List[LightData]] =
    HashMap.from(
      (0 to 8).map { i =>
        (i -> List.fill(i)(LightData.empty))
      }
    )

  def makeLightsData(lights: List[Light]): scalajs.js.Array[Float] = {
    val limitedLights = lights.take(MaxLights)
    val count         = limitedLights.length
    val fullLights    = limitedLights.map(makeLightData) ++ missingLightData(MaxLights - count)

    scalajs.js.Array[Float](count.toFloat, 0.0f, 0.0f, 0.0f) ++ fullLights.foldLeft(bareLightData)(_ + _).toArray
  }

  def makeLightData(light: Light): LightData =
    light match {
      case l: AmbientLight =>
        LightData(
          lightFlags = scalajs.js.Array[Float](1.0f, 0.0f, 0.0f, 0.0f),
          lightColor =
            scalajs.js.Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular = scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
          lightPositionRotation = scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
          lightNearFarAngleIntensity = scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f)
        )

      case l: DirectionLight =>
        LightData(
          lightFlags = scalajs.js.Array[Float](1.0f, 1.0f, 0.0f, 0.0f),
          lightColor =
            scalajs.js.Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular = scalajs.js
            .Array[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specular.a.toFloat),
          lightPositionRotation = scalajs.js.Array[Float](0.0f, 0.0f, l.rotation.toFloat, 0.0f),
          lightNearFarAngleIntensity = scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f)
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
          lightFlags = scalajs.js.Array[Float](1.0f, 2.0f, useFarCuttOff, falloffType),
          lightColor =
            scalajs.js.Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular = scalajs.js
            .Array[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specular.a.toFloat),
          lightPositionRotation = scalajs.js.Array[Float](l.position.x.toFloat, l.position.y.toFloat, 0.0f, 0.0f),
          lightNearFarAngleIntensity = scalajs.js.Array[Float](near, far, 0.0f, l.intensity.toFloat)
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
          lightFlags = scalajs.js.Array[Float](1.0f, 3.0f, useFarCuttOff, falloffType),
          lightColor =
            scalajs.js.Array[Float](l.color.r.toFloat, l.color.g.toFloat, l.color.b.toFloat, l.color.a.toFloat),
          lightSpecular = scalajs.js
            .Array[Float](l.specular.r.toFloat, l.specular.g.toFloat, l.specular.b.toFloat, l.specular.a.toFloat),
          lightPositionRotation =
            scalajs.js.Array[Float](l.position.x.toFloat, l.position.y.toFloat, l.rotation.toFloat, 0.0f),
          lightNearFarAngleIntensity = scalajs.js.Array[Float](near, far, l.angle.toFloat, l.intensity.toFloat)
        )
    }

  def mergeShaderToUniformData(
      shaderData: BlendShaderData
  )(using QuickCache[scalajs.js.Array[Float]]): scalajs.js.Array[DisplayObjectUniformData] =
    shaderData.uniformBlocks.toJSArray.map { ub =>
      DisplayObjectUniformData(
        uniformHash = ub.uniformHash,
        blockName = ub.blockName,
        data = DisplayObjectConversions.packUBO(ub.uniforms)
      )
    }
}

final case class LightData(
    lightFlags: scalajs.js.Array[Float],
    lightColor: scalajs.js.Array[Float],
    lightSpecular: scalajs.js.Array[Float],
    lightPositionRotation: scalajs.js.Array[Float],
    lightNearFarAngleIntensity: scalajs.js.Array[Float]
) derives CanEqual {
  def +(other: LightData): LightData =
    this.copy(
      lightFlags = lightFlags ++ other.lightFlags,
      lightColor = lightColor ++ other.lightColor,
      lightSpecular = lightSpecular ++ other.lightSpecular,
      lightPositionRotation = lightPositionRotation ++ other.lightPositionRotation,
      lightNearFarAngleIntensity = lightNearFarAngleIntensity ++ other.lightNearFarAngleIntensity
    )

  def toArray: scalajs.js.Array[Float] =
    lightFlags ++
      lightColor ++
      lightSpecular ++
      lightPositionRotation ++
      lightNearFarAngleIntensity
}
object LightData {
  val empty: LightData =
    LightData(
      scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
      scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
      scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
      scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f),
      scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f)
    )

  val emptyData: scalajs.js.Array[Float] =
    empty.toArray
}
