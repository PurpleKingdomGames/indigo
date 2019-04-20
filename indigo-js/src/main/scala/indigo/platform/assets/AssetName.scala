package indigo.platform.assets

import indigo.shared.EqualTo

final class AssetName(val name: String) extends AnyVal
object AssetName {

  implicit val equalTo: EqualTo[AssetName] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.name, b.name)
    }
  }

  def apply(name: String): AssetName =
    new AssetName(name)
}