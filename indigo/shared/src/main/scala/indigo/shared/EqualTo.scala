package indigo.shared

trait EqualTo[A] {
  def equal(a1: A, a2: A): Boolean
}

object EqualTo {

  def create[A](f: (A, A) => Boolean): EqualTo[A] =
    new EqualTo[A] {
      def equal(a1: A, a2: A): Boolean = f(a1, a2)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqChar: EqualTo[Char] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqString: EqualTo[String] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqInt: EqualTo[Int] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqLong: EqualTo[Long] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqFloat: EqualTo[Float] = create((a, b) => a > b - 0.001 && a < b + 0.001)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqDouble: EqualTo[Double] = create((a, b) => a > b - 0.001 && a < b + 0.001)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqBoolean: EqualTo[Boolean] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqTuple2[A, B](implicit eqA: EqualTo[A], eqB: EqualTo[B]): EqualTo[(A, B)] =
    create((a, b) => eqA.equal(a._1, b._1) && eqB.equal(a._2, b._2))

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqList[A](implicit eq: EqualTo[A]): EqualTo[List[A]] =
    create { (a, b) =>
      a.length == b.length && a.zip(b).forall(p => eq.equal(p._1, p._2))
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqOption[A](implicit eq: EqualTo[A]): EqualTo[Option[A]] =
    create {
      case (Some(a), Some(b)) =>
        eq.equal(a, b)

      case (None, None) =>
        true

      case _ =>
        false
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqEither[A, B](implicit eqA: EqualTo[A], eqB: EqualTo[B]): EqualTo[Either[A, B]] =
    create {
      case (Left(a), Left(b)) =>
        eqA.equal(a, b)

      case (Right(a), Right(b)) =>
        eqB.equal(a, b)

      case _ =>
        false
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.Nothing"))
  implicit def eqRight[B](implicit eqB: EqualTo[B]): EqualTo[Right[Nothing, B]] =
    create {
      case (Right(a), Right(b)) =>
        eqB.equal(a, b)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.Nothing"))
  implicit def eqLeft[A](implicit eqA: EqualTo[A]): EqualTo[Left[A, Nothing]] =
    create {
      case (Left(a), Left(b)) =>
        eqA.equal(a, b)
    }

  implicit class EqualToSyntax[A](val value: A)(implicit val eq: EqualTo[A]) {
    def ===(other: A): Boolean =
      eq.equal(value, other)

    def !==(other: A): Boolean =
      !eq.equal(value, other)
  }

}
