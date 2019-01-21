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

  trait EqSyntax[A] {
    val eq: Eq[A]
    val value: A
    def ===(other: A): Boolean =
      eq.equal(value, other)

    def !==(other: A): Boolean =
      !eq.equal(value, other)
  }

  implicit class EqString(val value: String)(implicit val eq: Eq[String]) extends EqSyntax[String]

  implicit class EqInt(val value: Int)(implicit val eq: Eq[Int]) extends EqSyntax[Int]

  implicit class EqFloat(val value: Float)(implicit val eq: Eq[Float]) extends EqSyntax[Float]

  implicit class EqDouble(val value: Double)(implicit val eq: Eq[Double]) extends EqSyntax[Double]

  implicit class EqBoolean(val value: Boolean)(implicit val eq: Eq[Boolean]) extends EqSyntax[Boolean]

//
//  implicit class EqInt(value: Int) extends Eq[Int] {
//    def ===(other: Int): Boolean =
//      equal(value, other)
//
//    def !==(other: Int): Boolean =
//      !equal(value, other)
//
//    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
//    def equal(a1: Int, a2: Int): Boolean =
//      a1 == a2
//  }
//
//  implicit class EqFloat(value: Float) extends Eq[Float] {
//    def ===(other: Float): Boolean =
//      equal(value, other)
//
//    def !==(other: Float): Boolean =
//      !equal(value, other)
//
//    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
//    def equal(a1: Float, a2: Float): Boolean =
//      a1 == a2
//  }
//
//  implicit class EqDouble(value: Double) extends Eq[Double] {
//    def ===(other: Double): Boolean =
//      equal(value, other)
//
//    def !==(other: Double): Boolean =
//      !equal(value, other)
//
//    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
//    def equal(a1: Double, a2: Double): Boolean =
//      a1 == a2
//  }
//
//  implicit class EqBoolean(value: Boolean) extends Eq[Boolean] {
//    def ===(other: Boolean): Boolean =
//      equal(value, other)
//
//    def !==(other: Boolean): Boolean =
//      !equal(value, other)
//
//    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
//    def equal(a1: Boolean, a2: Boolean): Boolean =
//      a1 == a2
//  }

}
