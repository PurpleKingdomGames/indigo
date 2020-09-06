package indigo.shared.assets

import indigo.shared.EqualTo

final class AssetTag(val value: String) extends AnyVal {
  override def toString(): String = s"AssetTag($value)"
}
object AssetTag {

  implicit val equalTo: EqualTo[AssetTag] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

  def apply(value: String): AssetTag =
    new AssetTag(value)

  def unapply(a: AssetTag): Option[String] =
    Some(a.value)
}
