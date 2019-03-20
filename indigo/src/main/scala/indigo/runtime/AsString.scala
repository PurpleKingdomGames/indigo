package indigo.runtime

trait AsString[A] {
  def show(a: A): String
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

  def create[A](f: A => String): AsString[A] =
    new AsString[A] {
      def show(a: A): String = f(a)
    }
}
