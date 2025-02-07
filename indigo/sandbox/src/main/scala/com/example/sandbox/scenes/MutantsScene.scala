package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGame
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*

object MutantsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepLatest

  def name: SceneName =
    SceneName("mutants")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
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
    case _ =>
      Outcome(viewModel)

  val cloneId    = CloneId("mutant")
  val cloneBlank = CloneBlank(cloneId, Archetype())

  // A pretty mutant data set
  val data: Array[Batch[MutantData]] =
    0.until(100).toArray.map { i =>
      val d  = Dice.fromSeed(i)
      val pt = Point(d.rollFromZero(SandboxGame.gameWidth), d.rollFromZero(SandboxGame.gameHeight))
      val sc = Vector2(0.3d + (d.rollDouble * 3.0d))
      val a  = 0.1d + (0.8d * d.rollDouble)

      Batch(MutantData(pt, sc, a))
    }

  // A large mutant data set (60 fps on my machine)
  val dataMax: Array[Batch[MutantData]] =
    0.until(3500).toArray.map { i =>
      val d  = Dice.fromSeed(i)
      val pt = Point(d.rollFromZero(SandboxGame.gameWidth), d.rollFromZero(SandboxGame.gameHeight))
      val sc = Vector2(0.3d + (d.rollDouble * 3.0d))
      val a  = 0.1d + (0.8d * d.rollDouble)

      Batch(MutantData(pt, sc, a))
    }

  // Equivalent to dataMax using standard primitives - 1/7 the volume! (60 fps on my machine)
  val gfx: Batch[Graphic[Material.ImageEffects]] =
    0.until(300).toBatch.map { i =>
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
      context: SceneContext[SandboxStartupData],
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

final case class Archetype() extends EntityNode[Archetype] with Cloneable:
  val position: Point   = Point.zero
  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
  val flip: Flip        = Flip.default
  val ref: Point        = Point.zero
  val size: Size        = Size(16)

  lazy val toShaderData: ShaderData =
    ShaderData(Archetype.shaderId)
      .withChannel0(SandboxAssets.dots)
      .addUniformData(MutantData(position, scale, 1.0d))

  val eventHandlerEnabled: Boolean                                    = false
  def eventHandler: ((Archetype, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

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

final case class MutantData(
    position: Point,
    scale: Vector2,
    alpha: Double
) derives ToUniformBlock
