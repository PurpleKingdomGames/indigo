package apigen

import utest._

object ApiGenTests extends TestSuite {

  val tests: Tests =
    Tests {}

}

object CodeSamples {

  val sample: String =
    """
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
    """

}
