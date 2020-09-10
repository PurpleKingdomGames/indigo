package indigo.shared.assets

import indigo.shared.EqualTo

final case class AssetName(value: String) extends AnyVal

object AssetName {

  implicit val equalTo: EqualTo[AssetName] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

}
