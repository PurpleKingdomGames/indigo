package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import indigo.ShaderPrimitive._
// import com.example.sandbox.SandboxAssets

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
          Circle(Point(50, 50), 20, ShapeMaterial(RGBA.Red, RGBA.White, 3, false, true).lineOutside),
          Circle(Point(100, 50), 20, ShapeMaterial(RGBA.Green, RGBA.White, 4, false, true).lineInside),
          Circle(Point(50, 75), 10, ShapeMaterial(RGBA.Blue, RGBA.Yellow, 10, false, false)),
          Circle(Point(100), 15, ShapeMaterial(RGBA.Magenta, RGBA.White, 2, false, true)),
          Circle(Point(30, 75), 15, ShapeMaterial(RGBA.Cyan, RGBA.White, 0, false, false)),
          Circle(Point(150), 50, ShapeMaterial(RGBA.Yellow, RGBA.Black, 7, false, true))
        )
    )

}

//,
// Foo(),
// Graphic(
//   32,
//   32,
//   StandardMaterial.PostMaterial(
//     ShapeShaders.postShader.id,
//     StandardMaterial.ImageEffects(SandboxAssets.dots)
//   )
// )
// Shape(
//   0,
//   0,
//   64,
//   64,
//   1,
//   GLSLShader(
//     ShapeShaders.externalCircleId,
//     List(
//       Uniform("ALPHA")        -> float(0.75),
//       Uniform("BORDER_COLOR") -> vec3(1.0, 1.0, 0.0)
//     )
//   )
// ).moveTo(context.startUpData.viewportCenter - Point(32, 32))

final case class Circle(
    center: Point,
    radius: Int,
    depth: Depth,
    material: ShapeMaterial
) extends SceneEntity {

  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
  val flip: Flip        = Flip.default
  val ref: Point        = Point.zero

  def position: Point =
    center - radius - (if (material.lineIsInside) 0 else material.lineWidth) - 1

  def bounds: Rectangle =
    Rectangle(
      position,
      Point(radius * 2) + (if (material.lineIsInside) 0 else material.lineWidth * 2) + 2
    )
}
object Circle {

  def apply(center: Point, radius: Int, material: ShapeMaterial): Circle =
    Circle(
      center,
      radius,
      Depth(1),
      material
    )

}

final case class ShapeMaterial(fill: RGBA, line: RGBA, lineWidth: Int, lineIsInside: Boolean, useAntiAliasing: Boolean) extends Material {

  def withFillColor(newFill: RGBA): ShapeMaterial =
    this.copy(fill = newFill)

  def withLineColor(newLine: RGBA): ShapeMaterial =
    this.copy(line = newLine)

  def withLineWidth(newWidth: Int): ShapeMaterial =
    this.copy(lineWidth = newWidth)

  def withLineInside(isInside: Boolean): ShapeMaterial =
    this.copy(lineIsInside = isInside)

  def lineInside: ShapeMaterial =
    this.copy(lineIsInside = true)

  def lineOutside: ShapeMaterial =
    this.copy(lineIsInside = false)

  def withAntiAliasing(smoothEdges: Boolean): ShapeMaterial =
    this.copy(useAntiAliasing = smoothEdges)

  def smooth: ShapeMaterial =
    withAntiAliasing(true)

  def sharp: ShapeMaterial =
    withAntiAliasing(false)

  val hash: String =
    "shape" + fill.hash + line.hash + lineWidth.toString() + useAntiAliasing.toString()

  def toGLSLShader: GLSLShader =
    GLSLShader(
      ShapeShaders.circleId,
      List(
        Uniform("BORDER_WIDTH") -> float(lineWidth.toDouble),
        Uniform("SMOOTH")       -> float(if (useAntiAliasing) 1.0 else 0.0),
        Uniform("BORDER_COLOR") -> vec4(fill.r, fill.g, fill.b, fill.a),
        Uniform("FILL_COLOR")   -> vec4(line.r, line.g, line.b, line.a)
      )
    )
}
object ShapeMaterial {

  def apply(fill: RGBA): ShapeMaterial =
    ShapeMaterial(fill, RGBA.Zero, 0, false, false)

  def apply(fill: RGBA, line: RGBA, lineWidth: Int): ShapeMaterial =
    ShapeMaterial(fill, line, lineWidth, false, false)

}

final case class Foo() extends SceneEntity {
  val bounds: Rectangle = Rectangle(10, 10, 100, 100)
  val material: Material = GLSLShader(
    ShapeShaders.circleId,
    List(
      Uniform("ALPHA")        -> float(0.75),
      Uniform("BORDER_COLOR") -> vec3(1.0, 1.0, 0.0)
    )
  )
  val depth: Depth      = Depth(1)
  val flip: Flip        = Flip.default
  val position: Point   = bounds.position
  val ref: Point        = Point.zero
  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
}

object ShapeShaders {

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(circleAsset, AssetPath("assets/circle.frag")),
      AssetType.Text(postVertAsset, AssetPath("assets/post.vert")),
      AssetType.Text(postFragAsset, AssetPath("assets/post.frag"))
    )

  val circleId: ShaderId     = ShaderId("circle external")
  val circleAsset: AssetName = AssetName("circle fragment")
  val circleExternal: CustomShader.External =
    CustomShader
      .External(circleId)
      .withFragmentProgram(circleAsset)

  val postVertAsset: AssetName = AssetName("post vertex")
  val postFragAsset: AssetName = AssetName("post fragment")
  def postShader: CustomShader.PostExternal =
    CustomShader
      .PostExternal(ShaderId("post shader test"), StandardShaders.Bitmap)
      .withPostVertexProgram(postVertAsset)
      .withPostFragmentProgram(postFragAsset)

}
