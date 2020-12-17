package indigo.shared

import indigo.shared.events.GlobalEvent

import scala.annotation.tailrec

final case class Outcome[+A](state: A, globalEvents: List[GlobalEvent]) {

  def addGlobalEvents(newEvents: GlobalEvent*): Outcome[A] =
    addGlobalEvents(newEvents.toList)

  def addGlobalEvents(newEvents: List[GlobalEvent]): Outcome[A] =
    this.copy(globalEvents = globalEvents ++ newEvents)

  def createGlobalEvents(f: A => List[GlobalEvent]): Outcome[A] =
    this.copy(globalEvents = globalEvents ++ f(state))

  def clearGlobalEvents: Outcome[A] =
    this.copy(globalEvents = Nil)

  def replaceGlobalEvents(f: List[GlobalEvent] => List[GlobalEvent]): Outcome[A] =
    this.copy(globalEvents = f(globalEvents))

  def mapAll[B](f: A => B, g: List[GlobalEvent] => List[GlobalEvent]): Outcome[B] =
    Outcome(f(state), g(globalEvents))

  def map[B](f: A => B): Outcome[B] =
    this.copy(state = f(state))

  def mapGlobalEvents(f: GlobalEvent => GlobalEvent): Outcome[A] =
    this.copy(globalEvents = globalEvents.map(f))

  def ap[B](of: Outcome[A => B]): Outcome[B] =
    map(of.state)

  def merge[B, C](other: Outcome[B])(f: (A, B) => C): Outcome[C] =
    flatMap(a => other.map(b => (a, b))).map(p => f(p._1, p._2))

  def combine[B](other: Outcome[B]): Outcome[(A, B)] =
    Outcome((state, other.state), globalEvents ++ other.globalEvents)

  def flatMap[B](f: A => Outcome[B]): Outcome[B] = {
    val next = f(state)
    this.copy(state = next.state, globalEvents = globalEvents ++ next.globalEvents)
  }

}

object Outcome {

  implicit class ListWithOutcomeSequence[A](val l: List[Outcome[A]]) extends AnyVal {
    def sequence: Outcome[List[A]] =
      Outcome.sequence(l)
  }
  implicit class tuple2Outcomes[A, B](val t: (Outcome[A], Outcome[B])) extends AnyVal {
    def combine: Outcome[(A, B)] =
      t._1.combine(t._2)
    def merge[C](f: (A, B) => C): Outcome[C] =
      t._1.merge(t._2)(f)
    def map2[C](f: (A, B) => C): Outcome[C] =
      merge(f)
  }
  implicit class tuple3Outcomes[A, B, C](val t: (Outcome[A], Outcome[B], Outcome[C])) extends AnyVal {
    def combine: Outcome[(A, B, C)] =
      Outcome((t._1.state, t._2.state, t._3.state), t._1.globalEvents ++ t._2.globalEvents ++ t._3.globalEvents)
    def merge[D](f: (A, B, C) => D): Outcome[D] =
      for {
        aa <- t._1
        bb <- t._2
        cc <- t._3
      } yield f(aa, bb, cc)
    def map3[D](f: (A, B, C) => D): Outcome[D] =
      merge(f)
  }

  def apply[A](state: A): Outcome[A] =
    Outcome[A](state, Nil)

  def sequence[A](l: List[Outcome[A]]): Outcome[List[A]] = {
    @tailrec
    def rec(remaining: List[Outcome[A]], accA: List[A], accEvents: List[GlobalEvent]): Outcome[List[A]] =
      remaining match {
        case Nil =>
          Outcome(accA).addGlobalEvents(accEvents)

        case x :: xs =>
          rec(xs, accA ++ List(x.state), accEvents ++ x.globalEvents)
      }

    rec(l, Nil, Nil)
  }

  def merge[A, B, C](oa: Outcome[A], ob: Outcome[B])(f: (A, B) => C): Outcome[C] =
    oa.merge(ob)(f)
  def map2[A, B, C](oa: Outcome[A], ob: Outcome[B])(f: (A, B) => C): Outcome[C] =
    merge(oa, ob)(f)
  def merge3[A, B, C, D](oa: Outcome[A], ob: Outcome[B], oc: Outcome[C])(f: (A, B, C) => D): Outcome[D] =
    for {
      aa <- oa
      bb <- ob
      cc <- oc
    } yield f(aa, bb, cc)
  def map3[A, B, C, D](oa: Outcome[A], ob: Outcome[B], oc: Outcome[C])(f: (A, B, C) => D): Outcome[D] =
    merge3(oa, ob, oc)(f)

  def combine[A, B](oa: Outcome[A], ob: Outcome[B]): Outcome[(A, B)] =
    oa.combine(ob)
  def combine3[A, B, C](oa: Outcome[A], ob: Outcome[B], oc: Outcome[C]): Outcome[(A, B, C)] =
    Outcome((oa.state, ob.state, oc.state)).addGlobalEvents(oa.globalEvents ++ ob.globalEvents ++ oc.globalEvents)

  def join[A](faa: Outcome[Outcome[A]]): Outcome[A] =
    Outcome(faa.state.state).addGlobalEvents(faa.globalEvents ++ faa.state.globalEvents)
  def flatten[A](faa: Outcome[Outcome[A]]): Outcome[A] =
    join(faa)

}
