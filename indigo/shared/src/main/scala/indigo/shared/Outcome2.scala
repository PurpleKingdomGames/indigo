package indigo.shared

import indigo.shared.events.GlobalEvent

import scala.util.control.NonFatal

import scala.annotation.tailrec
import scala.deprecated

sealed trait Outcome2[+A] {

  def isResult: Boolean
  def isError: Boolean

  @deprecated("The state field is now unsafe, use unsafeGet or getOrElse.")
  def state[B >: A]: B = unsafeGet
  def unsafeGet[B >: A]: B
  def getOrElse[B >: A](b: B): B

  @deprecated("The globalEvents field is now unsafe, use unsafeGlobalEvents or globalEventsOrNil.")
  def globalEvents: List[GlobalEvent] = unsafeGlobalEvents
  def unsafeGlobalEvents: List[GlobalEvent]
  def globalEventsOrNil: List[GlobalEvent]

  def raiseError(throwable: Throwable): Outcome2.Error = Outcome2.Error(throwable)

  def handleError[B >: A](recoverWith: Throwable => Outcome2[B]): Outcome2[B]

  def logCrash(log: Throwable => Unit): Unit

  def addGlobalEvents(newEvents: GlobalEvent*): Outcome2[A]

  def addGlobalEvents(newEvents: => List[GlobalEvent]): Outcome2[A]

  def createGlobalEvents(f: A => List[GlobalEvent]): Outcome2[A]

  def clearGlobalEvents: Outcome2[A]

  def replaceGlobalEvents(f: List[GlobalEvent] => List[GlobalEvent]): Outcome2[A]

  def mapAll[B](f: A => B, g: List[GlobalEvent] => List[GlobalEvent]): Outcome2[B]

  def map[B](f: A => B): Outcome2[B]

  def mapGlobalEvents(f: GlobalEvent => GlobalEvent): Outcome2[A]

  def ap[B](of: Outcome2[A => B]): Outcome2[B]

  def merge[B, C](other: Outcome2[B])(f: (A, B) => C): Outcome2[C]

  def combine[B](other: Outcome2[B]): Outcome2[(A, B)]

  def flatMap[B](f: A => Outcome2[B]): Outcome2[B]
}
object Outcome2 {

  final case class Result[A](_state: () => A, _globalEvents: () => List[GlobalEvent]) extends Outcome2[A] {

    def isResult: Boolean = true
    def isError: Boolean  = false

    def unsafeGet[B >: A]: B =
      _state()
    def getOrElse[B >: A](b: B): B =
      _state()

    def unsafeGlobalEvents: List[GlobalEvent] =
      _globalEvents()
    def globalEventsOrNil: List[GlobalEvent] =
      _globalEvents()

    def handleError[B >: A](recoverWith: Throwable => Outcome2[B]): Outcome2[B] =
      this

    def logCrash(log: Throwable => Unit): Unit =
      ()

    def addGlobalEvents(newEvents: GlobalEvent*): Outcome2[A] =
      addGlobalEvents(newEvents.toList)

    def addGlobalEvents(newEvents: => List[GlobalEvent]): Outcome2[A] =
      Outcome2(_state(), _globalEvents() ++ newEvents)

    def createGlobalEvents(f: A => List[GlobalEvent]): Outcome2[A] =
      Outcome2(_state(), _globalEvents() ++ f(_state()))

    def clearGlobalEvents: Outcome2[A] =
      Outcome2(_state())

    def replaceGlobalEvents(f: List[GlobalEvent] => List[GlobalEvent]): Outcome2[A] =
      Outcome2(_state(), f(_globalEvents()))

    def mapAll[B](f: A => B, g: List[GlobalEvent] => List[GlobalEvent]): Outcome2[B] =
      Outcome2(f(_state()), g(_globalEvents()))

    def map[B](f: A => B): Outcome2[B] =
      Outcome2(f(_state()), _globalEvents())

    def mapGlobalEvents(f: GlobalEvent => GlobalEvent): Outcome2[A] =
      Outcome2(_state(), _globalEvents().map(f))

    def ap[B](of: Outcome2[A => B]): Outcome2[B] =
      of match {
        case Error(e) =>
          Error(e)

        case Result(s, es) =>
          map(s()).addGlobalEvents(es())
      }

    def merge[B, C](other: Outcome2[B])(f: (A, B) => C): Outcome2[C] =
      flatMap(a => other.map(b => (a, b))).map(p => f(p._1, p._2))

    def combine[B](other: Outcome2[B]): Outcome2[(A, B)] =
      other match {
        case Error(e) =>
          Error(e)

        case Result(s, es) =>
          Outcome2((_state(), s()), _globalEvents() ++ es())
      }

    def flatMap[B](f: A => Outcome2[B]): Outcome2[B] =
      f(_state()) match {
        case Error(e) =>
          Error(e)

        case Result(s, es) =>
          Outcome2(s(), _globalEvents() ++ es())
      }

  }

