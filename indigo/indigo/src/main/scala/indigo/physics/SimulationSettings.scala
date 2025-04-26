package indigo.physics

import indigo.BoundingBox

/** SimulationSettings are used to tune the Physics simulation so that it is working at the right scale for your use
  * case, minimising expensive comparisons and allowing suitable culling to occur.
  *
  * @param bounds
  *   The area of the simulation, anything that goes beyond is assumed to have 'escaped' and is culled / ignored.
  * @param idealCount
  *   The number of fine grain object comparisons you'd prefer to make at most. Actually comparisons make exceed this
  *   number. Defaults to 16.
  * @param minSize
  *   The smallest sensible area to store objects against. As a guide: There is probably little point having a minSize
  *   much smaller than your smallest collider. Defaults to 1.
  * @param maxDepth
  *   The maximum depth the spatial tree should go to. Defaults to 16.
  * @param maxIterations
  *   The maximum number of solver iterations to perform. Fewer iterations improves performance at the cost of accuracy.
  *   Defaults to 4.
  */
final case class SimulationSettings(
    bounds: Option[BoundingBox],
    idealCount: Int,
    minSize: Double,
    maxDepth: Int,
    maxIterations: Int
):

  def withBounds(value: BoundingBox): SimulationSettings =
    this.copy(bounds = Option(value))

  def withIdealCount(value: Int): SimulationSettings =
    this.copy(idealCount = value)

  def withMinSize(value: Int): SimulationSettings =
    this.copy(minSize = value)

  def withMaxDepth(value: Int): SimulationSettings =
    this.copy(maxDepth = value)

  def withMaxIterations(value: Int): SimulationSettings =
    this.copy(maxIterations = value)

object SimulationSettings:

  val DefaultIdealCount: Int    = 16
  val DefaultMinSize: Double    = 1
  val DefaultMaxDepth: Int      = 16
  val DefaultMaxIterations: Int = 4

  def default: SimulationSettings =
    SimulationSettings(None, DefaultIdealCount, DefaultMinSize, DefaultMaxDepth, DefaultMaxIterations)

  def apply(bounds: BoundingBox): SimulationSettings =
    SimulationSettings(Option(bounds), DefaultIdealCount, DefaultMinSize, DefaultMaxDepth, DefaultMaxIterations)

  def apply(bounds: BoundingBox, idealCount: Int): SimulationSettings =
    SimulationSettings(Option(bounds), idealCount, DefaultMinSize, DefaultMaxDepth, DefaultMaxIterations)

  def apply(bounds: BoundingBox, idealCount: Int, minSize: Double): SimulationSettings =
    SimulationSettings(Option(bounds), idealCount, minSize, DefaultMaxDepth, DefaultMaxIterations)

  def apply(bounds: BoundingBox, idealCount: Int, minSize: Double, maxDepth: Int): SimulationSettings =
    SimulationSettings(Option(bounds), idealCount, minSize, maxDepth, DefaultMaxIterations)
