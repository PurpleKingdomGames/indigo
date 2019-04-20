package indigo.shared.animation

import indigo.shared.{AsString, EqualTo}
import indigo.shared.EqualTo._

final class AnimationKey(val value: String) extends AnyVal

object AnimationKey {

  implicit val animationKeyEqualTo: EqualTo[AnimationKey] =
    EqualTo.create { (a, b) =>
      a.value === b.value
    }

  implicit val animationKeyAsString: AsString[AnimationKey] =
    AsString.create { key =>
      s"AnimationKey(${key.value})"
    }

  def apply(key: String): AnimationKey =
    new AnimationKey(key)
}
