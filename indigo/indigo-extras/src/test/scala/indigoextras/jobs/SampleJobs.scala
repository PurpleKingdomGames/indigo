package indigoextras.jobs

import indigo.shared.Outcome
import indigoextras.jobs.SampleJobs.Fishing

sealed trait SampleJobs extends Job
object SampleJobs {

  case class Fishing(workUnitsCompleted: Int) extends SampleJobs {
    val jobName: JobName = JobName("fishing")
    val isLocal: Boolean = true
    val priority: Int    = 10

    def doWork(numberOfUnit: Int): Fishing =
      this.copy(workUnitsCompleted = Math.max(0, workUnitsCompleted + numberOfUnit))
  }
  object Fishing {
    val totalWorkUnits: Int = 100
  }

  case class WanderTo(position: Int) extends SampleJobs {
    val jobName: JobName = JobName("wander to")
    val isLocal: Boolean = true
    val priority: Int    = 50
  }

  case class CantHave() extends SampleJobs {
    val jobName: JobName = JobName("can't have this job")
    val isLocal: Boolean = false
    val priority: Int    = 100
  }

}

final case class SampleActor(position: Int, likesFishing: Boolean) {
  def fishingSpeed: Int = SampleActor.defaultFishingSpeed
}
object SampleActor {

  val default: SampleActor =
    SampleActor(0, false)

  val defaultFishingSpeed: Int = 10

  implicit val worker: Worker[SampleActor, SampleContext] =
    new Worker[SampleActor, SampleContext] {
      def isJobComplete(context: WorkContext[SampleActor, SampleContext]): Job => Boolean = {
        case SampleJobs.Fishing(completed) =>
          completed == Fishing.totalWorkUnits

        case SampleJobs.WanderTo(target) =>
          context.actor.position == target

        case _ =>
          true
      }

      def onJobComplete(context: WorkContext[SampleActor, SampleContext]): Job => Outcome[(List[Job], SampleActor)] = {
        case SampleJobs.Fishing(_) =>
          Outcome((List(SampleJobs.WanderTo(0)), context.actor), Nil)

        case SampleJobs.WanderTo(_) =>
          Outcome((Nil, context.actor))

        case _ =>
          Outcome((Nil, context.actor))
      }

      def workOnJob(context: WorkContext[SampleActor, SampleContext]): Job => (Job, SampleActor) = {
        case j @ SampleJobs.Fishing(_) =>
          (
            j.doWork(context.actor.fishingSpeed),
            context.actor.copy(likesFishing = context.context.predicate)
          )

        case j @ SampleJobs.WanderTo(_) =>
          (j, context.actor)

        case job =>
          (job, context.actor)
      }

      def generateJobs(context: WorkContext[SampleActor, SampleContext]): List[Job] =
        List(SampleJobs.WanderTo(100))

      def canTakeJob(context: WorkContext[SampleActor, SampleContext]): Job => Boolean = {
        case SampleJobs.CantHave() =>
          false

        case _: SampleJobs =>
          true

        case _ =>
          false
      }
    }

}

case class SampleContext(predicate: Boolean)
