package indigoextras.datatypes

final class UpdateList[A](list: List[A], pattern: UpdatePattern):

  def update(f: A => A): UpdateList[A] =
    val (v, p) = UpdateList.updateList(list, f, pattern)
    new UpdateList(v, p)

  def withPattern(newPattern: UpdatePattern): UpdateList[A] =
    new UpdateList[A](list, newPattern)

  def prepend(a: A): UpdateList[A] =
    new UpdateList[A](a :: list, pattern)

  def append(a: A): UpdateList[A] =
    new UpdateList[A](list ++ List(a), pattern)

  def replaceList(newList: List[A]): UpdateList[A] =
    new UpdateList[A](newList, pattern)

  def toList: List[A] =
    list

  def size: Int =
    list.size

object UpdateList:

  def apply[A](l: List[A]): UpdateList[A] =
    new UpdateList(l, UpdatePattern.Constant)

  def empty[A]: UpdateList[A] =
    apply(Nil)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  def updateList[A](l: List[A], f: A => A, pattern: UpdatePattern): (List[A], UpdatePattern) =
    var i: Int                   = 0
    val res: scalajs.js.Array[A] = new scalajs.js.Array[A]()

    while (i < l.length) {
      val v = l(i)
      res.insert(i, pattern.update(v, f, v, i))

      i = i + 1
    }

    (res.toList, pattern.step)

sealed trait UpdatePattern:
  def update[A, B](value: A, f: A => B, default: B, position: Int): B
  def step: UpdatePattern

object UpdatePattern:

  case object Constant extends UpdatePattern:
    def update[A, B](value: A, f: A => B, default: B, position: Int): B =
      f(value)

    def step: UpdatePattern = this

  final class Interleave(flip: Boolean) extends UpdatePattern:
    def update[A, B](value: A, f: A => B, default: B, position: Int): B =
      if (position % 2 == 0 ^ flip) f(value) else default

    def step: UpdatePattern =
      new Interleave(!flip)
  object Interleave:
    def apply(): Interleave =
      new Interleave(false)

  final class Every(every: Int, count: Int) extends UpdatePattern:
    def update[A, B](value: A, f: A => B, default: B, position: Int): B =
      if ((position - count) % every == 0) f(value) else default

    def step: UpdatePattern =
      new Every(every, (count + 1) % every)
  object Every:
    def apply(every: Int): Every =
      new Every(every, 0)

  final class Batch(size: Int, count: Int) extends UpdatePattern:
    def update[A, B](value: A, f: A => B, default: B, position: Int): B =
      if (((position / size) - count) % size == 0) f(value) else default

    def step: UpdatePattern =
      new Batch(size, (count + 1) % size)
  object Batch:
    def apply(size: Int): Batch =
      new Batch(size, 0)
