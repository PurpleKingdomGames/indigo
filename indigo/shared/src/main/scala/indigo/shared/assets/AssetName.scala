package indigo.shared.assets

import indigo.shared.{EqualTo, AsString}

final class AssetName(val value: String) extends AnyVal
object AssetName {

  implicit val equalTo: EqualTo[AssetName] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

  implicit val asString: AsString[AssetName] =
    AsString.create { r =>
      s"""AssetName(${r.value})"""
    }

  def apply(value: String): AssetName =
    new AssetName(value)
}
