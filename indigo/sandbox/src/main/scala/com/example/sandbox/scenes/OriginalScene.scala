package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import com.example.sandbox.SandboxView
import indigo.ShaderPrimitive._
import com.example.sandbox.SandboxAssets

object OriginalScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("original")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] = {
    val scene =
      SandboxView
        .updateView(model, viewModel, context.inputState)
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
            Shape(0, 0, 228 * 3, 140 * 3, 10, GLSLShader(Shaders.seaId))
          ).withKey(BindingKey("bg"))
            .withMagnification(1)
        )
        .addLayer(
          Layer(
            Graphic(120, 10, 32, 32, 1, SandboxAssets.dotsMaterial),
            Shape(140, 50, 32, 32, 1, GLSLShader(Shaders.circleId)),
            Shape(
              140,
              50,
              32,
              32,
              1,
              GLSLShader(
                Shaders.externalId,
                List(
                  Uniform("ALPHA")        -> float(0.75),
                  Uniform("BORDER_COLOR") -> vec3(1.0, 1.0, 0.0)
                )
              )
            ),
            Shape(
              150,
              60,
              32,
              32,
              1,
              GLSLShader(
                Shaders.externalId,
                List(
                  Uniform("ALPHA")        -> float(0.5),
                  Uniform("BORDER_COLOR") -> vec3(1.0, 0.0, 1.0)
                )
              )
            )
          )
        )
    )
  }

}

object Shaders {

  val circleId: ShaderId =
    ShaderId("circle")

  def circleVertex(orbitDist: Double): String =
    s"""
    |float timeToRadians(float t) {
    |  return TAU * mod(t, 1.0);
    |}
    |
    |void vertex() {
    |  float x = sin(timeToRadians(TIME / 2.0)) * ${orbitDist.toString()} + VERTEX.x;
    |  float y = cos(timeToRadians(TIME / 2.0)) * ${orbitDist.toString()} + VERTEX.y;
    |  vec2 orbit = vec2(x, y);
    |  VERTEX = vec4(orbit, VERTEX.zw);
    |}
    |""".stripMargin

  val circleFragment: String =
    """
    |float timeToRadians(float t) {
    |  return TAU * mod(t, 1.0);
    |}
    |
    |void fragment() {
    |  float red = UV.x * (1.0 - ((cos(timeToRadians(TIME)) + 1.0) / 2.0));
    |  float alpha = 1.0 - step(0.0, length(UV - 0.5) - 0.5);
    |  vec4 circle = vec4(vec3(red, UV.y, 0.0) * alpha, alpha);
    |  COLOR = circle;
    |}
    |""".stripMargin

  val circle: CustomShader.Source =
    CustomShader
      .Source(circleId)
      .withVertexProgram(circleVertex(0.5))
      .withFragmentProgram(circleFragment)

  val externalId: ShaderId =
    ShaderId("external")

  val vertAsset: AssetName = AssetName("vertex")
  val fragAsset: AssetName = AssetName("fragment")
  val seaAsset: AssetName  = AssetName("sea")

  val external: CustomShader.External =
    CustomShader
      .External(externalId)
      .withVertexProgram(vertAsset)
      .withFragmentProgram(fragAsset)
      .withLightProgram(fragAsset)

  val seaId: ShaderId =
    ShaderId("sea")

  val sea: CustomShader.External =
    CustomShader
      .External(seaId)
      .withFragmentProgram(seaAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(vertAsset, AssetPath("assets/shader.vert")),
      AssetType.Text(fragAsset, AssetPath("assets/shader.frag")),
      AssetType.Text(seaAsset, AssetPath("assets/sea.frag"))
    )

}
