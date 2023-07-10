package indigo.physics

import indigo.*
import indigo.physics.Collider
import indigo.physics.Resistance
import indigo.syntax.*
import scala.annotation.targetName

final case class World[A](colliders: Batch[Collider[A]], forces: Batch[Vector2], resistance: Resistance):

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

  def findByTag(tag: A)(using CanEqual[A, A]): Batch[Collider[A]] =
    colliders.filter(_.tag == tag)

  def removeByTag(tag: A)(using CanEqual[A, A]): World[A] =
    this.copy(colliders = colliders.filterNot(_.tag == tag))

  def modifyByTag(tag: A)(f: Collider[A] => Collider[A])(using CanEqual[A, A]) =
    this.copy(colliders = colliders.map(c => if c.tag == tag then f(c) else c))

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

  def present(filter: A => Boolean)(render: Collider[A] => SceneNode): Batch[SceneNode] =
    colliders.filter(c => filter(c.tag)).map(render)

  def presentNot(filterNot: A => Boolean)(render: Collider[A] => SceneNode): Batch[SceneNode] =
    colliders.filterNot(c => filterNot(c.tag)).map(render)

  @targetName("present_filter_whole_collider")
  def present(filter: Collider[A] => Boolean)(render: Collider[A] => SceneNode): Batch[SceneNode] =
    colliders.filter(filter).map(render)

  @targetName("present_filter_not_whole_collider")
  def presentNot(filterNot: Collider[A] => Boolean)(render: Collider[A] => SceneNode): Batch[SceneNode] =
    colliders.filterNot(filterNot).map(render)

  def update(timeDelta: Seconds): Outcome[World[A]] =
    Physics.update(timeDelta, this)

object World:

  def empty[A]: World[A] =
    World(Batch(), Batch(), Resistance.zero)
