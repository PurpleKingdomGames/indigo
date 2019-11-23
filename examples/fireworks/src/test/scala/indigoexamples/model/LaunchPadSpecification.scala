package indigoexamples.model

import org.scalacheck._

import indigo.Dice
import ingidoexamples.model.LaunchPad
import indigo.shared.datatypes.Point
import indigo.EqualTo._
import indigo.shared.datatypes.Rectangle

class LaunchPadSpecification extends Properties("LaunchPad") {

  import Generators._

  def launchPadGen: Gen[LaunchPad] =
    diceGen.map(dice => LaunchPad.generateLaunchPad(dice))

  implicit val arbLaunchPad: Arbitrary[LaunchPad] =
    Arbitrary(launchPadGen)

  property("generate a launch pad with a timer up to 1.5 seconds") = Prop.forAll { launchPad: LaunchPad =>
    launchPad.countDown.value >= 1 && launchPad.countDown.value <= 1500
  }

  property("generate a launch pad vertex y=0 and x=-1 to 1") = Prop.forAll { dice: Dice =>
    val launchPad: LaunchPad =
      LaunchPad.generateLaunchPad(dice)

    launchPad.position.y === 0 && launchPad.position.x >= -1 && launchPad.position.x <= 1
  }

  property("generate a launch pad at x < 0") = Prop.exists { dice: Dice =>
    val launchPad: LaunchPad =
      LaunchPad.generateLaunchPad(dice)

    launchPad.position.x < 0
  }

  property("generate a launch pad at x > 0") = Prop.exists { dice: Dice =>
    val launchPad: LaunchPad =
      LaunchPad.generateLaunchPad(dice)

    launchPad.position.x > 0
  }

}
