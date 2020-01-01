package indigo.shared.datatypes

import indigo.shared.{EqualTo, AsString}

final class ImageAssetRef(val ref: String) extends AnyVal
object ImageAssetRef {

  implicit def eq(implicit eqS: EqualTo[String]): EqualTo[ImageAssetRef] =
    EqualTo.create { (a, b) =>
      eqS.equal(a.ref, b.ref)
    }

  implicit val asString: AsString[ImageAssetRef] =
    AsString.create { r =>
      s"""ImageAssetRef(${r.ref})"""
    }

  def apply(ref: String): ImageAssetRef =
    new ImageAssetRef(ref)
}
