package indigoexamples.model

import org.scalacheck._

import indigo.Dice
import indigo.shared.datatypes.Point
import indigo.EqualTo._
import ingidoexamples.model.FireworksModel
import indigo.shared.time.Millis
import ingidoexamples.automata.LaunchPadAutomaton
import indigo.shared.datatypes.Rectangle

class FireworksModelSpecification extends Properties("FireworksModel") {

  import Generators._

  val screenDimensions: Rectangle =
    Rectangle(0, 0, 1920, 1080)

  property("generate between 1 and 5 fireworks") = Prop.forAll(diceGen) { dice =>
    val events = FireworksModel.launchFireworks(dice, screenDimensions)()

    events.length >= 5 && events.length <= 10
  }

  property("generated fireworks will launch from 5 pixels up from the baseline") = Prop.forAll(diceGen) { dice =>
    val start  = Point.zero
    val end    = Point(1920, 1080)
    val events = FireworksModel.launchFireworks(dice, screenDimensions)()

    events.map(_.at.y).mkString("[", ",", "]") |:
      Prop.all(events.map(_.at).forall(pt => pt.y == end.y - 5))
  }

  property("generated fireworks will launch from the central middle half of the baseline") = Prop.forAll(diceGen) { dice =>
    val events = FireworksModel.launchFireworks(dice, screenDimensions)()

    val diff: Int = screenDimensions.width
    val minX      = diff / 4
    val maxX      = (diff / 4) * 3

    "minX: " + minX + ", maxX: " + maxX + ", xs: " + events.map(_.at.x).mkString("[", ",", "]") |:
      Prop.all(events.map(_.at).forall(pt => pt.x >= minX && pt.x <= maxX))
  }

  property(s"generated fireworks will live from between ${LaunchPadAutomaton.MinCountDown}ms and ${LaunchPadAutomaton.MaxCountDown}ms") = Prop.forAll(diceGen) { dice =>
    val events = FireworksModel.launchFireworks(dice, screenDimensions)()

    events.map(_.lifeSpan).mkString("[", ",", "]") |: Prop.all(
      events.forall(_.lifeSpan.isDefined),
      events
        .map(_.lifeSpan)
        .collect { case Some(life) => life }
        .forall(ms => ms >= Millis(LaunchPadAutomaton.MinCountDown) && ms <= Millis(LaunchPadAutomaton.MaxCountDown))
    )
  }

}
