package indigo.shared.assets

import indigo.shared.{EqualTo, AsString}

final class AssetPath(val value: String) extends AnyVal
object AssetPath {

  implicit val equalTo: EqualTo[AssetPath] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

  implicit val asString: AsString[AssetPath] =
    AsString.create { r =>
      s"""AssetName(${r.value})"""
    }

  def apply(value: String): AssetPath =
    new AssetPath(value)
}
