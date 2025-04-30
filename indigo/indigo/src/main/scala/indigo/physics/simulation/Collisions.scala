package indigo.physics.simulation

import indigo.*
import indigo.physics.*

object Collisions:

  given [Tag]: CanEqual[IndexedCollider[Tag], IndexedCollider[Tag]] = CanEqual.derived

  def findCollisionGroups[Tag](
      indexedColliders: Batch[IndexedCollider[Tag]],
      transient: Batch[Collider[Tag]],
      settings: SimulationSettings
  ): Batch[(IndexedCollider[Tag], Batch[Collider[Tag]])] =
    val bounds =
      settings.bounds
        .getOrElse(
          indexedColliders
            .map(_.movementBounds)
            .foldLeft(BoundingBox.zero)(_.expandToInclude(_))
        )

    val lookup: QuadTree[BoundingBox, IndexedCollider[Tag]] =
      QuadTree
        .empty(bounds)
        .insert(
          combineAndCull(indexedColliders, transient, bounds).map(m => m.movementBounds -> m),
          settings.idealCount,
          settings.minSize,
          settings.maxDepth
        )

    indexedColliders.map { c =>
      if c.proposed.isStatic then c -> Batch()
      else
        val collisions =
          lookup
            .searchByBoundingBox(c.movementBounds)
            .map(_.value)
            .distinctBy(_.index)
            .filter { i =>
              i.index != c.index &&
              c.proposed.canCollideWith(i.proposed.tag) &&
              c.proposed.hitTest(i.proposed)
            }
            .map(_.proposed)

        c -> collisions
    }

  def combineAndCull[Tag](
      indexedColliders: Batch[IndexedCollider[Tag]],
      transient: Batch[Collider[Tag]],
      simulationBounds: BoundingBox
  ): Batch[IndexedCollider[Tag]] =
    (
      indexedColliders ++
        transient.zipWithIndex.map { case (t, i) => IndexedCollider(-i - 1, t, t) }
    ).filter(p => p.proposed.boundingBox.overlaps(simulationBounds))
