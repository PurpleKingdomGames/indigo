package indigoexamples.model

import org.scalacheck._

import indigo.Dice
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigoexamples.automata.LaunchPadAutomata

class LaunchPadSpecification extends Properties("LaunchPad") {

  import Generators._

  def launchPadGen: Gen[LaunchPad] =
    diceGen.map(dice => LaunchPad.generateLaunchPad(dice))

  implicit val arbLaunchPad: Arbitrary[LaunchPad] =
    Arbitrary(launchPadGen)

  property("generate a launch pad with a timer up to " + LaunchPadAutomata.MaxCountDown.value + " seconds") = Prop.forAll { (launchPad: LaunchPad) =>
    launchPad.countDown.value >= 0.1 && launchPad.countDown.value <= LaunchPadAutomata.MaxCountDown.value
  }

  property("generate a launch pad vertex y=0 and x=-1 to 1") = Prop.forAll { (dice: Dice) =>
    val launchPad: LaunchPad =
      LaunchPad.generateLaunchPad(dice)

    launchPad.position.y == 0 && launchPad.position.x >= -1 && launchPad.position.x <= 1
  }

  property("generate a launch pad at x < 0") = Prop.exists { (dice: Dice) =>
    val launchPad: LaunchPad =
      LaunchPad.generateLaunchPad(dice)

    launchPad.position.x < 0
  }

  property("generate a launch pad at x > 0") = Prop.exists { (dice: Dice) =>
    val launchPad: LaunchPad =
      LaunchPad.generateLaunchPad(dice)

    launchPad.position.x > 0
  }

}
