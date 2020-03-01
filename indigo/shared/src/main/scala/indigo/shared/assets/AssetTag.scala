package indigo.shared.assets

import indigo.shared.{EqualTo, AsString}

final class AssetTag(val value: String) extends AnyVal
object AssetTag {

  implicit val equalTo: EqualTo[AssetTag] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

  implicit val asString: AsString[AssetTag] =
    AsString.create { r =>
      s"""AssetTag(${r.value})"""
    }

  def apply(value: String): AssetTag =
    new AssetTag(value)

  def unapply(a: AssetTag): Option[String] =
    Some(a.value)
}
