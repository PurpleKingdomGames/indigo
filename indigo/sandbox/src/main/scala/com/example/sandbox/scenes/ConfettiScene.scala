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
      Outcome(
        model
          .spawn(
            context.dice,
            Signal.Orbit(context.startUpData.viewportCenter * 2, 100).at(context.running * 0.5).toPoint,
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
          val crop1 = crops(p.color)
          List(
            CloneTiles(
              cloneId,
              ps.foldLeft(
                CloneTileData(p.x, p.y, Radians.zero, p.scale, p.scale, crop1(0), crop1(1), crop1(2), crop1(3))
              ) { (pa, pb) =>
                val crop2 = crops(pb.color)
                pa ++ CloneTileData(
                  pb.x,
                  pb.y,
                  Radians.zero,
                  pb.scale,
                  pb.scale,
                  crop2(0),
                  crop2(1),
                  crop2(2),
                  crop2(3)
                )
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

opaque type ConfettiModel = (Int, List[Particle])
object ConfettiModel:
  val empty: ConfettiModel = (0, Nil)

  extension (m: ConfettiModel)
    inline def color: Int                = m._1
    inline def particles: List[Particle] = m._2

    def spawn(dice: Dice, position: Point, count: Int): ConfettiModel =
      (
        m.color,
        (0 until count).toList.map { _ =>
          Particle(
            position.x,
            position.y,
            dice.rollDouble * 2.0 - 1.0,
            dice.rollDouble * 2.0,
            m.color,
            (dice.rollDouble * 0.5 + 0.5) * 0.5
          )
        } ++ m.particles
      )

    def update: ConfettiModel =
      (
        (m.color + 1) % 4,
        m.particles.filter(p => p.y < 500).map { p =>
          val newFy = p.fy - 0.1
          val newFx = p.fx * 0.95
          p.copy(
            x = (p.x + (15 * newFx)).toInt,
            y = (p.y - (5 * newFy)).toInt,
            fx = newFx,
            fy = newFy
          )
        }
      )

final case class Particle(x: Int, y: Int, fx: Double, fy: Double, color: Int, scale: Double)
object Particle:
  given CanEqual[Option[Particle], Option[Particle]] = CanEqual.derived
  given CanEqual[List[Particle], List[Particle]]     = CanEqual.derived
