package indigo.runtime

trait IndigoShow[A] {
  def show(a: A): String
}
object IndigoShow {

  implicit val stringShow: IndigoShow[String] =
    create(identity)

  implicit val intShow: IndigoShow[Int] =
    create(i => i.toString)

  implicit val longShow: IndigoShow[Long] =
    create(i => i.toString)

  implicit val doubleShow: IndigoShow[Double] =
    create(i => i.toString)

  implicit val floatShow: IndigoShow[Float] =
    create(i => i.toString)

  implicit val boolShow: IndigoShow[Boolean] =
    create(b => b.toString)

  implicit def tuple2Show[A, B](implicit showA: IndigoShow[A], showB: IndigoShow[B]): IndigoShow[(A, B)] =
    create(t => s"(${showA.show(t._1)}, ${showB.show(t._2)})")

  implicit def listShow[A](implicit showA: IndigoShow[A]): IndigoShow[List[A]] =
    create(l => s"[${l.map(a => showA.show(a)).mkString(", ")}]")

  implicit def setShow[A](implicit showA: IndigoShow[A]): IndigoShow[Set[A]] =
    create(s => s"[${s.map(a => showA.show(a)).mkString(", ")}]")

  implicit def optionShow[A](implicit showA: IndigoShow[A]): IndigoShow[Option[A]] =
    create(oa => s"${oa.map(a => s"Some(${showA.show(a)})").getOrElse("None")}]")

  def create[A](f: A => String): IndigoShow[A] =
    new IndigoShow[A] {
      def show(a: A): String = f(a)
    }
}