  final case class Error(e: Throwable) extends Outcome2[Nothing] {

    def isResult: Boolean = false
    def isError: Boolean  = true

    def unsafeGet[B >: Nothing]: B =
      throw e
    def getOrElse[B >: Nothing](b: B): B =
      b

    def unsafeGlobalEvents: List[GlobalEvent] =
      throw e
    def globalEventsOrNil: List[GlobalEvent] =
      Nil

    def handleError[B >: Nothing](recoverWith: Throwable => Outcome2[B]): Outcome2[B] =
      recoverWith(e)

    def logCrash(log: Throwable => Unit): Unit = {
      log(e)
      throw e
    }

    def addGlobalEvents(newEvents: GlobalEvent*): Error                              = this
    def addGlobalEvents(newEvents: => List[GlobalEvent]): Error                      = this
    def createGlobalEvents(f: Nothing => List[GlobalEvent]): Error                   = this
    def clearGlobalEvents: Error                                                     = this
    def replaceGlobalEvents(f: List[GlobalEvent] => List[GlobalEvent]): Error        = this
    def mapAll[B](f: Nothing => B, g: List[GlobalEvent] => List[GlobalEvent]): Error = this
    def map[B](f: Nothing => B): Error                                               = this
    def mapGlobalEvents(f: GlobalEvent => GlobalEvent): Error                        = this
    def ap[B](of: Outcome2[Nothing => B]): Outcome2[B]                               = this
    def merge[B, C](other: Outcome2[B])(f: (Nothing, B) => C): Error                 = this
    def combine[B](other: Outcome2[B]): Error                                        = this
    def flatMap[B](f: Nothing => Outcome2[B]): Error                                 = this

  }

  implicit class ListWithOutcomeSequence[A](val l: List[Outcome2[A]]) extends AnyVal {
    def sequence: Outcome2[List[A]] =
      Outcome2.sequence(l)
  }
  implicit class tuple2Outcomes[A, B](val t: (Outcome2[A], Outcome2[B])) extends AnyVal {
    def combine: Outcome2[(A, B)] =
      t._1.combine(t._2)
    def merge[C](f: (A, B) => C): Outcome2[C] =
      t._1.merge(t._2)(f)
    def map2[C](f: (A, B) => C): Outcome2[C] =
      merge(f)
  }
  implicit class tuple3Outcomes[A, B, C](val t: (Outcome2[A], Outcome2[B], Outcome2[C])) extends AnyVal {
    def combine: Outcome2[(A, B, C)] =
      t match {
        case (Result(s1, es1), Result(s2, es2), Result(s3, es3)) =>
          Outcome2((s1(), s2(), s3()), es1() ++ es2() ++ es3())

        case (Error(e), _, _) =>
          Error(e)

        case (_, Error(e), _) =>
          Error(e)

        case (_, _, Error(e)) =>
          Error(e)
      }
    def merge[D](f: (A, B, C) => D): Outcome2[D] =
      for {
        aa <- t._1
        bb <- t._2
        cc <- t._3
      } yield f(aa, bb, cc)
    def map3[D](f: (A, B, C) => D): Outcome2[D] =
      merge(f)
  }

  def apply[A](state: => A): Outcome2[A] =
    try Outcome2.Result[A](() => state, () => Nil)
    catch {
      case NonFatal(e) =>
        Outcome2.Error(e)
    }

  def apply[A](state: => A, globalEvents: => List[GlobalEvent]): Outcome2[A] =
    try Outcome2.Result[A](() => state, () => globalEvents)
    catch {
      case NonFatal(e) =>
        Outcome2.Error(e)
    }

  def unapply[A](outcome: Outcome2[A]): Option[(A, List[GlobalEvent])] =
    outcome match {
      case Outcome2.Error(_) =>
        None

      case Outcome2.Result(s, es) =>
        Some((s(), es()))
    }

  def sequence[A](l: List[Outcome2[A]]): Outcome2[List[A]] = {
    @tailrec
    def rec(remaining: List[Outcome2[A]], accA: List[A], accEvents: List[GlobalEvent]): Outcome2[List[A]] =
      remaining match {
        case Nil =>
          Outcome2(accA).addGlobalEvents(accEvents)

        case Error(e) :: _ =>
          Error(e)

        case Result(s, es) :: xs =>
          rec(xs, accA ++ List(s()), accEvents ++ es())
      }

    rec(l, Nil, Nil)
  }

  def merge[A, B, C](oa: Outcome2[A], ob: Outcome2[B])(f: (A, B) => C): Outcome2[C] =
    oa.merge(ob)(f)
  def map2[A, B, C](oa: Outcome2[A], ob: Outcome2[B])(f: (A, B) => C): Outcome2[C] =
    merge(oa, ob)(f)
  def merge3[A, B, C, D](oa: Outcome2[A], ob: Outcome2[B], oc: Outcome2[C])(f: (A, B, C) => D): Outcome2[D] =
    for {
      aa <- oa
      bb <- ob
      cc <- oc
    } yield f(aa, bb, cc)
  def map3[A, B, C, D](oa: Outcome2[A], ob: Outcome2[B], oc: Outcome2[C])(f: (A, B, C) => D): Outcome2[D] =
    merge3(oa, ob, oc)(f)

  def combine[A, B](oa: Outcome2[A], ob: Outcome2[B]): Outcome2[(A, B)] =
    oa.combine(ob)
  def combine3[A, B, C](oa: Outcome2[A], ob: Outcome2[B], oc: Outcome2[C]): Outcome2[(A, B, C)] =
    (oa, ob, oc) match {
      case (Result(s1, es1), Result(s2, es2), Result(s3, es3)) =>
        Outcome2((s1(), s2(), s3()), es1() ++ es2() ++ es3())

      case (Error(e), _, _) =>
        Error(e)

      case (_, Error(e), _) =>
        Error(e)

      case (_, _, Error(e)) =>
        Error(e)
    }

  def join[A](faa: Outcome2[Outcome2[A]]): Outcome2[A] =
    faa match {
      case Error(e) =>
        Error(e)

      case Result(outcome, es) =>
        val next = outcome()
        Outcome2(next.unsafeGet, es() ++ next.unsafeGlobalEvents)
    }
  def flatten[A](faa: Outcome2[Outcome2[A]]): Outcome2[A] =
    join(faa)

}
