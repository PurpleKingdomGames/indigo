package indigo.physics

import indigo.*
import indigo.physics.Collider
import indigo.physics.Resistance
import indigo.syntax.*

final case class World[A](
    colliders: Batch[Collider[A]],
    forces: Batch[Vector2],
    resistance: Resistance,
    settings: SimulationSettings
):

  def combinedForce: Vector2 =
    forces.foldLeft(Vector2.zero)(_ + _)

  def withForces(newForces: Batch[Vector2]): World[A] =
    this.copy(forces = newForces)
  def withForces(newForces: Vector2*): World[A] =
    withForces(newForces.toBatch)

  def addForces(additionalForces: Batch[Vector2]): World[A] =
    this.copy(forces = forces ++ additionalForces)
  def addForces(additionalForces: Vector2*): World[A] =
    addForces(additionalForces.toBatch)

  def withResistance(newResistance: Resistance): World[A] =
    this.copy(resistance = newResistance)

  def findFirstByTag(tag: A)(using CanEqual[A, A]): Option[Collider[A]] =
    findByTag(tag).headOption

  def findByTag(tag: A)(using CanEqual[A, A]): Batch[Collider[A]] =
    colliders.filter(_.tag == tag)

  def removeByTag(tag: A)(using CanEqual[A, A]): World[A] =
    this.copy(colliders = colliders.filterNot(_.tag == tag))

  def modifyByTag(tag: A)(f: Collider[A] => Collider[A])(using CanEqual[A, A]): World[A] =
    this.copy(colliders = colliders.map(c => if c.tag == tag then f(c) else c))

  def findFirstAt(position: Vertex): Option[Collider[A]] =
    findAt(position).headOption

  def findAt(position: Vertex): Batch[Collider[A]] =
    colliders.filter {
      case c: Collider.Circle[_] if c.bounds.contains(position) => true
      case c: Collider.Box[_] if c.bounds.contains(position)    => true
      case _                                                    => false
    }

  def removeAt(position: Vertex): World[A] =
    this.copy(
      colliders = colliders.filterNot {
        case c: Collider.Circle[_] if c.bounds.contains(position) => true
        case c: Collider.Box[_] if c.bounds.contains(position)    => true
        case _                                                    => false
      }
    )

  def modifyAt(position: Vertex)(f: Collider[A] => Collider[A]): World[A] =
    this.copy(
      colliders = colliders.map {
        case c: Collider.Circle[_] if c.bounds.contains(position) => f(c)
        case c: Collider.Box[_] if c.bounds.contains(position)    => f(c)
        case c                                                    => c
      }
    )

  def findFirstOn(line: LineSegment): Option[Collider[A]] =
    findOn(line).headOption

  def findOn(line: LineSegment): Batch[Collider[A]] =
    colliders.filter {
      case c: Collider.Circle[_] if c.bounds.lineIntersects(line) => true
      case c: Collider.Box[_] if c.bounds.lineIntersects(line)    => true
      case _                                                      => false
    }

  def removeOn(line: LineSegment): World[A] =
    this.copy(
      colliders = colliders.filterNot {
        case c: Collider.Circle[_] if c.bounds.lineIntersects(line) => true
        case c: Collider.Box[_] if c.bounds.lineIntersects(line)    => true
        case _                                                      => false
      }
    )

  def modifyOn(line: LineSegment)(f: Collider[A] => Collider[A]): World[A] =
    this.copy(
      colliders = colliders.map {
        case c: Collider.Circle[_] if c.bounds.lineIntersects(line) => f(c)
        case c: Collider.Box[_] if c.bounds.lineIntersects(line)    => f(c)
        case c                                                      => c
      }
    )

  def findFirstByHitTest(box: BoundingBox): Option[Collider[A]] =
    findByHitTest(box).headOption
  def findFirstByHitTest(circle: BoundingCircle): Option[Collider[A]] =
    findByHitTest(circle).headOption

  def findByHitTest(box: BoundingBox): Batch[Collider[A]] =
    colliders.filter {
      case c: Collider.Circle[_] if c.bounds.overlaps(box) => true
      case c: Collider.Box[_] if c.bounds.overlaps(box)    => true
      case _                                               => false
    }
  def findByHitTest(circle: BoundingCircle): Batch[Collider[A]] =
    colliders.filter {
      case c: Collider.Circle[_] if c.bounds.overlaps(circle) => true
      case c: Collider.Box[_] if c.bounds.overlaps(circle)    => true
      case _                                                  => false
    }

  def removeByHitTest(box: BoundingBox): World[A] =
    this.copy(
      colliders = colliders.filterNot {
        case c: Collider.Circle[_] if c.bounds.overlaps(box) => true
        case c: Collider.Box[_] if c.bounds.overlaps(box)    => true
        case _                                               => false
      }
    )
  def removeByHitTest(circle: BoundingCircle): World[A] =
    this.copy(
      colliders = colliders.filterNot {
        case c: Collider.Circle[_] if c.bounds.overlaps(circle) => true
        case c: Collider.Box[_] if c.bounds.overlaps(circle)    => true
        case _                                                  => false
      }
    )

  def modifyByHitTest(box: BoundingBox)(f: Collider[A] => Collider[A]): World[A] =
    this.copy(
      colliders = colliders.map {
        case c: Collider.Circle[_] if c.bounds.overlaps(box) => f(c)
        case c: Collider.Box[_] if c.bounds.overlaps(box)    => f(c)
        case c                                               => c
      }
    )
  def modifyByHitTest(circle: BoundingCircle)(f: Collider[A] => Collider[A]): World[A] =
    this.copy(
      colliders = colliders.map {
        case c: Collider.Circle[_] if c.bounds.overlaps(circle) => f(c)
        case c: Collider.Box[_] if c.bounds.overlaps(circle)    => f(c)
        case c                                                  => c
      }
    )

  def removeAll: World[A] =
    this.copy(colliders = Batch())

  def withColliders(newColliders: Batch[Collider[A]]): World[A] =
    this.copy(colliders = newColliders)
  def withColliders(newColliders: Collider[A]*): World[A] =
    withColliders(newColliders.toBatch)

  def addColliders(additionalColliders: Batch[Collider[A]]): World[A] =
    this.copy(colliders = colliders ++ additionalColliders)
  def addColliders(additionalColliders: Collider[A]*): World[A] =
    addColliders(additionalColliders.toBatch)

  def present(render: Collider[A] => SceneNode): Batch[SceneNode] =
    colliders.map(render)

  def present(filter: Collider[A] => Boolean)(render: Collider[A] => SceneNode): Batch[SceneNode] =
    colliders.filter(filter).map(render)

  def presentNot(filterNot: Collider[A] => Boolean)(render: Collider[A] => SceneNode): Batch[SceneNode] =
    colliders.filterNot(filterNot).map(render)

  def present(transient: Batch[Collider[A]])(render: Collider[A] => SceneNode): Batch[SceneNode] =
    (colliders ++ transient).map(render)

  def present(filter: Collider[A] => Boolean, transient: Batch[Collider[A]])(
      render: Collider[A] => SceneNode
  ): Batch[SceneNode] =
    (colliders ++ transient).filter(filter).map(render)

  def presentNot(filterNot: Collider[A] => Boolean, transient: Batch[Collider[A]])(
      render: Collider[A] => SceneNode
  ): Batch[SceneNode] =
    (colliders ++ transient).filterNot(filterNot).map(render)

  private val minSize: Double =
    colliders.map(_.boundingBox.size.x).foldLeft(Double.MaxValue) { case (acc, next) =>
      if next < acc then next else acc
    }

  def update(timeDelta: Seconds): Outcome[World[A]] =
    Physics.update(timeDelta, this, Batch.empty, settings)

  def update(timeDelta: Seconds)(transient: Batch[Collider[A]]): Outcome[World[A]] =
    Physics.update(timeDelta, this, transient, settings)
  def update(timeDelta: Seconds)(transient: Collider[A]*): Outcome[World[A]] =
    Physics.update(timeDelta, this, transient.toBatch, settings)

object World:

  def empty[A](settings: SimulationSettings): World[A] =
    World(Batch(), Batch(), Resistance.zero, settings)
