package indigoextras.jobs

import indigo.shared.time.GameTime
import utest._
import indigoextras.jobs.SampleJobs.{CantHave, Fishing, WanderTo}
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.time.Seconds

object WorkerTests extends TestSuite {

  val worker: Worker[SampleActor, SampleContext] = SampleActor.worker
  val actor: SampleActor                         = SampleActor(0, likesFishing = false)

  def tests: Tests =
    Tests {
      "You should be able to create a Worker instance" - {
        final case class TestActor()
        final case class TestContext()
        final case class TestJob() extends Job {
          val jobName: JobName = JobName("test job")
          val isLocal: Boolean = true
          val priority: Int    = 1
        }

        val isComplete: WorkContext[TestActor, TestContext] => Job => Boolean =
          _ => _ => true

        val onComplete: WorkContext[TestActor, TestContext] => Job => Outcome[(List[Job], TestActor)] =
          w => _ => Outcome((Nil, w.actor))

        val doWork: WorkContext[TestActor, TestContext] => Job => (Job, TestActor) =
          w => j => (j, w.actor)

        val jobGenerator: WorkContext[TestActor, TestContext] => List[Job] =
          _ => Nil

        val jobAcceptable: WorkContext[TestActor, TestContext] => Job => Boolean =
          _ => _ => false

        val worker = Worker.create[TestActor, TestContext](
          isComplete,
          onComplete,
          doWork,
          jobGenerator,
          jobAcceptable
        )

        val actor   = TestActor()
        val context = TestContext()
        val time    = GameTime.zero
        val dice    = Dice.loaded(1)
        val job     = TestJob()

        val workContext =
          WorkContext[TestActor, TestContext](
            time,
            dice,
            actor,
            context
          )

        worker.isJobComplete(workContext)(job) ==> true

        val completed = worker.onJobComplete(workContext)(job)
        completed.state ==> (Nil, TestActor())
        completed.globalEvents ==> Nil

        worker.workOnJob(workContext)(job) ==> (job, actor)
        worker.generateJobs(workContext) ==> Nil
        worker.canTakeJob(workContext)(job) ==> false
      }

      def workContext(time: Double, p: Boolean): WorkContext[SampleActor, SampleContext] =
        WorkContext[SampleActor, SampleContext](
          GameTime.is(Seconds(time)),
          Dice.loaded(1),
          SampleActor.default,
          SampleContext(p)
        )

      "A Worker instance" - {

        "should be able to check a job is complete" - {
          worker.isJobComplete(workContext(0d, true))(Fishing(Fishing.totalWorkUnits)) ==> true
        }

        "should be able to perform an action when a job completes" - {
          worker.onJobComplete(workContext(0d, false))(Fishing(Fishing.totalWorkUnits)).state._1.head ==> WanderTo(0)
        }

        "should be able to work on a job" - {
          val res = worker.workOnJob(workContext(0d, false))(Fishing(0))
          res ==> (Fishing(SampleActor.defaultFishingSpeed), actor)
        }

        "and working on a job can affect the actor" - {
          val res = worker.workOnJob(workContext(0d, true))(Fishing(0))
          res ==> (Fishing(SampleActor.defaultFishingSpeed), actor.copy(likesFishing = true))
        }

        "should be able to generate jobs" - {
          worker.generateJobs(workContext(0d, true)) ==> List(WanderTo(100))
        }

        "should be able to distinguish between jobs you can take and ones you can't" - {
          worker.canTakeJob(workContext(0d, true))(WanderTo(30)) ==> true
          worker.canTakeJob(workContext(0d, true))(CantHave()) ==> false
        }

      }
    }

}
