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
    tolerance: Double,
    sizeCompleted: Int
) {

  def isComplete: Boolean =
    steps.isEmpty

  def sizeRemaining: Int = steps.map(_.size).sum

  def portionCompleted: Double = sizeCompleted.toDouble / (sizeCompleted + sizeRemaining)

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
            result = acc.getOrElse(result),
            sizeCompleted = sizeCompleted + unitsDone
          )

        case head :: _ if unitsDone > 0 && head.size + unitsDone > unitsToAttempt =>
          this.copy[ReferenceData, ResultModel](
            steps = remaining,
            result = acc.getOrElse(result),
            sizeCompleted = sizeCompleted + unitsDone
          )

        case head :: next =>
          rec(next, unitsDone + head.size, Option(head.perform(reference, acc.getOrElse(result))))

      }

    val next = rec(steps, 0, None)

    next.copy[ReferenceData, ResultModel](
      unitsToAttempt =
        if (LongCompute.isWithinTolerance(gameTime, tolerance)) next.unitsToAttempt + rateOfChange
        else next.unitsToAttempt - rateOfChange
    )
  }

  def withUnitRateOfChange(newRateOfChange: Int): LongCompute[ReferenceData, ResultModel] =
    this.copy[ReferenceData, ResultModel](rateOfChange = newRateOfChange)

  def withTolerance(newTolerance: Double): LongCompute[ReferenceData, ResultModel] =
    this.copy[ReferenceData, ResultModel](tolerance = newTolerance)
}

object LongCompute {

  def apply[ReferenceData, ResultModel](reference: ReferenceData, initialValue: ResultModel, steps: List[MonitoredStep[ReferenceData, ResultModel]]): LongCompute[ReferenceData, ResultModel] =
    LongCompute(
      reference,
      initialValue,
      steps,
      0,
      if (steps.nonEmpty) steps.map(_.size).sum / steps.length else 1,
      0.2,
      0
    )

  def apply[ResultModel](initialValue: ResultModel, steps: List[MonitoredStep[Unit, ResultModel]]): LongCompute[Unit, ResultModel] =
    LongCompute(
      (),
      initialValue,
      steps,
      0,
      if (steps.nonEmpty) steps.map(_.size).sum / steps.length else 1,
      0.2,
      0
    )

  def isWithinTolerance(gameTime: GameTime, tolerance: Double): Boolean =
    (gameTime.frameDuration - gameTime.delta.toMillis).toDouble >= gameTime.frameDuration.toDouble - (gameTime.frameDuration.toDouble * tolerance)

}
