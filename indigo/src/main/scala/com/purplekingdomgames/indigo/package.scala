package com.purplekingdomgames

import com.purplekingdomgames.indigo.runtime.{IndigoLogger, Show}

package object indigo {

  val logger: IndigoLogger.type = IndigoLogger

  implicit class WithShow[T](val t: T) extends AnyVal {
    def show(implicit showMe: Show[T]): String = showMe.show(t)
  }

}
