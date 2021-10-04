package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import indigoextras.ui.HitArea

import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex
import com.example.sandbox.Log
import com.example.sandbox.SandboxAssets

object ConfettiScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = ConfettiModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: indigo.scenes.Lens[SandboxGameModel, ConfettiModel] =
    Lens(_.confetti, (m, c) => m.copy(confetti = c))

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("confetti")

  def subSystems: Set[SubSystem] =
    Set()

  val spawnCount: Int = 50

  def updateModel(
      context: FrameContext[SandboxStartupData],
      model: ConfettiModel
  ): GlobalEvent => Outcome[ConfettiModel] =

    case FrameTick =>
      val pos = Signal.Orbit(context.startUpData.viewportCenter * 2, 100).at(context.running * 0.5)
      Outcome(
        model
          .spawn(
            context.dice,
            pos.x.toFloat,
            pos.y.toFloat,
            spawnCount
          )
          .update
      )

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: FrameContext[SandboxStartupData],
      model: ConfettiModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val cloneId: CloneId = CloneId("dots")

  val cloneBlanks: List[CloneBlank] =
    List(CloneBlank(cloneId, SandboxAssets.colouredDots).static)

  val crops =
    Array(
      Array[Float](0, 0, 16, 16),
      Array[Float](16, 0, 16, 16),
      Array[Float](0, 16, 16, 16),
      Array[Float](16, 16, 16, 16)
    )

  val count: TextBox =
    TextBox("", 456, 20).alignCenter
      .withFontSize(Pixels(12))
      .withColor(RGBA.White)

  def present(
      context: FrameContext[SandboxStartupData],
      model: ConfettiModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    val tiles: List[CloneTiles] =
      model.particles match
        case Nil =>
          Nil

        case p :: ps =>
          List(
            CloneTiles(
              cloneId,
              ps.foldLeft(
                CloneTileData.unsafe(Array[Float](p.x, p.y, 0.0f, p.scale, p.scale) ++ crops(p.color))
              ) { (pa, pb) =>
                pa ++ CloneTileData.unsafe(Array(pb.x, pb.y, 0.0f, pb.scale, pb.scale) ++ crops(pb.color))
              }
            )
          )

    Outcome(
      SceneUpdateFragment(
        Layer(
          tiles ++ List(count.withText(s"count: ${model.particles.length}"))
        ).withMagnification(1)
      ).addCloneBlanks(cloneBlanks)
    )

final case class ConfettiModel(color: Int, particles: List[Particle]):
  def spawn(dice: Dice, x: Float, y: Float, count: Int): ConfettiModel =
    this.copy(
      particles = (0 until count).toList.map { _ =>
        Particle(
          x,
          y,
          dice.rollFloat * 2.0f - 1.0f,
          dice.rollFloat * 2.0f,
          color,
          (dice.rollFloat * 0.5f + 0.5f) * 0.5f
        )
      } ++ particles
    )

  def update: ConfettiModel =
    this.copy(
      color = (color + 1) % 4,
      particles = particles.filter(p => p.y < 500).map { p =>
        val newFy = p.fy - 0.1f
        val newFx = p.fx * 0.95f
        p.copy(
          x = p.x + (15.0f * newFx),
          y = p.y - (5.0f * newFy),
          fx = newFx,
          fy = newFy
        )
      }
    )

object ConfettiModel:
  val empty: ConfettiModel =
    ConfettiModel(0, Nil)

final case class Particle(x: Float, y: Float, fx: Float, fy: Float, color: Int, scale: Float)
object Particle:
  given CanEqual[Option[Particle], Option[Particle]] = CanEqual.derived
  given CanEqual[List[Particle], List[Particle]]     = CanEqual.derived
