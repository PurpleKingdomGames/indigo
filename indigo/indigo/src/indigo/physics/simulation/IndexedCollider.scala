package indigo.physics.simulation

import indigo.physics.Collider
import indigo.shared.geometry.BoundingBox

final case class IndexedCollider[Tag](index: Int, previous: Collider[Tag], proposed: Collider[Tag]) derives CanEqual:

  val safeMove: Boolean = previous.boundingBox.overlaps(proposed.boundingBox)

  val movementBounds: BoundingBox =
    if safeMove then proposed.boundingBox
    else BoundingBox.expandToInclude(previous.boundingBox, proposed.boundingBox)
