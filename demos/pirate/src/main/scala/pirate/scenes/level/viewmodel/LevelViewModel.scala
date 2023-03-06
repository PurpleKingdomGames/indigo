package pirate.scenes.level.viewmodel

import indigo.*
import indigoextras.geometry.Vertex
import pirate.scenes.level.model.Pirate

/*
The view model cannot be initialised at game start up, because we want to load
some data during the loading screen, parse it, and use it to build the
`worldToScreenSpace` function, which relies on knowing the size of the tiles
which is stored in the Tiled data.
 */
sealed trait LevelViewModel derives CanEqual:
  val notReady: Boolean

  def update(gameTime: GameTime, pirate: Pirate): Outcome[LevelViewModel]

object LevelViewModel:

  // The uninitialised ViewModel
  case object NotReady extends LevelViewModel:
    val notReady: Boolean = true

    def update(gameTime: GameTime, pirate: Pirate): Outcome[LevelViewModel] =
      Outcome(this)

  // The initialised / useable ViewModel
  final case class Ready(worldToScreenSpace: Vertex => Point, pirateViewState: PirateViewState) extends LevelViewModel:
    val notReady: Boolean = false

    def update(gameTime: GameTime, pirate: Pirate): Outcome[LevelViewModel] =
      pirateViewState
        .update(gameTime, pirate)
        .map(ps =>
          this.copy(
            pirateViewState = ps
          )
        )
