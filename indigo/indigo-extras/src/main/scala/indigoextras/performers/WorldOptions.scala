package indigoextras.performers

import indigo.physics.*
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Vector2

/** Represents the options for the physics world.
  */
final case class WorldOptions(
    forces: Batch[Vector2],
    resistance: Resistance,
    settings: SimulationSettings
):

  def withForces(newForces: Batch[Vector2]): WorldOptions =
    this.copy(forces = newForces)
  def withForces(newForces: Vector2*): WorldOptions =
    withForces(Batch.fromSeq(newForces))

  def addForces(additionalForces: Batch[Vector2]): WorldOptions =
    this.copy(forces = forces ++ additionalForces)
  def addForces(additionalForces: Vector2*): WorldOptions =
    addForces(Batch.fromSeq(additionalForces))

  def withResistance(newResistance: Resistance): WorldOptions =
    this.copy(resistance = newResistance)

  def withSimulationSettings(newSettings: SimulationSettings): WorldOptions =
    this.copy(settings = newSettings)

object WorldOptions:

  def default: WorldOptions =
    WorldOptions(Batch(), Resistance.zero, SimulationSettings.default)
