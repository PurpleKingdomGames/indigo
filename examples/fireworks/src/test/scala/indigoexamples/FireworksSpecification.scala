package indigoexamples

import indigo.Dice
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.time.Millis
import indigoexamples.automata.LaunchPadAutomata
import indigoexamples.model.Projectiles
import indigoextras.geometry.Vertex
import org.scalacheck._

class FireworksSpecification extends Properties("FireworksModel") {

  import indigoexamples.model.Generators._

  val screenDimensions: Rectangle =
    Rectangle(0, 0, 1920, 1080)

  val toScreenSpace: Vertex => Point =
    Projectiles.toScreenSpace(screenDimensions)

  property("generate between 1 and 5 fireworks") = Prop.forAll { (dice: Dice) =>
    val events = Fireworks.launchFireworks(dice, toScreenSpace)

    events.length >= 5 && events.length <= 10
  }

  property(s"generated fireworks will live from between ${LaunchPadAutomata.MinCountDown}ms and ${LaunchPadAutomata.MaxCountDown}ms") = Prop.forAll { (dice: Dice) =>
    val events = Fireworks.launchFireworks(dice, toScreenSpace)

    events.map(_.lifeSpan).mkString("[", ",", "]") |: Prop.all(
      events.forall(_.lifeSpan.isDefined),
      events
        .map(_.lifeSpan)
        .collect { case Some(life) => life }
        .forall(secs => secs >= LaunchPadAutomata.MinCountDown && secs <= LaunchPadAutomata.MaxCountDown)
    )
  }

}
