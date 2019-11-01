package indigoexts.primitives

import indigo.shared.EqualTo._

sealed trait UpdatePattern {
  def update[A, B](value: A, f: A => B, default: B, position: Int): B
  def step: UpdatePattern
}

object UpdatePattern {

  case object Constant extends UpdatePattern {
    def update[A, B](value: A, f: A => B, default: B, position: Int): B =
      f(value)

    def step: UpdatePattern = this
  }

  final class Interleave(flip: Boolean) extends UpdatePattern {
    def update[A, B](value: A, f: A => B, default: B, position: Int): B =
      if (position % 2 === 0 ^ flip) f(value) else default

    def step: UpdatePattern =
      new Interleave(!flip)
  }
  object Interleave {
    def apply(): Interleave =
      new Interleave(false)
  }

  final class Every(every: Int, count: Int) extends UpdatePattern {
    def update[A, B](value: A, f: A => B, default: B, position: Int): B =
      if ((position - count) % every === 0) f(value) else default

    def step: UpdatePattern =
      new Every(every, (count + 1) % every)
  }
  object Every {
    def apply(every: Int): Every =
      new Every(every, 0)
  }

  final class Batch(size: Int, count: Int) extends UpdatePattern {
    def update[A, B](value: A, f: A => B, default: B, position: Int): B =
      if (((position / size) - count) % size === 0) f(value) else default

    def step: UpdatePattern =
      new Batch(size, (count + 1) % size)
  }
  object Batch {
    def apply(size: Int): Batch =
      new Batch(size, 0)
  }

}
