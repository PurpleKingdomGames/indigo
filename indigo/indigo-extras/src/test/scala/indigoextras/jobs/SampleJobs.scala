package indigoextras.jobs

import indigo.shared.time.GameTime
import indigoextras.jobs.SampleJobs.Fishing

sealed trait SampleJobs extends Job
object SampleJobs {

  case class Fishing(workUnitsCompleted: Int) extends SampleJobs {
    val jobName: JobName = JobName("fishing")
    val isLocal: Boolean = true

    def doWork(numberOfUnit: Int): Fishing =
      this.copy(workUnitsCompleted = Math.max(0, workUnitsCompleted + numberOfUnit))
  }
  object Fishing {
    val totalWorkUnits: Int = 100
  }

  case class WanderTo(position: Int) extends SampleJobs {
    val jobName: JobName = JobName("wander to")
    val isLocal: Boolean = true
  }

  case class CantHave() extends SampleJobs {
    val jobName: JobName = JobName("can't have this job")
    val isLocal: Boolean = false
  }

}

case class SampleActor(position: Int, likesFishing: Boolean) {
  val fishingSpeed: Int = SampleActor.defaultFishingSpeed
}
object SampleActor {

  val default: SampleActor =
    SampleActor(0, false)

  val defaultFishingSpeed: Int = 10

  implicit val worker: Worker[SampleActor, SampleContext] =
    new Worker[SampleActor, SampleContext] {
      def isJobComplete(actor: SampleActor): Job => Boolean = {
        case SampleJobs.Fishing(completed) =>
          completed == Fishing.totalWorkUnits

        case SampleJobs.WanderTo(target) =>
          actor.position == target

        case _ =>
          true
      }

      def onJobComplete(actor: SampleActor, context: SampleContext): Job => JobComplete = {
        case SampleJobs.Fishing(_) =>
          JobComplete(List(SampleJobs.WanderTo(0)), Nil)

        case SampleJobs.WanderTo(_) =>
          JobComplete.empty

        case _ =>
          JobComplete.empty
      }

      def workOnJob(gameTime: GameTime, actor: SampleActor, context: SampleContext): Job => (Job, SampleActor) = {
        case j @ SampleJobs.Fishing(_) =>
          (j.doWork(actor.fishingSpeed), actor.copy(likesFishing = context.predicate))

        case j @ SampleJobs.WanderTo(_) =>
          (j, actor)

        case job =>
          (job, actor)
      }

      def generateJobs: () => List[Job] = () => List(SampleJobs.WanderTo(100))

      def canTakeJob(actor: SampleActor): Job => Boolean = {
        case SampleJobs.CantHave() =>
          false

        case j: SampleJobs =>
          true

        case _ =>
          false
      }
    }

}

case class SampleContext(predicate: Boolean)
