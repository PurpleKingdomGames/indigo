package com.example.sandbox.scenes

import com.example.sandbox.*
import indigo.*
import indigo.scenes.*
import indigoextras.pathfinding.*
import indigoextras.pathfinding.PathBuilder.*
import indigoextras.pathfinding.PathBuilder.Movements.*

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

final case class PathFindingModel(
    // will represent a weighted grid
    data: js.Array[Int] = js.Array()
)

object PathFindingModel:
  val empty: PathFindingModel = PathFindingModel()

object PathFindingScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = PathFindingModel
  type SceneViewModel = SandboxViewModel

  val increase: Int        = 30
  val gridSize: Int        = 10
  val gridDisplaySize: Int = 20

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SceneModel] =
    Lens(_.pathfinding, (m, pm) => m.copy(pathfinding = pm))

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("pathfinding")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] =

    case FrameTick =>
      if (model.data.isEmpty)
        Outcome(
          PathFindingModel(data = List.fill(gridSize * gridSize)(0).toJSArray) // initialise the grid with 0s
        )
      else
        // increase randomly the value at a random point
        val n = context.frame.dice.roll(model.data.length) - 1
        val v = (model.data(n) + context.frame.dice.roll(increase)) % 256
        model.data.update(n, v)
        Outcome(model)

    case _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SceneModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SceneModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =

    // in practice we should not have to compute the path every frame
    val start = Point(0, 0)                       // top left
    val end   = Point(gridSize - 1, gridSize - 1) // bottom right
    val pathBuilder =
      PathBuilder.fromWeightedGrid(
        grid = Batch(model.data),
        width = gridSize,
        height = gridSize,
        allowedMovements = All,
        directSideCost = DefaultSideCost,
        diagonalCost = DefaultDiagonalCost,
        maxHeuristicFactor = DefaultMaxHeuristicFactor
      )
    val path = PathFinder.findPath(start, end, pathBuilder).getOrElse(Batch.empty) // if no path found, return empty

    Outcome(
      SceneUpdateFragment(
        Batch.combineAll(
          (for {
            y <- 0 until gridSize
            x <- 0 until gridSize
            c = model.data(y * gridSize + x)
          } yield Batch(
            Shape.Box(
              Rectangle(Point(x * gridDisplaySize, y * gridDisplaySize), Size(gridDisplaySize, gridDisplaySize)),
              Fill.Color(
                // if the point is in the path, color it red, otherwise color it with the value of the grid as a shade of grey
                if (path.contains(Point(x, y))) RGBA.Red
                else RGBA.fromColorInts(c, c, c)
              )
            )
          ))*
        )
      )
    )
