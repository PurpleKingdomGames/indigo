package indigo.shared

trait Eq[A] {
  def equal(a1: A, a2: A): Boolean
}

object Eq {

  def create[A](f: (A, A) => Boolean): Eq[A] =
    new Eq[A] {
      def equal(a1: A, a2: A): Boolean = f(a1, a2)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqString: Eq[String] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqInt: Eq[Int] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqFloat: Eq[Float] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqDouble: Eq[Double] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqBoolean: Eq[Boolean] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqTuple2[A, B](implicit eqA: Eq[A], eqB: Eq[B]): Eq[(A, B)] =
    create((a, b) => eqA.equal(a._1, b._1) && eqB.equal(a._2, b._2))

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqList[A](implicit eq: Eq[A]): Eq[List[A]] =
    create { (a, b) =>
      a.length == b.length && a.zip(b).forall(p => eq.equal(p._1, p._2))
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqOption[A](implicit eq: Eq[A]): Eq[Option[A]] =
    create {
      case (Some(a), Some(b)) =>
        eq.equal(a, b)

      case (None, None) =>
        true

      case _ =>
        false
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqEither[A, B](implicit eqA: Eq[A], eqB: Eq[B]): Eq[Either[A, B]] =
    create {
      case (Left(a), Left(b)) =>
        eqA.equal(a, b)

      case (Right(a), Right(b)) =>
        eqB.equal(a, b)

      case _ =>
        false
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.Nothing"))
  implicit def eqRight[B](implicit eqB: Eq[B]): Eq[Right[Nothing, B]] =
    create {
      case (Right(a), Right(b)) =>
        eqB.equal(a, b)

      case _ =>
        false
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.Nothing"))
  implicit def eqLeft[A](implicit eqA: Eq[A]): Eq[Left[A, Nothing]] =
    create {
      case (Left(a), Left(b)) =>
        eqA.equal(a, b)

      case _ =>
        false
    }

  implicit class EqValue[A](val value: A)(implicit val eq: Eq[A]) {
    def ===(other: A): Boolean =
      eq.equal(value, other)

    def !==(other: A): Boolean =
      !eq.equal(value, other)
  }

}
