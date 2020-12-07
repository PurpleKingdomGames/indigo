// package indigo.shared.temporal

// import indigo.shared.time.Seconds

// trait SignalType[A] {
//   def merge[B, C](other: SignalType[B])(f: (A, B) => C): SignalType[C]

//   def |>[B](sf: SignalFunction[A, B]): SignalType[B]
//   def pipe[B](sf: SignalFunction[A, B]): SignalType[B]

//   def |*|[B](other: SignalType[B]): SignalType[(A, B)]
//   def combine[B](other: SignalType[B]): SignalType[(A, B)]

//   def clampTime(from: Seconds, to: Seconds): SignalType[A]
//   def wrapTime(at: Seconds): SignalType[A]
//   def affectTime(multiplyBy: Double): SignalType[A]

//   def map[B](f: A => B): SignalType[B]
//   def ap[B](f: SignalType[A => B]): SignalType[B]
//   def flatMap[B](f: A => SignalType[B]): SignalType[B]
// }
