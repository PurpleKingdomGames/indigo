package indigoextras.interleaved

import indigo.shared.time.GameTime

import scala.collection.immutable.Nil
import scala.annotation.tailrec

final case class LongCompute[ReferenceData, ResultModel](
    reference: ReferenceData,
    result: ResultModel,
    steps: List[MonitoredStep[ReferenceData, ResultModel]],
    unitsToAttempt: Int,
    rateOfChange: Int,
    tolerance: Double
) {

  def isComplete: Boolean =
    steps.isEmpty

  def giveResult: Option[ResultModel] =
    if (isComplete) Some(result) else None

  def giveResultSoFar: ResultModel =
    result

  def update(gameTime: GameTime): LongCompute[ReferenceData, ResultModel] = {
    @tailrec
    def rec(remaining: List[MonitoredStep[ReferenceData, ResultModel]], unitsDone: Int, acc: Option[ResultModel]): LongCompute[ReferenceData, ResultModel] =
      remaining match {
        case Nil =>
          this.copy[ReferenceData, ResultModel](
            steps = remaining,
            result = acc.getOrElse(result)
          )

        case head :: _ if head.size + unitsDone > unitsToAttempt =>
          this.copy[ReferenceData, ResultModel](
            steps = remaining,
            result = acc.getOrElse(result)
          )

        case head :: next =>
          rec(next, unitsDone + head.size, Option(head.perform(reference, acc.getOrElse(result))))

      }

    val isWithinTolerance: Boolean = (gameTime.frameDuration - gameTime.delta.toMillis).toDouble <= gameTime.frameDuration.toDouble * 0.1
    val next                       = rec(steps, 0, None)

    next.copy[ReferenceData, ResultModel](
      unitsToAttempt = if (isWithinTolerance) next.unitsToAttempt + rateOfChange else next.unitsToAttempt - rateOfChange
    )
  }

  def withUnitRateOfChange(newRateOfChange: Int): LongCompute[ReferenceData, ResultModel] =
    this.copy[ReferenceData, ResultModel](rateOfChange = newRateOfChange)

  def withTolerance(newTolerance: Double): LongCompute[ReferenceData, ResultModel] =
    this.copy[ReferenceData, ResultModel](tolerance = newTolerance)
}

object LongCompute {

  def apply[ReferenceData, ResultModel](reference: ReferenceData, initialValue: ResultModel, steps: List[MonitoredStep[ReferenceData, ResultModel]]): LongCompute[ReferenceData, ResultModel] =
    LongCompute(reference, initialValue, steps, 0, 1, 0.1)

  def apply[ResultModel](initialValue: ResultModel, steps: List[MonitoredStep[Unit, ResultModel]]): LongCompute[Unit, ResultModel] =
    LongCompute((), initialValue, steps, 0, 1, 0.1)

}
