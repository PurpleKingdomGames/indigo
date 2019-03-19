package indigo.shared

trait IndigoEq[A] {
  def equal(a1: A, a2: A): Boolean
}

object IndigoEq {

  def create[A](f: (A, A) => Boolean): IndigoEq[A] =
    new IndigoEq[A] {
      def equal(a1: A, a2: A): Boolean = f(a1, a2)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqString: IndigoEq[String] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqInt: IndigoEq[Int] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqFloat: IndigoEq[Float] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqDouble: IndigoEq[Double] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqBoolean: IndigoEq[Boolean] = create(_ == _)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqTuple2[A, B](implicit eqA: IndigoEq[A], eqB: IndigoEq[B]): IndigoEq[(A, B)] =
    create((a, b) => eqA.equal(a._1, b._1) && eqB.equal(a._2, b._2))

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqList[A](implicit eq: IndigoEq[A]): IndigoEq[List[A]] =
    create { (a, b) =>
      a.length == b.length && a.zip(b).forall(p => eq.equal(p._1, p._2))
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqOption[A](implicit eq: IndigoEq[A]): IndigoEq[Option[A]] =
    create {
      case (Some(a), Some(b)) =>
        eq.equal(a, b)

      case (None, None) =>
        true

      case _ =>
        false
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit def eqEither[A, B](implicit eqA: IndigoEq[A], eqB: IndigoEq[B]): IndigoEq[Either[A, B]] =
    create {
      case (Left(a), Left(b)) =>
        eqA.equal(a, b)

      case (Right(a), Right(b)) =>
        eqB.equal(a, b)

      case _ =>
        false
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.Nothing"))
  implicit def eqRight[B](implicit eqB: IndigoEq[B]): IndigoEq[Right[Nothing, B]] =
    create {
      case (Right(a), Right(b)) =>
        eqB.equal(a, b)

      case _ =>
        false
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.Nothing"))
  implicit def eqLeft[A](implicit eqA: IndigoEq[A]): IndigoEq[Left[A, Nothing]] =
    create {
      case (Left(a), Left(b)) =>
        eqA.equal(a, b)

      case _ =>
        false
    }

  implicit class EqValue[A](val value: A)(implicit val eq: IndigoEq[A]) {
    def ===(other: A): Boolean =
      eq.equal(value, other)

    def !==(other: A): Boolean =
      !eq.equal(value, other)
  }

}
