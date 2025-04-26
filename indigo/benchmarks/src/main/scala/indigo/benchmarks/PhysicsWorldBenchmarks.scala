package indigo.benchmarks

import indigo.*
import indigo.physics.*
import indigo.syntax.*
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*

object PhysicsWorldBenchmarks:

  def render[A]: Collider[A] => SceneNode = {
    case c: Collider.Circle[A] =>
      Shape.Circle(
        c.bounds.position.toPoint,
        c.bounds.radius.toInt,
        Fill.Color(RGBA.White.withAlpha(0.2)),
        Stroke(1, RGBA.White)
      )

    case c: Collider.Box[A] =>
      Shape.Box(
        c.bounds.toRectangle,
        Fill.Color(RGBA.White.withAlpha(0.2)),
        Stroke(1, RGBA.White)
      )
  }

  val suite = GuiSuite(
    Suite("Physics World Benchmarks")(
      Benchmark("update - balls") {
        TestWorlds.worldBalls.update(Seconds(0.1))
      },
      Benchmark("present - balls") {
        TestWorlds.worldBalls.present(render)
      },
      Benchmark("update - boxes") {
        TestWorlds.worldBoxes.update(Seconds(0.1))
      },
      Benchmark("present - boxes") {
        TestWorlds.worldBoxes.present(render)
      },
      Benchmark("update - balls and boxes") {
        TestWorlds.worldBallsAndBoxes.update(Seconds(0.1))
      },
      Benchmark("present - balls and boxes") {
        TestWorlds.worldBallsAndBoxes.present(render)
      }
    )
  )

object TestWorlds:

  val dice: Dice = Dice.default

  val basicWorld: World[MyTag] =
    val circles =
      (0 to 19).toBatch.map { i =>
        Collider(
          MyTag.StaticCircle,
          BoundingCircle(i.toDouble * 100 + 30, 200.0, 20.0)
        ).makeStatic
      }

    World(SimulationSettings(BoundingBox(0, 0, 1280, 920)))
      .addForces(Vector2(0, 600))
      .withResistance(Resistance(0.01))
      .withColliders(circles)
      .addColliders(
        Collider(MyTag.Ball, BoundingCircle(400.0, 80.0, 25.0))
          .withRestitution(Restitution(0.6)),
        Collider(MyTag.Platform, BoundingCircle(-100.0d, 700.0, 300.0)).makeStatic,
        Collider(MyTag.Platform, BoundingCircle(900.0d, 700.0, 300.0)).makeStatic,
        Collider(MyTag.Platform, BoundingCircle(400.0d, 800.0, 300.0)).makeStatic
      )

  val worldBalls: World[MyTag] =
    val balls =
      (0 to 50).toBatch.map { i =>
        Collider(
          MyTag.Ball,
          BoundingCircle(i.toDouble * 10 + 28, (if i % 2 == 0 then 20 else 40) + i.toDouble * 2, 15.0)
        )
          .withRestitution(Restitution(0.8))
      }

    basicWorld.addColliders(balls)

  def worldBoxes: World[MyTag] =
    val cubes =
      (0 to 50).toBatch.map { i =>
        Collider(MyTag.Box, BoundingBox(i.toDouble * 10 + 200, (if i % 2 == 0 then 0 else 50) + 60, 40, 40))
          .withRestitution(Restitution(0.6))
          .withVelocity(dice.roll(200) - 100, -dice.roll(350))
      }

    basicWorld.addColliders(cubes)

  val worldBallsAndBoxes: World[MyTag] =
    val cubes =
      (0 to 25).toBatch.map { i =>
        Collider(MyTag.Box, BoundingBox(i.toDouble * 10 + 200, (if i % 2 == 0 then 0 else 50) + 60, 40, 40))
          .withRestitution(Restitution(0.4))
          .withVelocity(dice.roll(200) - 100, -dice.roll(200))
      }

    val balls =
      (0 to 25).toBatch.map { i =>
        Collider(
          MyTag.Ball,
          BoundingCircle(i.toDouble * 10 + 28, (if i % 2 == 0 then 20 else 40) + i.toDouble * 2, 15.0)
        )
          .withRestitution(Restitution(0.7))
      }

    basicWorld
      .addColliders(balls)
      .addColliders(cubes)

enum MyTag:
  case Platform
  case StaticCircle
  case Ball
  case Box
