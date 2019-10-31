package indigoexts.primitives

import indigo.shared.EqualTo._

final class UpdateList[A](list: List[UpdateList.Updateable[A]], pattern: UpdateList.Pattern) {

  def update(f: A => A): UpdateList[A] =
    new UpdateList(
      list.map { x =>
        if (pattern(x)) new UpdateList.Updateable(f(x.value), x.updateCount + 1, x.position)
        else x
      },
      pattern
    )

  def withPattern(newPattern: UpdateList.Pattern): UpdateList[A] =
    new UpdateList[A](list, newPattern)

  def toList: List[A] =
    list.map(_.value)
}

object UpdateList {

  final class Updateable[A](val value: A, val updateCount: Long, val position: Long)

  def apply[A](l: List[A]): UpdateList[A] = {
    val inits: List[(Long, Long)] =
      List.fill(l.length)(0L).zipWithIndex.map(p => (p._1, p._2.toLong))

    new UpdateList(
      tupledToUpdateable(l.zip(inits)),
      Pattern.none
    )
  }

  def tupledToUpdateable[A]: List[(A, (Long, Long))] => List[Updateable[A]] =
    _.map { p =>
      new Updateable[A](p._1, p._2._1, p._2._2)
    }

  type Pattern = Updateable[_] => Boolean

  object Pattern {

    val none: Pattern =
      _ => true

    def interleave(every: Int): Pattern =
      _.position % every === 0

  }

}
