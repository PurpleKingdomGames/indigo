package pirate.scenes.level.subsystems

import org.scalacheck.*
import org.scalacheck.Prop.*

import indigo.Dice
import indigo.Seconds

class CloudsSubSystemSpecification extends Properties("CloudsSubSystem") {

  val diceGen: Gen[Dice] =
    Gen.choose(0L, Long.MaxValue).map(Dice.Sides.MaxInt)

  implicit val arbDice: Arbitrary[Dice] =
    Arbitrary(diceGen)

  val screenWidthGen: Gen[Int] =
    Gen.choose(1, 1920)

  implicit val arbScreenWidth: Arbitrary[Int] =
    Arbitrary(screenWidthGen)

  property("generate a valid small cloud start point") = forAll { (screenWidth: Int, dice: Dice) =>
    val p =
      CloudsSubSystem.generateSmallCloudStartPoint(screenWidth, dice)

    all(
      s"${p.x} > $screenWidth" |: p.x > screenWidth,
      s"${p.x} < $screenWidth + 30" |: p.x <= screenWidth + 30,
      s"${p.y} > 10" |: p.y > 10,
      s"${p.y} <= 10 + 100" |: p.y <= 10 + 100
    )
  }

  property("generate a valid lifespan for a small cloud") = forAll { (dice: Dice) =>
    val seconds =
      CloudsSubSystem.generateSmallCloudLifeSpan(dice)

    all(
      seconds.isDefined :| s"t = ${seconds}",
      (seconds.get >= Seconds(10)) :| s"t = ${seconds}",
      (seconds.get <= Seconds(20)) :| s"t = ${seconds}"
    )
  }

}
