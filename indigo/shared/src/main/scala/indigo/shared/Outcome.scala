package indigo.shared

import indigo.shared.events.GlobalEvent

import scala.annotation.tailrec

final class Outcome[+A](val state: A, val globalEvents: List[GlobalEvent]) {

  def addGlobalEvents(newEvents: GlobalEvent*): Outcome[A] =
    Outcome.addGlobalEvents(this, newEvents.toList)

  def addGlobalEvents(newEvents: List[GlobalEvent]): Outcome[A] =
    Outcome.addGlobalEvents(this, newEvents)

  def createGlobalEvents(f: A => List[GlobalEvent]): Outcome[A] =
    Outcome.createGlobalEvents(this, f)

  def mapState[B](f: A => B): Outcome[B] =
    Outcome.mapState(this)(f)

  def map[B](f: A => B): Outcome[B] =
    mapState(f)

  def mapGlobalEvents[B](f: List[GlobalEvent] => List[GlobalEvent]): Outcome[A] =
    Outcome.mapGlobalEvents(this)(f)

  def mapAll[B](f: A => B, g: List[GlobalEvent] => List[GlobalEvent]): Outcome[B] =
    Outcome.mapAll(this)(f, g)

  def apState[B](of: Outcome[A => B]): Outcome[B] =
    Outcome.apState(this)(of)

  def ap[B](of: Outcome[A => B]): Outcome[B] =
    apState(of)

  def merge[B, C](other: Outcome[B])(f: (A, B) => C): Outcome[C] =
    Outcome.merge(this, other)(f)

  def |+|[B](other: Outcome[B]): Outcome[(A, B)] =
    Outcome.combine(this, other)

  def flatMapState[B](f: A => Outcome[B]): Outcome[B] =
    Outcome.flatMapState(this)(f)

  def flatMap[B](f: A => Outcome[B]): Outcome[B] =
    flatMapState(f)

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  override def toString(): String =
    s"Outcome(${state.toString()}, ${globalEvents.toString()})"

}

object Outcome {

  implicit class ListWithOutcomeSequence[A](val l: List[Outcome[A]]) extends AnyVal {
    def sequence: Outcome[List[A]] =
      Outcome.sequence(l)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqGlobalEvent: EqualTo[GlobalEvent] =
    EqualTo.create(_ == _)

  implicit def eq[A](implicit eqA: EqualTo[A], eqE: EqualTo[List[GlobalEvent]]): EqualTo[Outcome[A]] =
    EqualTo.create { (a, b) =>
      eqA.equal(a.state, b.state) && eqE.equal(a.globalEvents, b.globalEvents)
    }

  def apply[A](state: A, globalEvents: List[GlobalEvent]): Outcome[A] =
    new Outcome(state, globalEvents)

  def apply[A](state: A): Outcome[A] =
    pure(state)

  def pure[A](state: A): Outcome[A] =
    new Outcome[A](state, Nil)

  def unapply[A](outcome: Outcome[A]): Option[(A, List[GlobalEvent])] =
    Option((outcome.state, outcome.globalEvents))

  def addGlobalEvents[A](o: Outcome[A], newEvents: List[GlobalEvent]): Outcome[A] =
    new Outcome(o.state, o.globalEvents ++ newEvents)

  def createGlobalEvents[A](o: Outcome[A], f: A => List[GlobalEvent]): Outcome[A] =
    new Outcome(o.state, o.globalEvents ++ f(o.state))

  def sequence[A](l: List[Outcome[A]]): Outcome[List[A]] = {
    @tailrec
    def rec(remaining: List[Outcome[A]], accA: List[A], accEvents: List[GlobalEvent]): Outcome[List[A]] =
      remaining match {
        case Nil =>
          Outcome(accA).addGlobalEvents(accEvents)

        case x :: xs =>
          rec(xs, accA :+ x.state, accEvents ++ x.globalEvents)
      }

    rec(l, Nil, Nil)
  }

  def mapState[A, B](oa: Outcome[A])(f: A => B): Outcome[B] =
    mapAll(oa)(f, identity)

  def mapGlobalEvents[A](oa: Outcome[A])(f: List[GlobalEvent] => List[GlobalEvent]): Outcome[A] =
    mapAll(oa)(identity, f)

  def mapAll[A, B](oa: Outcome[A])(f: A => B, g: List[GlobalEvent] => List[GlobalEvent]): Outcome[B] =
    Outcome(f(oa.state))
      .addGlobalEvents(g(oa.globalEvents))

  def apState[A, B](oa: Outcome[A])(of: Outcome[A => B]): Outcome[B] =
    oa.mapState(of.state)

  def ap2State[A, B, C](oa: Outcome[A], ob: Outcome[B])(of: Outcome[(A, B) => C]): Outcome[C] =
    apState(combine(oa, ob))(of.mapState(f => (t: (A, B)) => f.curried(t._1)(t._2)))

  def merge[A, B, C](oa: Outcome[A], ob: Outcome[B])(f: (A, B) => C): Outcome[C] =
    oa.flatMap(a => ob.map(b => (a, b))).map(p => f(p._1, p._2))

  def combine[A, B](oa: Outcome[A], ob: Outcome[B]): Outcome[(A, B)] =
    Outcome((oa.state, ob.state)).addGlobalEvents(oa.globalEvents ++ ob.globalEvents)

  def combine3[A, B, C](oa: Outcome[A], ob: Outcome[B], oc: Outcome[C]): Outcome[(A, B, C)] =
    Outcome((oa.state, ob.state, oc.state)).addGlobalEvents(oa.globalEvents ++ ob.globalEvents ++ oc.globalEvents)

  def join[A](faa: Outcome[Outcome[A]]): Outcome[A] =
    Outcome(faa.state.state).addGlobalEvents(faa.globalEvents ++ faa.state.globalEvents)

  def flatten[A](faa: Outcome[Outcome[A]]): Outcome[A] =
    join(faa)

  def flatMapState[A, B](fa: Outcome[A])(f: A => Outcome[B]): Outcome[B] =
    join(mapState(fa)(f))

}
