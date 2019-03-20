package indigo.gameengine

import indigo.runtime.AsString
import indigo.abstractions.Monad
import indigo.gameengine.events.{GlobalEvent, InFrameEvent}
import indigo.shared.IndigoEq

import scala.annotation.tailrec

final class Outcome[A](val state: A, val globalEvents: List[GlobalEvent], val inFrameEvents: List[InFrameEvent]) {

  def addGlobalEvents(newEvents: GlobalEvent*): Outcome[A] =
    Outcome.addGlobalEvents(this, newEvents.toList)

  def addGlobalEvents(newEvents: List[GlobalEvent]): Outcome[A] =
    Outcome.addGlobalEvents(this, newEvents)

  def addInFrameEvents(events: InFrameEvent*): Outcome[A] =
    addInFrameEvents(events.toList)

  def addInFrameEvents(events: List[InFrameEvent]): Outcome[A] =
    Outcome.addInFrameEvents(this, inFrameEvents ++ events)

  def createGlobalEvents(f: A => List[GlobalEvent]): Outcome[A] =
    Outcome.createGlobalEvents(this, f)

  def createInFrameEvents(f: A => List[InFrameEvent]): Outcome[A] =
    Outcome.createInFrameEvents(this, f)

  def mapState[B](f: A => B): Outcome[B] =
    Outcome.mapState(this)(f)

  def mapGlobalEvents[B](f: List[GlobalEvent] => List[GlobalEvent]): Outcome[A] =
    Outcome.mapGlobalEvents(this)(f)

  def mapInFrameEvents[B](f: List[InFrameEvent] => List[InFrameEvent]): Outcome[A] =
    Outcome.mapInFrameEvents(this)(f)

  def mapAll[B](f: A => B, g: List[GlobalEvent] => List[GlobalEvent], h: List[InFrameEvent] => List[InFrameEvent]): Outcome[B] =
    Outcome.mapAll(this)(f, g, h)

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
  implicit val showGlobalEvent: AsString[GlobalEvent] =
    AsString.create(_.toString)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val eqGlobalEvent: IndigoEq[GlobalEvent] =
    IndigoEq.create(_ == _)

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

  implicit def eq[A](implicit eqA: IndigoEq[A], eqE: IndigoEq[List[GlobalEvent]]): IndigoEq[Outcome[A]] =
    IndigoEq.create { (a, b) =>
      eqA.equal(a.state, b.state) && eqE.equal(a.globalEvents, b.globalEvents)
    }

  implicit def show[A](implicit as: AsString[A], ae: AsString[List[GlobalEvent]]): AsString[Outcome[A]] =
    AsString.create { outcomeA =>
      s"Outcome(${as.show(outcomeA.state)}, ${ae.show(outcomeA.globalEvents)})"
    }

  def apply[A](state: A): Outcome[A] =
    pure(state)

  def pure[A](state: A): Outcome[A] =
    new Outcome[A](state, Nil, Nil)

  def unapply[A](outcome: Outcome[A]): Option[(A, List[GlobalEvent])] =
    Option((outcome.state, outcome.globalEvents))

  def addGlobalEvents[A](o: Outcome[A], newEvents: List[GlobalEvent]): Outcome[A] =
    new Outcome(o.state, o.globalEvents ++ newEvents, o.inFrameEvents)

  def addInFrameEvents[A](o: Outcome[A], newEvents: List[InFrameEvent]): Outcome[A] =
    new Outcome(o.state, o.globalEvents, o.inFrameEvents ++ newEvents)

  def createGlobalEvents[A](o: Outcome[A], f: A => List[GlobalEvent]): Outcome[A] =
    new Outcome(o.state, o.globalEvents ++ f(o.state), o.inFrameEvents)

  def createInFrameEvents[A](o: Outcome[A], f: A => List[InFrameEvent]): Outcome[A] =
    new Outcome(o.state, o.globalEvents, o.inFrameEvents ++ f(o.state))

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
    mapAll(oa)(f, identity, identity)

  def mapGlobalEvents[A](oa: Outcome[A])(f: List[GlobalEvent] => List[GlobalEvent]): Outcome[A] =
    mapAll(oa)(identity, f, identity)

  def mapInFrameEvents[A](oa: Outcome[A])(f: List[InFrameEvent] => List[InFrameEvent]): Outcome[A] =
    mapAll(oa)(identity, identity, f)

  def mapAll[A, B](oa: Outcome[A])(f: A => B, g: List[GlobalEvent] => List[GlobalEvent], h: List[InFrameEvent] => List[InFrameEvent]): Outcome[B] =
    Outcome(f(oa.state))
      .addGlobalEvents(g(oa.globalEvents))
      .addInFrameEvents(h(oa.inFrameEvents))

  def apState[A, B](oa: Outcome[A])(of: Outcome[A => B]): Outcome[B] =
    oa.mapState(of.state)

  def map2[A, B, C](t: (Outcome[A], Outcome[B]))(f: ((A, B)) => C): Outcome[C] =
    apState(combine(t._1, t._2))(pure(f))

  def combine[A, B](oa: Outcome[A], ob: Outcome[B]): Outcome[(A, B)] =
    Outcome((oa.state, ob.state)).addGlobalEvents(oa.globalEvents ++ ob.globalEvents)

  def join[A](faa: Outcome[Outcome[A]]): Outcome[A] =
    faa.state.addGlobalEvents(faa.globalEvents)

  def flatMapState[A, B](fa: Outcome[A])(f: A => Outcome[B]): Outcome[B] =
    join(mapState(fa)(f))

}
