package indigoexamples.model

import org.scalacheck._

import indigo.Dice
import indigo.shared.datatypes.Point
import indigo.EqualTo._
import ingidoexamples.model.FireworksModel
import indigo.shared.time.Millis
import ingidoexamples.automata.LaunchPadAutomaton

class FireworksModelSpecification extends Properties("FireworksModel") {

  import Generators._

  val viewportSize: Point =
    Point(1920, 1080)

  property("generate between 1 and 5 fireworks") = Prop.forAll(diceGen) { dice =>
    val events = FireworksModel.launchFireworks(dice, viewportSize)()

    events.length >= 1 && events.length <= 5
  }

  property("generated fireworks will launch from 5 pixels up from the baseline") = Prop.forAll(diceGen) { dice =>
    val start  = Point.zero
    val end    = Point(1920, 1080)
    val events = FireworksModel.launchFireworks(dice, viewportSize)()

    events.map(_.at.y).mkString("[", ",", "]") |:
      Prop.all(events.map(_.at).forall(pt => pt.y == end.y - 5))
  }

  property("generated fireworks will launch from the center third of the baseline") = Prop.forAll(diceGen) { dice =>
    val events = FireworksModel.launchFireworks(dice, viewportSize)()

    val diff: Int = viewportSize.x
    val minX      = diff / 3
    val maxX      = (diff / 3) * 2

    "minX: " + minX + ", maxX: " + maxX + ", xs: " + events.map(_.at.x).mkString("[", ",", "]") |:
      Prop.all(events.map(_.at).forall(pt => pt.x >= minX && pt.x <= maxX))
  }

  property(s"generated fireworks will live from between 500ms and ${LaunchPadAutomaton.MaxCountDown}ms") = Prop.forAll(diceGen) { dice =>
    val events = FireworksModel.launchFireworks(dice, viewportSize)()

    events.map(_.lifeSpan).mkString("[", ",", "]") |: Prop.all(
      events.forall(_.lifeSpan.isDefined),
      events
        .map(_.lifeSpan)
        .collect { case Some(life) => life }
        .forall(ms => ms >= Millis(500) && ms <= Millis(LaunchPadAutomaton.MaxCountDown))
    )
  }

}
