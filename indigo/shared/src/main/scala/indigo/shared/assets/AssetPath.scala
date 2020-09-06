package indigo.shared.assets

import indigo.shared.EqualTo

final class AssetPath(val value: String) extends AnyVal {
  override def toString(): String = s"AssetPath($value)"
}
object AssetPath {

  implicit val equalTo: EqualTo[AssetPath] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

  def apply(value: String): AssetPath =
    new AssetPath(value)
}
