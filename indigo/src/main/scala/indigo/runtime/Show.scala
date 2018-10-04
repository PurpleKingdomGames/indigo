package indigo.runtime

trait Show[A] {
  def show(a: A): String
}
object Show {

  implicit val stringShow: Show[String] =
    create(identity)

  implicit val intShow: Show[Int] =
    create(i => i.toString)

  implicit val longShow: Show[Long] =
    create(i => i.toString)

  implicit val doubleShow: Show[Double] =
    create(i => i.toString)

  implicit val floatShow: Show[Float] =
    create(i => i.toString)

  implicit val boolShow: Show[Boolean] =
    create(b => b.toString)

  implicit def tuple2Show[A, B](implicit showA: Show[A], showB: Show[B]): Show[(A, B)] =
    create(t => s"(${showA.show(t._1)}, ${showB.show(t._2)})")

  implicit def listShow[A](implicit showA: Show[A]): Show[List[A]] =
    create(l => s"[${l.map(a => showA.show(a)).mkString(", ")}]")

  implicit def setShow[A](implicit showA: Show[A]): Show[Set[A]] =
    create(s => s"[${s.map(a => showA.show(a)).mkString(", ")}]")

  implicit def optionShow[A](implicit showA: Show[A]): Show[Option[A]] =
    create(oa => s"${oa.map(a => s"Some(${showA.show(a)})").getOrElse("None")}]")

  def create[A](f: A => String): Show[A] =
    new Show[A] {
      def show(a: A): String = f(a)
    }
}
