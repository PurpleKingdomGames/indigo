package indigo.physics

import indigo.*
import indigo.physics.Resistance
import indigo.physics.simulation.Simulation
import indigo.syntax.*

final case class World[Tag](
    colliders: Batch[Collider[Tag]],
    forces: Batch[Vector2],
    resistance: Resistance,
    settings: SimulationSettings
):

  def combinedForce: Vector2 =
    forces.foldLeft(Vector2.zero)(_ + _)

  def withForces(newForces: Batch[Vector2]): World[Tag] =
    this.copy(forces = newForces)
  def withForces(newForces: Vector2*): World[Tag] =
    withForces(newForces.toBatch)

  def addForces(additionalForces: Batch[Vector2]): World[Tag] =
    this.copy(forces = forces ++ additionalForces)
  def addForces(additionalForces: Vector2*): World[Tag] =
    addForces(additionalForces.toBatch)

  def withResistance(newResistance: Resistance): World[Tag] =
    this.copy(resistance = newResistance)

  def withSimulationSettings(newSettings: SimulationSettings): World[Tag] =
    this.copy(settings = newSettings)

  def collect[B](matching: PartialFunction[Collider[Tag], B]): Batch[B] =
    colliders.collect(matching)

  def findFirstByTag(tag: Tag)(using CanEqual[Tag, Tag]): Option[Collider[Tag]] =
    findByTag(tag).headOption

  def findByTag(tag: Tag)(using CanEqual[Tag, Tag]): Batch[Collider[Tag]] =
    colliders.filter(_.tag == tag)

  def removeByTag(tag: Tag)(using CanEqual[Tag, Tag]): World[Tag] =
    this.copy(colliders = colliders.filterNot(_.tag == tag))

  def removeAllByTag(tags: Batch[Tag])(using CanEqual[Tag, Tag]): World[Tag] =
    this.copy(colliders = colliders.filterNot(c => tags.exists(_ == c.tag)))

  def modifyAll(f: Collider[Tag] => Collider[Tag]): World[Tag] =
    this.copy(colliders = colliders.map(f))

  def modifyByTag(tag: Tag)(f: Collider[Tag] => Collider[Tag])(using CanEqual[Tag, Tag]): World[Tag] =
    this.copy(colliders = colliders.map(c => if c.tag == tag then f(c) else c))

  def findFirstAt(position: Vertex): Option[Collider[Tag]] =
    findAt(position).headOption

  def findAt(position: Vertex): Batch[Collider[Tag]] =
    colliders.filter {
      case c: Collider.Circle[_] if c.bounds.contains(position) => true
      case c: Collider.Box[_] if c.bounds.contains(position)    => true
      case _                                                    => false
    }

  def removeAt(position: Vertex): World[Tag] =
    this.copy(
      colliders = colliders.filterNot {
        case c: Collider.Circle[_] if c.bounds.contains(position) => true
        case c: Collider.Box[_] if c.bounds.contains(position)    => true
        case _                                                    => false
      }
    )

  def modifyAt(position: Vertex)(f: Collider[Tag] => Collider[Tag]): World[Tag] =
    this.copy(
      colliders = colliders.map {
        case c: Collider.Circle[_] if c.bounds.contains(position) => f(c)
        case c: Collider.Box[_] if c.bounds.contains(position)    => f(c)
        case c                                                    => c
      }
    )

  def findFirstOn(line: LineSegment): Option[Collider[Tag]] =
    findOn(line).headOption

  def findOn(line: LineSegment): Batch[Collider[Tag]] =
    colliders.filter {
      case c: Collider.Circle[_] if c.bounds.lineIntersects(line) => true
      case c: Collider.Box[_] if c.bounds.lineIntersects(line)    => true
      case _                                                      => false
    }

  def removeOn(line: LineSegment): World[Tag] =
    this.copy(
      colliders = colliders.filterNot {
        case c: Collider.Circle[_] if c.bounds.lineIntersects(line) => true
        case c: Collider.Box[_] if c.bounds.lineIntersects(line)    => true
        case _                                                      => false
      }
    )

  def modifyOn(line: LineSegment)(f: Collider[Tag] => Collider[Tag]): World[Tag] =
    this.copy(
      colliders = colliders.map {
        case c: Collider.Circle[_] if c.bounds.lineIntersects(line) => f(c)
        case c: Collider.Box[_] if c.bounds.lineIntersects(line)    => f(c)
        case c                                                      => c
      }
    )

  def findFirstByHitTest(box: BoundingBox): Option[Collider[Tag]] =
    findByHitTest(box).headOption
  def findFirstByHitTest(circle: BoundingCircle): Option[Collider[Tag]] =
    findByHitTest(circle).headOption

  def findByHitTest(box: BoundingBox): Batch[Collider[Tag]] =
    colliders.filter {
      case c: Collider.Circle[_] if c.bounds.overlaps(box) => true
      case c: Collider.Box[_] if c.bounds.overlaps(box)    => true
      case _                                               => false
    }
  def findByHitTest(circle: BoundingCircle): Batch[Collider[Tag]] =
    colliders.filter {
      case c: Collider.Circle[_] if c.bounds.overlaps(circle) => true
      case c: Collider.Box[_] if c.bounds.overlaps(circle)    => true
      case _                                                  => false
    }

  def removeByHitTest(box: BoundingBox): World[Tag] =
    this.copy(
      colliders = colliders.filterNot {
        case c: Collider.Circle[_] if c.bounds.overlaps(box) => true
        case c: Collider.Box[_] if c.bounds.overlaps(box)    => true
        case _                                               => false
      }
    )
  def removeByHitTest(circle: BoundingCircle): World[Tag] =
    this.copy(
      colliders = colliders.filterNot {
        case c: Collider.Circle[_] if c.bounds.overlaps(circle) => true
        case c: Collider.Box[_] if c.bounds.overlaps(circle)    => true
        case _                                                  => false
      }
    )

  def modifyByHitTest(box: BoundingBox)(f: Collider[Tag] => Collider[Tag]): World[Tag] =
    this.copy(
      colliders = colliders.map {
        case c: Collider.Circle[_] if c.bounds.overlaps(box) => f(c)
        case c: Collider.Box[_] if c.bounds.overlaps(box)    => f(c)
        case c                                               => c
      }
    )
  def modifyByHitTest(circle: BoundingCircle)(f: Collider[Tag] => Collider[Tag]): World[Tag] =
    this.copy(
      colliders = colliders.map {
        case c: Collider.Circle[_] if c.bounds.overlaps(circle) => f(c)
        case c: Collider.Box[_] if c.bounds.overlaps(circle)    => f(c)
        case c                                                  => c
      }
    )

  def removeAll: World[Tag] =
    this.copy(colliders = Batch())

  def withColliders(newColliders: Batch[Collider[Tag]]): World[Tag] =
    this.copy(colliders = newColliders)
  def withColliders(newColliders: Collider[Tag]*): World[Tag] =
    withColliders(newColliders.toBatch)

  def addColliders(additionalColliders: Batch[Collider[Tag]]): World[Tag] =
    this.copy(colliders = colliders ++ additionalColliders)
  def addColliders(additionalColliders: Collider[Tag]*): World[Tag] =
    addColliders(additionalColliders.toBatch)

  def present(render: Collider[Tag] => SceneNode): Batch[SceneNode] =
    colliders.map(render)

  def present(filter: Collider[Tag] => Boolean)(render: Collider[Tag] => SceneNode): Batch[SceneNode] =
    colliders.filter(filter).map(render)

  def presentNot(filterNot: Collider[Tag] => Boolean)(render: Collider[Tag] => SceneNode): Batch[SceneNode] =
    colliders.filterNot(filterNot).map(render)

  def present(transient: Batch[Collider[Tag]])(render: Collider[Tag] => SceneNode): Batch[SceneNode] =
    (colliders ++ transient).map(render)

  def present(filter: Collider[Tag] => Boolean, transient: Batch[Collider[Tag]])(
      render: Collider[Tag] => SceneNode
  ): Batch[SceneNode] =
    (colliders ++ transient).filter(filter).map(render)

  def presentNot(filterNot: Collider[Tag] => Boolean, transient: Batch[Collider[Tag]])(
      render: Collider[Tag] => SceneNode
  ): Batch[SceneNode] =
    (colliders ++ transient).filterNot(filterNot).map(render)

  def update(timeDelta: Seconds): Outcome[World[Tag]] =
    Simulation.updateWorld(this, timeDelta, Batch.empty, settings)

  def update(timeDelta: Seconds)(transient: Batch[Collider[Tag]]): Outcome[World[Tag]] =
    Simulation.updateWorld(this, timeDelta, transient, settings)
  def update(timeDelta: Seconds)(transient: Collider[Tag]*): Outcome[World[Tag]] =
    Simulation.updateWorld(this, timeDelta, transient.toBatch, settings)

object World:

  def empty[Tag]: World[Tag] =
    World(Batch(), Batch(), Resistance.zero, SimulationSettings.default)

  def apply[Tag](settings: SimulationSettings): World[Tag] =
    World(Batch(), Batch(), Resistance.zero, settings)
