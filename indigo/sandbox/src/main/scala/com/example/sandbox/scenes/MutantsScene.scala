package com.example.sandbox.scenes

import com.example.sandbox.Log
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGame
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo._
import indigo.scenes._
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex
import indigoextras.ui.HitArea

object MutantsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepLatest

  def name: SceneName =
    SceneName("mutants")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    case _ =>
      Outcome(viewModel)

  val cloneId    = CloneId("mutant")
  val cloneBlank = CloneBlank(cloneId, Archetype())

  // A pretty mutant data set
  val data: Array[List[UniformBlock]] =
    (0 until 100).toArray.map { i =>
      val d  = Dice.fromSeed(i)
      val pt = Point(d.rollFromZero(SandboxGame.gameWidth), d.rollFromZero(SandboxGame.gameHeight))
      val sc = Vector2(0.3d + (d.rollDouble * 3.0d))
      val a  = 0.1d + (0.8d * d.rollDouble)
      Archetype.makeUniformBlock(pt, sc, a)
    }

  // A large mutant data set (60 fps on my machine)
  val dataMax: Array[List[UniformBlock]] =
    (0 until 2100).toArray.map { i =>
      val d  = Dice.fromSeed(i)
      val pt = Point(d.rollFromZero(SandboxGame.gameWidth), d.rollFromZero(SandboxGame.gameHeight))
      val sc = Vector2(0.3d + (d.rollDouble * 3.0d))
      val a  = 0.1d + (0.8d * d.rollDouble)
      Archetype.makeUniformBlock(pt, sc, a)
    }

  // Equivalent to dataMax using standard primitives - 1/7 the volume! (60 fps on my machine)
  val gfx: List[Graphic[Material.ImageEffects]] =
    (0 until 300).toList.map { i =>
      val d  = Dice.fromSeed(i)
      val pt = Point(d.rollFromZero(SandboxGame.gameWidth), d.rollFromZero(SandboxGame.gameHeight))
      val sc = Vector2(0.3d + (d.rollDouble * 3.0d))
      val a  = 0.1d + (0.8d * d.rollDouble)
      SandboxAssets.blueDot
        .moveTo(pt)
        .withRef(Point.zero)
        .scaleBy(sc)
        .modifyMaterial(m => Material.ImageEffects(m.diffuse).withAlpha(a))
    }

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Mutants(cloneId, data)
        // Mutants(cloneId, dataMax)
        // gfx
      ).addCloneBlanks(cloneBlank)
    )

final case class Archetype() extends EntityNode with Cloneable:
  val position: Point                       = Point.zero
  val rotation: Radians                     = Radians.zero
  val scale: Vector2                        = Vector2.one
  val depth: Depth                          = Depth.one
  val flip: Flip                            = Flip.default
  val ref: Point                            = Point.zero
  val size: Size                            = Size(16)
  def withDepth(newDepth: Depth): Archetype = this

  def toShaderData: ShaderData =
    ShaderData(Archetype.shaderId)
      .withChannel0(SandboxAssets.dots)
      .withUniformBlocks(Archetype.makeUniformBlock(position, scale, 1.0d))

object Archetype:

  val vertAsset: AssetName = AssetName("mutant vertex")
  val fragAsset: AssetName = AssetName("mutant fragment")

  val shaderId =
    ShaderId("archetype")

  val shader =
    EntityShader
      .External(shaderId)
      .withVertexProgram(vertAsset)
      .withFragmentProgram(fragAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(vertAsset, AssetPath("assets/mutant.vert")),
      AssetType.Text(fragAsset, AssetPath("assets/mutant.frag"))
    )

  def makeUniformBlock(position: Point, scale: Vector2, alpha: Double): List[UniformBlock] =
    List(
      UniformBlock(
        "MutantData",
        List(
          Uniform("MOVE_TO")  -> vec2.fromPoint(position),
          Uniform("SCALE_TO") -> vec2.fromVector2(scale),
          Uniform("ALPHA")    -> float(alpha)
        )
      )
    )
