package indigo.scenes

class LensTests extends munit.FunSuite {

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

  test("Constructors.should have a simple identity function when there's nothing to do") {
    assertEquals(Lens.identity.get(B(10)), B(10))
  }

  test("Constructors.should have allow you to keep the original") {
    assertEquals(Lens.keepOriginal[D].get(D("hi")), D("hi"))
    assertEquals(Lens.keepOriginal[D].set(D("hi"), D("fish")), D("hi"))
  }

  test("Constructors.should have allow you to keep the latest") {
    assertEquals(Lens.keepLatest[D].get(D("hi")), D("hi"))
    assertEquals(Lens.keepLatest[D].set(D("hi"), D("fish")), D("fish"))
  }

  test("Constructors.should allow you to define a fixed lens with a constant B") {
    assertEquals(Lens.fixed[C, D](D("hi")).get(C(D("x"))), D("hi"))
    assertEquals(Lens.fixed[C, D](D("hi")).set(C(D("x")), D("y")), C(D("x")))
  }

  test("Constructors.should be able to define a read only lens") {
    val lens     = Lens.readOnly[C, D](_.d)
    val original = C(D("x"))

    assertEquals(lens.get(original), D("x"))
    assertEquals(lens.set(original, D("fish")), original)
  }

  test("Constructors.should allow you to define a lens that throws away the value") {
    val m = C(D("x"))
    assertEquals(Lens.unit[C].get(m), ())
    assertEquals(Lens.unit[C].set(m, ()), m)
  }

  test("Getting.should be able to get a sub-object") {
    assertEquals(lensAB.get(sample), B(10))
  }

  test("Getting.should be able to get a more deeply nested sub-object with lens composition") {
    assertEquals((lensAC andThen lensCD >=> lensD).get(sample), "hello")
  }

  test("Setting.should be able to set a sub-object") {
    assertEquals(lensAB.set(sample, B(20)), sample.copy(b = B(20)))
  }

  test("Setting.should be able to set then get") {
    assertEquals(lensAB.get(lensAB.set(sample, B(50))), B(50))
  }

  test("Setting.should be able to set a more deeply nested sub-object with lens composition") {
    val lens: Lens[A, String] = lensAC andThen lensCD andThen lensD
    val res1                  = lens.set(sample, "world")

    assertEquals(res1.c.d.s, "world")
    assertEquals(lens.get(res1), "world")
  }

  test("Modifying.should be able to modify a value in place") {

    val lens  = lensCD andThen lensD
    val value = C(D("hello"))

    assertEquals(lensCD.modify(value, d => D(d.s + " there")), C(D("hello there")))
    assertEquals(lens.modify(value, _ + ", world!"), C(D("hello, world!")))

  }

  val lens: Lens[B, Int] =
    Lens(
      b => b.i,
      (b, ii) => b.copy(i = ii)
    )

  test("Lens Laws.must be true, that getting and setting a value back, changes nothing.") {
    val x = B(100)

    assertEquals(lens.set(x, lens.get(x)), x)
  }

  test("Lens Laws.must be true, that setting and then getting returns what I set.") {
    assertEquals(lens.get(lens.set(B(100), 50)), 50)
  }

  test("Lens Laws.must be true, that setting twice and then getting returns the last value (no history)") {
    assertEquals(lens.get(lens.set(lens.set(B(100), 50), 25)), 25)
  }

}

final case class A(b: B, c: C)
final case class B(i: Int)
final case class C(d: D)
final case class D(s: String)
