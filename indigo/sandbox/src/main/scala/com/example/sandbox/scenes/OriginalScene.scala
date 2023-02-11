package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxView
import com.example.sandbox.SandboxViewModel
import indigo.ShaderPrimitive._
import indigo._
import indigo.scenes._

object OriginalScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("original")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] = {
    val scene: SceneUpdateFragment =
      SandboxView
        .updateView(model, viewModel, context.inputState.mouse, context.boundaryLocator)
        .addLayer(
          Layer(
            // viewModel.single.draw(gameTime, boundaryLocator) //|+|
            viewModel.multi.draw(context.gameTime, context.boundaryLocator)
          ).withDepth(Depth(1000))
        )

    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          Layer.empty
            .withKey(BindingKey("bg"))
            .withMagnification(1)
        ) |+| scene
        .addLayer(
          Layer(
            CustomShape(0, 0, 228 * 3, 140 * 3, Depth(10), ShaderData(Shaders.seaId))
          ).withKey(BindingKey("bg"))
        )
        .addLayer(
          Layer(
            Graphic(120, 10, 32, 32, 1, SandboxAssets.dotsMaterial),
            CustomShape(140, 50, 32, 32, Depth.zero, ShaderData(Shaders.circleId)),
            CustomShape(
              140,
              50,
              32,
              32,
              Depth.zero,
              ShaderData(
                Shaders.externalId,
                UniformBlock(
                  "CustomData",
                  Batch(
                    Uniform("ALPHA")        -> float(0.75),
                    Uniform("BORDER_COLOR") -> vec3(1.0, 1.0, 0.0)
                  )
                )
              )
            ),
            CustomShape(
              150,
              60,
              32,
              32,
              Depth.zero,
              ShaderData(
                Shaders.externalId,
                UniformBlock(
                  "CustomData",
                  Batch(
                    Uniform("ALPHA")        -> float(0.5),
                    Uniform("BORDER_COLOR") -> vec3(1.0, 0.0, 1.0)
                  )
                )
              )
            )
          )
        )
    )
  }

}

final case class CustomShape(x: Int, y: Int, width: Int, height: Int, depth: Depth, shader: ShaderData)
    extends EntityNode[CustomShape]:
  val flip: Flip                    = Flip.default
  val position: Point               = Point(x, y)
  val size: Size                    = Size(width, height)
  val ref: Point                    = Point.zero
  val rotation: Radians             = Radians.zero
  val scale: Vector2                = Vector2.one
  lazy val toShaderData: ShaderData = shader

  def withDepth(newDepth: Depth): CustomShape =
    this.copy(depth = newDepth)

  val eventHandlerEnabled: Boolean                                      = false
  def eventHandler: ((CustomShape, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

object Shaders:

  val circleId: ShaderId =
    ShaderId("circle")

  def circleVertex(orbitDist: Double): String =
    s"""
    |float timeToRadians(float t) {
    |  return TAU * mod(t, 1.0);
    |}
    |
    |vec4 vertex(vec4 v) {
    |  float x = sin(timeToRadians(TIME / 2.0)) * ${orbitDist.toString()} + VERTEX.x;
    |  float y = cos(timeToRadians(TIME / 2.0)) * ${orbitDist.toString()} + VERTEX.y;
    |  vec2 orbit = vec2(x, y);
    |  return vec4(orbit, VERTEX.zw);
    |}
    |""".stripMargin

  val circleFragment: String =
    """
    |float timeToRadians(float t) {
    |  return TAU * mod(t, 1.0);
    |}
    |
    |vec4 fragment(vec4 color) {
    |  float red = UV.x * (1.0 - ((cos(timeToRadians(TIME)) + 1.0) / 2.0));
    |  float alpha = 1.0 - step(0.0, length(UV - 0.5) - 0.5);
    |  vec4 circle = vec4(vec3(red, UV.y, 0.0) * alpha, alpha);
    |  return circle;
    |}
    |""".stripMargin

  val circle: EntityShader.Source =
    EntityShader
      .Source(circleId)
      .withVertexProgram(circleVertex(0.5))
      .withFragmentProgram(circleFragment)

  val externalId: ShaderId =
    ShaderId("external")

  val vertAsset: AssetName = AssetName("vertex")
  val fragAsset: AssetName = AssetName("fragment")
  val seaAsset: AssetName  = AssetName("sea")

  val external: EntityShader.External =
    EntityShader
      .External(externalId)
      .withVertexProgram(vertAsset)
      .withFragmentProgram(fragAsset)
      .withLightProgram(fragAsset)

  val seaId: ShaderId =
    ShaderId("sea")

  val sea: EntityShader.External =
    EntityShader
      .External(seaId)
      .withFragmentProgram(seaAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(vertAsset, AssetPath("assets/shader.vert")),
      AssetType.Text(fragAsset, AssetPath("assets/shader.frag")),
      AssetType.Text(seaAsset, AssetPath("assets/sea.frag"))
    )
