package indigoexts.collections

sealed trait HList extends Product with Serializable {
  type HeadType

  val length: Int

  def ::[A](head: A): HList

  private def unstableConcat(remaining: HList, acc: HList): HList =
    remaining match {
      case HNil =>
        acc

      case l: HList.NelHList[_] =>
        unstableConcat(l.tail, l.head :: acc)
    }

  def ++(other: HList): HList =
    unstableConcat(this.reverse, other)

  def reverse: HList =
    this match {
      case HNil =>
        this

      case l: HList.NelHList[_] =>
        unstableConcat(l, HNil)
    }

  def headOption: Option[HeadType]

  def ===(other: HList): Boolean =
    HList.equalityCheck(this, other)

  val tail: HList
}

object HList {

  type Aux[Out] = HList { type HeadType = Out }

  def cons[A](head: A, tail: HList): HList =
    apply[A](head, tail)

  def apply[A](head: A): HList =
    apply(head, HNil)

  def apply[A](head: A, tail: HList): HList =
    Cons(head, tail)

  def unapply(hlist: HList): Option[(hlist.HeadType, HList)] =
    hlist.headOption.map(h => (h, hlist.tail))

  def empty: HList = HNil

  def fromList[A](list: List[A]): HList =
    list.reverse.foldLeft(HList.empty)((acc, n) => n :: acc)

  def equalityCheck(a: HList, b: HList): Boolean = {
    def rec(remainingA: HList, remainingB: HList): Boolean =
      (remainingA, remainingB) match {
        case (HNil, HNil) =>
          true

        case (HList(ax, axs), HList(bx, bxs)) if ax == bx =>
          rec(axs, bxs)

        case _ =>
          false
      }

    rec(a, b)
  }

  sealed trait NelHList[T] extends HList {
    type HeadType = T
    val head: HeadType
    val tail: HList

    def ::[A](head: A): HList =
      HList.cons(head, this)

    def headOption: Option[HeadType] =
      Some(head)
  }

  private final case class Cons[A](first: A, tail: HList) extends NelHList[A] {
    val head: HeadType = first
    val length: Int    = tail.length + 1
  }
}

sealed trait HNil extends HList

case object HNil extends HNil {
  type HeadType = HNil

  val length: Int = 0

  def ::[A](head: A): HList =
    HList.cons(head, HNil)

  val tail: HList = HNil

  def headOption: Option[HeadType] = None
}
