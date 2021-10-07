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
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

object ConfettiScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  val spawnCount: Int = 600

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

  def updateModel(
      context: FrameContext[SandboxStartupData],
      model: ConfettiModel
  ): GlobalEvent => Outcome[ConfettiModel] =

    case FrameTick =>
      val pos = Signal.Orbit(context.startUpData.viewportCenter * 2, 100).at(context.running * 0.5).toPoint
      Outcome(
        model
          .spawn(
            context.dice,
            pos.x,
            pos.y,
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
      Array[Int](0, 0, 16, 16),
      Array[Int](16, 0, 16, 16),
      Array[Int](0, 16, 16, 16),
      Array[Int](16, 16, 16, 16)
    )

  val count: TextBox =
    TextBox("", 228, 20).alignCenter
      .withFontSize(Pixels(12))
      .withColor(RGBA.White)

  def particlesToCloneTiles(particles: Array[Particle]): CloneTiles =
    CloneTiles(
      cloneId,
      particles.map { p =>
        val crop = crops(p.color)
        CloneTileData(p.x, p.y, Radians.zero, p.scale, p.scale, crop(0), crop(1), crop(2), crop(3))
      }
    )

  def present(
      context: FrameContext[SandboxStartupData],
      model: ConfettiModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          model.particles.map(particlesToCloneTiles).toList
        ).withMagnification(1),
        Layer(
          count.withText(s"count: ${model.particles.length * spawnCount}")
        )
      ).addCloneBlanks(cloneBlanks)
    )

final case class ConfettiModel(color: Int, particles: Array[Array[Particle]]):
  def spawn(dice: Dice, x: Int, y: Int, count: Int): ConfettiModel =
    this.copy(
      particles = Array((0 until count).toArray.map { _ =>
        Particle(
          x,
          y,
          dice.rollFloat * 2.0f - 1.0f,
          dice.rollFloat * 2.0f,
          color,
          (dice.rollFloat * 0.5f + 0.5f) * 0.25f
        )
      }) ++ particles
    )

  def update: ConfettiModel =
    this.copy(
      color = (color + 1) % 4,
      particles = particles
        .map {
          _.filter(p => p.y < 400).map { p =>
            val newFy = p.fy - 0.1f
            val newFx = p.fx * 0.95f
            p.copy(
              x = p.x + (15 * newFx).toInt,
              y = p.y - (5 * newFy).toInt,
              fx = newFx,
              fy = newFy
            )
          }
        }
        .filter(_.nonEmpty)
    )

object ConfettiModel:
  val empty: ConfettiModel =
    ConfettiModel(0, Array())

final case class Particle(x: Int, y: Int, fx: Float, fy: Float, color: Int, scale: Float)
object Particle:
  given CanEqual[Option[Particle], Option[Particle]] = CanEqual.derived
  given CanEqual[List[Particle], List[Particle]]     = CanEqual.derived
