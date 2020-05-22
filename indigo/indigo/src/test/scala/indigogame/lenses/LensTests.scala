package indigogame.lenses

import utest._

object LensTests extends TestSuite {

  val sample: A =
    A(B(10), C(D("hello")))

  val lensAB: Lens[A, B] =
    Lens(
      (a: A) => a.b,
      (a: A, b: B) => a.copy(b = b)
    )

  val lensAC: Lens[A, C] =
    Lens(
      (a: A) => a.c,
      (a: A, c: C) => a.copy(c = c)
    )

  val lensCD: Lens[C, D] =
    Lens(
      (c: C) => c.d,
      (c: C, d: D) => c.copy(d = d)
    )

  val lensD: Lens[D, String] =
    Lens(
      (d: D) => d.s,
      (d: D, s: String) => d.copy(s = s)
    )

  val tests: Tests =
    Tests {
      "Constructors" - {
        "should have a simple identity function when there's nothing to do" - {
          Lens.identity.get(B(10)) ==> B(10)
        }

        "should have allow you to keep the original" - {
          Lens.keepOriginal[D].get(D("hi")) ==> D("hi")
          Lens.keepOriginal[D].set(D("hi"), D("fish")) ==> D("hi")
        }

        "should have allow you to keep the latest" - {
          Lens.keepLatest[D].get(D("hi")) ==> D("hi")
          Lens.keepLatest[D].set(D("hi"), D("fish")) ==> D("fish")
        }

        "should allow you to define a fixed lens with a constant B" - {
          Lens.fixed[C, D](D("hi")).get(C(D("x"))) ==> D("hi")
          Lens.fixed[C, D](D("hi")).set(C(D("x")), D("y")) ==> C(D("x"))
        }
      }

      "Getting" - {

        "should be able to get a sub-object" - {
          lensAB.get(sample) ==> B(10)
        }

        "should be able to get a more deeply nested sub-object with lens composition" - {
          (lensAC andThen lensCD >=> lensD).get(sample) ==> "hello"
        }

      }

      "Setting" - {

        "should be able to set a sub-object" - {
          lensAB.set(sample, B(20)) ==> sample.copy(b = B(20))
        }

        "should be able to set then get" - {
          lensAB.get(lensAB.set(sample, B(50))) ==> B(50)
        }

        "should be able to set a more deeply nested sub-object with lens composition" - {
          val lens: Lens[A, String] = lensAC andThen lensCD andThen lensD
          val res1                  = lens.set(sample, "world")

          res1.c.d.s ==> "world"
          lens.get(res1) ==> "world"
        }

      }

      "Modifying" - {

        "should be able to modify a value in place" - {

          val lens  = lensCD andThen lensD
          val value = C(D("hello"))

          lensCD.modify(value, d => D(d.s + " there")) ==> C(D("hello there"))
          lens.modify(value, _ + ", world!") ==> C(D("hello, world!"))

        }

      }

      "Lens Laws" - {

        val lens: Lens[B, Int] =
          Lens(
            b => b.i,
            (b, ii) => b.copy(i = ii)
          )

        "must be true, that getting and setting a value back, changes nothing." - {
          val x = B(100)

          lens.set(x, lens.get(x)) ==> x
        }

        "must be true, that setting and then getting returns what I set." - {
          lens.get(lens.set(B(100), 50)) ==> 50
        }

        "must be true, that setting twice and then getting returns the last value (no history)" - {
          lens.get(lens.set(lens.set(B(100), 50), 25)) ==> 25
        }

      }

      // "Optional lens" - {
      //   "should not be able to get a missing value" - {
      //     pending
      //   }

      //   "should not be able to set a missing value" - {
      //     pending
      //   }

      //   "should be able to get a present value" - {
      //     pending
      //   }

      //   "should be able to set a present value" - {
      //     pending
      //   }
      // }

      // "Membership lens" - {
      //   "should not be able to get a missing value" - {
      //     pending
      //   }

      //   "should be able to set a missing value" - {
      //     pending
      //   }

      //   "should be able to get a present value" - {
      //     pending
      //   }

      //   "should be able to set a present value" - {
      //     pending
      //   }
      // }

      // "Prism" - {
      //   pending
      // }
    }

  // What does this even mean? Clearly a load of rubbish.
  /*
  There's a lot wrong here.
  A lens on a collection needs to be recursive
  Or it needs to be a memberhsip lens
  Or it needs to be an optional lens
  Or it needs to be a prism.

  I'm leaving this here for now, not sure if I'll fix it as technically none of this is needed.
    ..but it is interesting.
   */
//  "Parametric lenses" - {
//
//    "should be able to change the type of a tuple" - {
//
//      implicit val lensTupleRight: Lens[(Int, String), String] =
//        Lens(
//          p => p._2,
//          (a, b) => (a._1, b)
//        )
//      implicit val lensList: Lens[List[Int], Int] =
//        Lens(
//          p => p.head,
//          (a, b) => b :: a.drop(1)
//        )
//
//      val value: (Int, String) = (10, "foo")
//
//      "String => String (tuple)" - {
//        val expectedA: (Int, String) = (10, "bar")
//
//        val f: String => String                         = _ => "bar"
//        val g: ((Int, String), String) => (Int, String) = (s, b) => (s._1, b)
//
//        Lens.modifyF(value)(f, g) ==> expectedA
//      }
//
//      "String => Boolean (tuple)" - {
//        val expectedB: (Int, Boolean) = (10, true)
//
//        val f: String => Boolean                          = _.length < 5
//        val g: ((Int, String), Boolean) => (Int, Boolean) = (s, b) => (s._1, b)
//
//        Lens.modifyF(value)(f, g) ==> expectedB
//      }
//
//      "String => Boolean (list)" - {
//        val l: List[Int] = List(1,2,3,4)
//        val expectedC: List[Boolean] = List(false, true, false, true)
//
//        val f: Int => Boolean                          = _ % 2 == 0
//        val g: (List[Int], Boolean) => List[Boolean] = (s, b) => (s._1, b)
//
//        Lens.modifyF(l)(f, g) ==> expectedC
//      }
//    }
//
//  }

}

final case class A(b: B, c: C)
final case class B(i: Int)
final case class C(d: D)
final case class D(s: String)
