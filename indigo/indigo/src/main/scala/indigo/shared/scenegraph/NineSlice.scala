package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.materials.ShaderData
import indigo.shared.shader.ShaderPrimitive.float
import indigo.shared.shader.StandardShaders
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock

final case class NineSlice[M <: Material](material: M) extends EntityNode[NineSlice[M]]:
  def withDepth(newDepth: Depth): NineSlice[M]                           = this
  def eventHandler: ((NineSlice[M], GlobalEvent)) => Option[GlobalEvent] = _ => None
  def eventHandlerEnabled: Boolean                                       = false
  def size: Size                                                         = Size(128, 64)
  def depth: Depth                                                       = Depth.zero
  def flip: Flip                                                         = Flip.default
  def position: Point                                                    = Point.zero
  def ref: Point                                                         = Point.zero
  def rotation: Radians                                                  = Radians.zero
  def scale: Vector2                                                     = Vector2.one

  lazy val toShaderData: ShaderData =
    val data = material.toShaderData
    data
      .withShaderId(StandardShaders.shaderIdToNineSliceShaderId(data.shaderId))
      .addUniformBlock(
        UniformBlock(
          "IndigoNineSliceData",
          Batch(
            Uniform("SOME_VALUE") -> float(1.0)
          )
        )
      )