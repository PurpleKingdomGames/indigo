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

  val spawnCount: Int = 100

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

  val cloneBlanks: List[CloneBlank] =
    List(
      CloneBlank(CloneId("r"), SandboxAssets.redDot).static,
      CloneBlank(CloneId("g"), SandboxAssets.greenDot).static,
      CloneBlank(CloneId("b"), SandboxAssets.blueDot).static,
      CloneBlank(CloneId("y"), SandboxAssets.yellowDot).static
    )

  val dots: List[CloneId] =
    List(
      CloneId("r"),
      CloneId("g"),
      CloneId("b"),
      CloneId("y")
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
    Outcome(
      SceneUpdateFragment(
        Layer(
          model.particles.flatMap { particles =>
            val res =
              particles match
                case Nil =>
                  Nil

                case p :: ps =>
                  List(
                    CloneBatch(
                      dots(p.color),
                      ps.foldLeft(CloneTransformData(p.x, p.y, Radians.zero, p.scale, p.scale)) { (pa, pb) =>
                        pa ++ CloneTransformData(pb.x, pb.y, Radians.zero, pb.scale, pb.scale)
                      }
                    )
                  )
            res
          } ++ List(
            count.withText(s"count: ${model.particles.flatten.length}")
          )
        ).withMagnification(1)
      ).addCloneBlanks(cloneBlanks)
    )

opaque type ConfettiModel = (Int, List[List[Particle]])
object ConfettiModel:
  val empty: ConfettiModel = (0, Nil)

  extension (m: ConfettiModel)
    inline def color: Int                      = m._1
    inline def particles: List[List[Particle]] = m._2

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
        } :: m.particles
      )

    def update: ConfettiModel =
      (
        (m.color + 1) % 4,
        m.particles.map {
          _.filter(p => p.y < 500)
            .map { p =>
              val newFy = p.fy - 0.1
              val newFx = p.fx * 0.95
              p.copy(
                x = (p.x + (15 * newFx)).toInt,
                y = (p.y - (5 * newFy)).toInt,
                fx = newFx,
                fy = newFy
              )
            }
        }
      )

final case class Particle(x: Int, y: Int, fx: Double, fy: Double, color: Int, scale: Double)
object Particle:
  given CanEqual[Option[Particle], Option[Particle]] = CanEqual.derived
  given CanEqual[List[Particle], List[Particle]]     = CanEqual.derived
