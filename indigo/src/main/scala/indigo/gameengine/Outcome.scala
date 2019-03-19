package indigo.gameengine

import indigo._
import indigo.abstractions.Monad
import scala.annotation.tailrec

final class Outcome[A](val state: A, val events: List[GlobalEvent]) {

  def addEvents(newEvents: GlobalEvent*): Outcome[A] =
    Outcome.addEvents(this, newEvents.toList)

  def addEvents(newEvents: List[GlobalEvent]): Outcome[A] =
    Outcome.addEvents(this, newEvents)

  def createEvents(f: A => List[GlobalEvent]): Outcome[A] =
    Outcome.createEvents(this, f)

  def mapState[B](f: A => B): Outcome[B] =
    Outcome.mapState(this)(f)

  def mapEvents[B](f: List[GlobalEvent] => List[GlobalEvent]): Outcome[A] =
    Outcome.mapEvents(this)(f)

  def mapAll[B](f: A => B, g: List[GlobalEvent] => List[GlobalEvent]): Outcome[B] =
    Outcome.mapAll(this)(f, g)

  def apState[B](of: Outcome[A => B]): Outcome[B] =
    Outcome.apState(this)(of)

  def |+|[B](other: Outcome[B]): Outcome[(A, B)] =
    Outcome.combine(this, other)

  def flatMapState[B](f: A => Outcome[B]): Outcome[B] =
    Outcome.flatMapState(this)(f)

}

object Outcome {

  implicit class ListWithOutcomeSequence[A](val l: List[Outcome[A]]) extends AnyVal {
    def sequence: Outcome[List[A]] =
      Outcome.sequence(l)
  }

  implicit class tupleOutcomesMap2[A, B, C](val t: (Outcome[A], Outcome[B])) extends AnyVal {
    def map2(f: ((A, B)) => C): Outcome[C] =
      Outcome.map2(t)(f)
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  implicit val showGlobalEvent: Show[GlobalEvent] =
    Show.create(_.toString)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqGlobalEvent: Eq[GlobalEvent] =
    Eq.create(_ == _)

  implicit val monad: Monad[Outcome] =
    new Monad[Outcome] {

      def pure[A](a: A): Outcome[A] =
        Outcome.pure(a)

      def map[A, B](fa: Outcome[A])(f: A => B): Outcome[B] =
        Outcome.mapState(fa)(f)

      def ap[A, B](fa: Outcome[A])(f: Outcome[A => B]): Outcome[B] =
        Outcome.apState(fa)(f)

      def flatMap[A, B](fa: Outcome[A])(f: A => Outcome[B]): Outcome[B] =
        Outcome.flatMapState(fa)(f)

    }

  implicit def eq[A](implicit eqA: Eq[A], eqE: Eq[List[GlobalEvent]]): Eq[Outcome[A]] =
    Eq.create { (a, b) =>
      eqA.equal(a.state, b.state) && eqE.equal(a.events, b.events)
    }

  implicit def show[A](implicit as: Show[A], ae: Show[List[GlobalEvent]]): Show[Outcome[A]] =
    Show.create { outcomeA =>
      s"Outcome(${as.show(outcomeA.state)}, ${ae.show(outcomeA.events)})"
    }

  def apply[A](state: A): Outcome[A] =
    pure(state)

  def pure[A](state: A): Outcome[A] =
    new Outcome[A](state, Nil)

  def unapply[A](outcome: Outcome[A]): Option[(A, List[GlobalEvent])] =
    Option((outcome.state, outcome.events))

  def addEvents[A](o: Outcome[A], newEvents: List[GlobalEvent]): Outcome[A] =
    new Outcome(o.state, o.events ++ newEvents)

  def createEvents[A](o: Outcome[A], f: A => List[GlobalEvent]): Outcome[A] =
    new Outcome(o.state, o.events ++ f(o.state))

  def sequence[A](l: List[Outcome[A]]): Outcome[List[A]] = {
    @tailrec
    def rec(remaining: List[Outcome[A]], accA: List[A], accEvents: List[GlobalEvent]): Outcome[List[A]] =
      remaining match {
        case Nil =>
          Outcome(accA).addEvents(accEvents)

        case x :: xs =>
          rec(xs, accA :+ x.state, accEvents ++ x.events)
      }

    rec(l, Nil, Nil)
  }

  def mapState[A, B](oa: Outcome[A])(f: A => B): Outcome[B] =
    mapAll(oa)(f, identity)

  def mapEvents[A](oa: Outcome[A])(f: List[GlobalEvent] => List[GlobalEvent]): Outcome[A] =
    mapAll(oa)(identity, f)

  def mapAll[A, B](oa: Outcome[A])(f: A => B, g: List[GlobalEvent] => List[GlobalEvent]): Outcome[B] =
    Outcome(f(oa.state)).addEvents(g(oa.events))

  def apState[A, B](oa: Outcome[A])(of: Outcome[A => B]): Outcome[B] =
    oa.mapState(of.state)

  def map2[A, B, C](t: (Outcome[A], Outcome[B]))(f: ((A, B)) => C): Outcome[C] =
    apState(combine(t._1, t._2))(pure(f))

  def combine[A, B](oa: Outcome[A], ob: Outcome[B]): Outcome[(A, B)] =
    Outcome((oa.state, ob.state)).addEvents(oa.events ++ ob.events)

  def join[A](faa: Outcome[Outcome[A]]): Outcome[A] =
    faa.state.addEvents(faa.events)

  def flatMapState[A, B](fa: Outcome[A])(f: A => Outcome[B]): Outcome[B] =
    join(mapState(fa)(f))

}
