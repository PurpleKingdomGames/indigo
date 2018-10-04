package indigoexts.pathfinding

/**
  * This is very crude and inefficient, but should be ok for the Snake use case,
  * in that:
  *
  * 1) Snake has small grids, so the amount of the grid to search requires no optimisation.
  * 2) Snake does not do diagonals, so scoring does not expand diagonally either.
  * 3) Snake does not have different types of terrain, so there is no weighting
  */
object PathFinding {

  //TODO

}
