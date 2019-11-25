package indigoexamples.model

import org.scalacheck._

import indigo.Dice
import indigo.shared.datatypes.Point
import indigo.EqualTo._
import indigo.shared.time.Millis
import indigoexamples.automata.LaunchPadAutomata
import indigo.shared.datatypes.Rectangle
import indigoexts.geometry.Vertex

class FireworksModelSpecification extends Properties("FireworksModel") {

  import Generators._

  val screenDimensions: Rectangle =
    Rectangle(0, 0, 1920, 1080)

  val toScreenSpace: Vertex => Point =
    Projectiles.toScreenSpace(screenDimensions)

  property("generate between 1 and 5 fireworks") = Prop.forAll { dice: Dice =>
    val events = FireworksModel.launchFireworks(dice, toScreenSpace)()

    events.length >= 5 && events.length <= 10
  }

  property(s"generated fireworks will live from between ${LaunchPadAutomata.MinCountDown}ms and ${LaunchPadAutomata.MaxCountDown}ms") = Prop.forAll { dice: Dice =>
    val events = FireworksModel.launchFireworks(dice, toScreenSpace)()

    events.map(_.lifeSpan).mkString("[", ",", "]") |: Prop.all(
      events.forall(_.lifeSpan.isDefined),
      events
        .map(_.lifeSpan)
        .collect { case Some(life) => life }
        .forall(ms => ms >= Millis(LaunchPadAutomata.MinCountDown) && ms <= Millis(LaunchPadAutomata.MaxCountDown))
    )
  }

}
