package indigo.shared

/**
  * AsString is a simple TypeClass that Indigo uses to enforce intentional
  * conversions of any type A to String
  * @tparam A The type were require an AsString instance for.
  */
trait AsString[A] {

  /**
    * show converts the type A into a String
    * @param a
    * @return
    */
  def show(a: A): String
  def asString(a: A): String = show(a)
}
object AsString {

  implicit val stringShow: AsString[String] =
    create(identity)

  implicit val intShow: AsString[Int] =
    create(i => i.toString)

  implicit val longShow: AsString[Long] =
    create(i => i.toString)

  implicit val doubleShow: AsString[Double] =
    create(i => i.toString)

  implicit val floatShow: AsString[Float] =
    create(i => i.toString)

  implicit val boolShow: AsString[Boolean] =
    create(b => b.toString)

  implicit def tuple2Show[A, B](implicit showA: AsString[A], showB: AsString[B]): AsString[(A, B)] =
    create(t => s"(${showA.show(t._1)}, ${showB.show(t._2)})")

  implicit def listShow[A](implicit showA: AsString[A]): AsString[List[A]] =
    create(l => s"[${l.map(a => showA.show(a)).mkString(", ")}]")

  implicit def setShow[A](implicit showA: AsString[A]): AsString[Set[A]] =
    create(s => s"[${s.map(a => showA.show(a)).mkString(", ")}]")

  implicit def optionShow[A](implicit showA: AsString[A]): AsString[Option[A]] =
    create(oa => s"${oa.map(a => s"Some(${showA.show(a)})").getOrElse("None")}]")

  /**
    * @constructor Used to make AsString instances
    * @param f A function defining the process of turning [A] into a [String]
    * @tparam A The type were require an AsString instance for.
    * @return An instance of [AsString] for [A]
    */
  def create[A](f: A => String): AsString[A] =
    new AsString[A] {
      def show(a: A): String = f(a)
    }

  /**
    * Provides an implicit extension method
    * @example `
    *          import indigo.AsString._
    *          10.show
    *          `
    * @param a value of type A to convert to a String
    * @param s required implicit AsString for type A
    * @tparam A the type we need an implicit AsString instance of.
    */
  implicit class AsStringSyntax[A](val a: A)(implicit val s: AsString[A]) {
    def show: String =
      s.show(a)

    def asString: String =
      s.show(a)
  }
}
