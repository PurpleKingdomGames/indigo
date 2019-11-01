package indigoexts.primitives

import indigo.shared.EqualTo._
import scala.collection.mutable.ListBuffer

final class UpdateList[A](list: List[A], pattern: UpdateList.Pattern) {

  def update(f: A => A): UpdateList[A] = {
    val (v, p) = UpdateList.updateList(list, f, pattern)

    new UpdateList(v, p)
  }

  def withPattern(newPattern: UpdateList.Pattern): UpdateList[A] =
    new UpdateList[A](list, newPattern)

  def toList: List[A] =
    list
}

object UpdateList {

  def apply[A](l: List[A]): UpdateList[A] =
    new UpdateList(
      l,
      Pattern.Constant
    )

  @SuppressWarnings(Array("org.wartremover.warts.While", "org.wartremover.warts.Var", "org.wartremover.warts.MutableDataStructures"))
  def updateList[A](l: List[A], f: A => A, pattern: Pattern): (List[A], Pattern) = {
    var i: Int             = 0
    val res: ListBuffer[A] = new ListBuffer[A]

    while (i < l.length) {
      res.insert(i, pattern.update(l(i), f, i))

      i = i + 1
    }

    (res.toList, pattern.step)
  }

  sealed trait Pattern {
    def update[A](value: A, f: A => A, position: Int): A
    def step: Pattern
  }

  object Pattern {

    case object Constant extends Pattern {
      def update[A](value: A, f: A => A, position: Int): A =
        f(value)

      def step: Pattern = this
    }

    final class Interleave(flip: Boolean) extends Pattern {
      def update[A](value: A, f: A => A, position: Int): A =
        if (position % 2 === 0 ^ flip) f(value) else value

      def step: Pattern =
        new Interleave(!flip)
    }
    object Interleave {
      def apply(): Interleave =
        new Interleave(false)
    }

    final class Every(every: Int, count: Int) extends Pattern {
      def update[A](value: A, f: A => A, position: Int): A =
        if ((position - count) % every === 0) f(value) else value

      def step: Pattern =
        new Every(every, (count + 1) % every)
    }
    object Every {
      def apply(every: Int): Every =
        new Every(every, 0)
    }

    final class Batch(size: Int, count: Int) extends Pattern {
      def update[A](value: A, f: A => A, position: Int): A =
        if (((position / size) - count) % size === 0) f(value) else value

      def step: Pattern =
        new Batch(size, (count + 1) % size)
    }
    object Batch {
      def apply(size: Int): Batch =
        new Batch(size, 0)
    }

  }
}
