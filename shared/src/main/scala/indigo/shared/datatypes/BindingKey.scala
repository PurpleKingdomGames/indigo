package indigo.shared.datatypes

import scala.util.Random

import indigo.shared.EqualTo
import indigo.shared.AsString

final class BindingKey(val value: String) extends AnyVal

object BindingKey {

  def apply(value: String): BindingKey =
    new BindingKey(value)

  def generate: BindingKey =
    BindingKey(Random.alphanumeric.take(16).mkString)

  implicit def eq(implicit eqS: EqualTo[String]): EqualTo[BindingKey] =
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }

  implicit val bindingKeyAsString: AsString[BindingKey] =
    AsString.create { k =>
      s"""BindingKey(${k.value})"""
    }

}
