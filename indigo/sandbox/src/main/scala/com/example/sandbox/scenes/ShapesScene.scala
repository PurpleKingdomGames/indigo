package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import indigo.ShaderPrimitive._

object ShapesScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("shapes")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
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
          )
        )
    )

}

object ShapeShaders {

  val externalCircleId: ShaderId =
    ShaderId("circle external")

  val vertAsset: AssetName = AssetName("circle vertex")
  val fragAsset: AssetName = AssetName("circle fragment")

  val circleExternal: CustomShader.External =
    CustomShader
      .External(externalCircleId)
      .withVertexProgram(vertAsset)
      .withFragmentProgram(fragAsset)
      .withLightProgram(fragAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(vertAsset, AssetPath("assets/circle.vert")),
      AssetType.Text(fragAsset, AssetPath("assets/circle.frag"))
    )

}
