package indigoexts.temporal

import indigo.GameTime

sealed trait TemporalPredicate {
  val valueAt: GameTime => Boolean
}

object TemporalPredicate {

  case class Test(valueAt: GameTime => Boolean) extends TemporalPredicate

  case object True extends TemporalPredicate {
    val valueAt: GameTime => Boolean = _ => true
  }

  case object False extends TemporalPredicate {
    val valueAt: GameTime => Boolean = _ => false
  }

  case class TrueDuring(startTime: Double, endTime: Double) extends TemporalPredicate {
    val valueAt: GameTime => Boolean = gameTime =>
      gameTime.running >= startTime && gameTime.running <= endTime

  }

}
