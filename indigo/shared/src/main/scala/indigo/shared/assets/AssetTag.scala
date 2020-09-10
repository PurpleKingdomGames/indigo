package indigo.shared.assets

import indigo.shared.EqualTo

final case class AssetTag(value: String) extends AnyVal

object AssetTag {

  implicit val equalTo: EqualTo[AssetTag] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

}
