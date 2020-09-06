package indigo.shared.assets

import indigo.shared.EqualTo

final class AssetName(val value: String) extends AnyVal {
  override def toString(): String = s"AssetName($value)"
}
object AssetName {

  implicit val equalTo: EqualTo[AssetName] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

  def apply(value: String): AssetName =
    new AssetName(value)

  def unapply(a: AssetName): Option[String] =
    Some(a.value)
}
