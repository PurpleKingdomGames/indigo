package indigo.physics.simulation

import indigo.*
import indigo.physics.Collider
import indigo.physics.SimulationSettings
import indigo.physics.World

import scala.annotation.tailrec

object Simulation:

  def updateWorld[Tag](
      world: World[Tag],
      timeDelta: Seconds,
      transient: Batch[Collider[Tag]],
      settings: SimulationSettings
  ): Outcome[World[Tag]] =
    val moved: Batch[IndexedCollider[Tag]] =
      Move.colliders(timeDelta, world.colliders, world.combinedForce, world.resistance)

    val staticTransient =
      transient.map(_.makeStatic)

    @tailrec
    def rec(
        remaining: Int,
        workingColliders: Batch[IndexedCollider[Tag]],
        accEvents: Batch[GlobalEvent]
    ): (Batch[IndexedCollider[Tag]], Batch[GlobalEvent]) =
      if remaining == 0 then (workingColliders, accEvents)
      else
        val collisions: Batch[(IndexedCollider[Tag], Batch[Collider[Tag]])] =
          Collisions.findCollisionGroups(workingColliders, staticTransient, settings)

        if collisions.exists(_._2.nonEmpty) then
          val collisionEvents: Batch[GlobalEvent] =
            collisions.flatMap { case (c, cs) =>
              cs.flatMap(target => c.proposed.onCollisionWith(target))
            }

          val resolved: Batch[IndexedCollider[Tag]] =
            Solver.solveAllCollisions(collisions)

          rec(remaining - 1, resolved, collisionEvents)
        else (workingColliders, accEvents)

    val (resolved, collisionEvents) =
      rec(settings.maxIterations, moved, Batch.empty)

    Outcome(world.copy(colliders = resolved.map(_.proposed)))
      .addGlobalEvents(collisionEvents)
