package indigoexamples.model

import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.dice.Dice
import indigo.shared.temporal.Signal
import indigo.shared.time.Seconds
import indigoextras.geometry.Bezier
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import org.scalacheck.Prop._
import org.scalacheck._

class ProjectilesSpecification extends Properties("Projectiles") {

  import Generators._

  val screenDimensions: Rectangle =
    Rectangle(0, 0, 1920, 1080)

  val safeSpace: Rectangle =
    Rectangle(
      1920 / 4,
      (1080 - ((1080 / 6) * 5)) / 2,
      1920 / 2,
      (1080 / 6) * 5
    )

  property("toScreenSpace should always put vertices on the screen") = Prop.forAll(vertexClamped(-1, 1, 0, 1)) { (vertex: Vertex) =>
    val position: Point =
      Projectiles.toScreenSpace(screenDimensions)(vertex)

    // I'm using `isPointWithin` below, but that
    // method is exclusive of the upper bound, but
    // there's nothing wrong with a screen coordinate
    // of exactly 1920 or 1080, for example.
    val padding: Rectangle =
      Rectangle(0, 0, 1, 1)

    Prop.all(
      s"within screen ${position} not within $screenDimensions" |:
        (screenDimensions + padding).isPointWithin(position),
      s"within safe space: ${position} not within $safeSpace" |:
        (safeSpace + padding).isPointWithin(position)
    )
  }

  property("specific toScreenSpace checks") = Prop.all(
    s"0,0 was ${vertexToScreenPoint(Vertex(0, 0))} not ${Point(safeSpace.horizontalCenter, safeSpace.bottom)}" |:
      vertexToScreenPoint(Vertex(0, 0)) == Point(safeSpace.horizontalCenter, safeSpace.bottom),
    s"0,1 was ${vertexToScreenPoint(Vertex(0, 1))} not ${Point(safeSpace.horizontalCenter, safeSpace.top)}" |:
      vertexToScreenPoint(Vertex(0, 1)) == Point(safeSpace.horizontalCenter, safeSpace.top),
    s"-1,0 was ${vertexToScreenPoint(Vertex(-1, 0))} not ${Point(safeSpace.left, safeSpace.bottom)}" |:
      vertexToScreenPoint(Vertex(-1, 0)) == Point(safeSpace.left, safeSpace.bottom),
    s"-1,1 was ${vertexToScreenPoint(Vertex(-1, 1))} not ${Point(safeSpace.left, safeSpace.top)}" |:
      vertexToScreenPoint(Vertex(-1, 1)) == Point(safeSpace.left, safeSpace.top),
    s"1,0 was ${vertexToScreenPoint(Vertex(1, 0))} not ${Point(safeSpace.right, safeSpace.bottom)}" |:
      vertexToScreenPoint(Vertex(1, 0)) == Point(safeSpace.right, safeSpace.bottom),
    s"1,1 was ${vertexToScreenPoint(Vertex(1, 1))} not ${Point(safeSpace.right, safeSpace.top)}" |:
      vertexToScreenPoint(Vertex(1, 1)) == Point(safeSpace.right, safeSpace.top)
  )

  def vertexToScreenPoint(vertex: Vertex): Point =
    Projectiles.toScreenSpace(screenDimensions)(vertex)

  property("arc signal should always produce a value inside the beziers bounds") = Prop.forAll { (dice: Dice, target: Vertex, time: Seconds) =>
    Prop.forAll(vertexGen, vertexGen) {
      case (va, vb) =>
        val vertices: NonEmptyList[Vertex] =
          NonEmptyList(va, vb)

        val bounds: BoundingBox =
          Bezier.fromVerticesNel(vertices).bounds

        val signal: Signal[Vertex] =
          Projectiles.createArcSignal(Seconds(1000))(vertices)

        val point: Vertex =
          signal.at(time)

        "-- Bounds: " + bounds.toString() +
          "\n-- Bounds (left): " + bounds.left.toString +
          "\n-- Bounds (right): " + bounds.right.toString +
          "\n-- Bounds (top): " + bounds.top.toString +
          "\n-- Bounds (bottom): " + bounds.bottom.toString +
          "\n-- target: " + target.toString() +
          "\n-- Point: " + point.toString() +
          "\n-- Time: " + time.toString() +
          "" |: true =? (bounds + BoundingBox(0, 0, 1, 1)).contains(signal.at(time))
    }
  }

  // Previous test guarantees we're always inside the bounds, so we can limit the test cases a bit here.
  property("Over time, the signal should always generate values moving closer to the target") = Prop.forAll { (dice: Dice, target: Vertex) =>
    Prop.forAll(nowNextSeconds(0, 1), vertexGen) {
      case ((now, next), target) =>
        val vertices: NonEmptyList[Vertex] =
          NonEmptyList(Vertex.zero, target / 2, target)

        val signal: Signal[Vertex] =
          Projectiles.createArcSignal(Seconds(1))(vertices)

        val v1: Vertex =
          signal.at(now)

        val v2: Vertex =
          signal.at(next)

        "t1: " + now +
          "\nt2: " + next +
          "\nv1: " + v1 +
          "\nv2: " + v2 +
          "\ntarget: " + target +
          "\nv1 distance: " + v1.distanceTo(target) +
          "\nv2 distance: " + v2.distanceTo(target) +
          "" |: v2.distanceTo(target) <= v1.distanceTo(target)
    }
  }

  property("able to generate a flight time") = Prop.forAll { (dice: Dice) =>
    Prop.forAll(nowNextSeconds(-10, 10)) {
      case (min, max) =>
        val flightTime: Seconds =
          Projectiles.pickFlightTime(dice, min, max)

        Prop.all(
          flightTime >= min,
          flightTime < max
        )
    }
  }
}
