package indigo.shared.assets

import indigo.shared.EqualTo

final case class AssetPath(value: String) extends AnyVal

object AssetPath {

  implicit val equalTo: EqualTo[AssetPath] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

}
