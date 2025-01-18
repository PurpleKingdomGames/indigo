package indigoextras.jobs

import indigo.shared.Context
import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey
import indigo.shared.dice.Dice
import indigo.shared.events.FrameTick
import indigo.shared.scenegraph.SceneAudio
import indigo.shared.subsystems
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemId
import indigo.shared.time.GameTime

class JobMarketTests extends munit.FunSuite {

  val context =
    SubSystemContext.fromContext(
      Context.initial
        .modifyFrame(
          _.withDice(Dice.loaded(6))
        )
    )

  val workContext =
    WorkContext[SampleActor, SampleContext](
      GameTime.zero,
      Dice.loaded(6),
      SampleActor.default,
      SampleContext(true)
    )

  test("The job market.subsytem event filter should only allow JobMarketEvents") {
    val market = JobMarket.subSystem(SubSystemId("market"))

    val job: Job                  = SampleJobs.CantHave()
    val key: BindingKey           = BindingKey("test")
    val predicate: Job => Boolean = _ => true

    assertEquals(market.eventFilter(FrameTick), None)
    assertEquals(market.eventFilter(JobMarketEvent.Post(job)), Some(JobMarketEvent.Post(job)))
    assertEquals(market.eventFilter(JobMarketEvent.Find(key, predicate)), Some(JobMarketEvent.Find(key, predicate)))
    assertEquals(market.eventFilter(JobMarketEvent.Allocate(key, job)), Some(JobMarketEvent.Allocate(key, job)))
    assertEquals(market.eventFilter(JobMarketEvent.NothingFound(key)), Some(JobMarketEvent.NothingFound(key)))
  }

  test("The job market.should not process outbound JobMarketEvent types") {

    val bindingKey: BindingKey            = BindingKey("0001")
    val job: Job                          = SampleJobs.WanderTo(10)
    val market                            = JobMarket.subSystem(SubSystemId("market"))
    val allocateEvent: JobMarketEvent     = JobMarketEvent.Allocate(bindingKey, job)
    val nothingFoundEvent: JobMarketEvent = JobMarketEvent.NothingFound(bindingKey)

    val updatedA = market.update(context, List(job))(allocateEvent)
    assertEquals(updatedA.unsafeGet, List(job))
    assertEquals(updatedA.unsafeGlobalEvents, Batch.empty)

    val updatedB = market.update(context, updatedA.unsafeGet)(nothingFoundEvent)
    assertEquals(updatedB.unsafeGet, List(job))
    assertEquals(updatedB.unsafeGlobalEvents, Batch.empty)

  }

  test("The job market.should be able to report it's current jobs") {
    val job: Job                = SampleJobs.CantHave()
    val market: JobMarket[Unit] = JobMarket(SubSystemId("market"), List(job))

    val report = market.availableJobs.map(_.jobName.toString).mkString(",")

    assertEquals(report.contains(job.jobName.toString), true)
  }

  test("The job market.should not render anything") {
    val job: Job                = SampleJobs.WanderTo(10)
    val market: JobMarket[Unit] = JobMarket.subSystem(SubSystemId("market"))

    assertEquals(market.present(context, List(job)).unsafeGet.layers.flatMap(_.toBatch).flatMap(_.nodes).isEmpty, true)
    assertEquals(market.present(context, List(job)).unsafeGlobalEvents.isEmpty, true)
    assertEquals(market.present(context, List(job)).unsafeGet.audio, None)
  }

  test("The job market.should have an empty subsystem representation") {
    val market = JobMarket.subSystem(SubSystemId("market"))

    assertEquals(market.availableJobs, Nil)
  }

  test("The job market.should allow a you to find work.when there is a job you can do") {
    val bindingKey: BindingKey    = BindingKey("0001")
    val job: Job                  = SampleJobs.WanderTo(10)
    val market: JobMarket[Unit]   = JobMarket.subSystem(SubSystemId("market"))
    val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(workContext))

    val updated = market.update(context, List(job))(findEvent)

    assertEquals(updated.unsafeGet, Nil)
    assertEquals(updated.unsafeGlobalEvents.head, JobMarketEvent.Allocate(bindingKey, job))
  }

  test("The job market.should allow a you to find work.but not when there isn't any work") {
    val bindingKey: BindingKey    = BindingKey("0001")
    val market: JobMarket[Unit]   = JobMarket.subSystem(SubSystemId("market"))
    val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(workContext))

    val updated = market.update(context, Nil)(findEvent)

    assertEquals(updated.unsafeGet, Nil)
    assertEquals(updated.unsafeGlobalEvents.head, JobMarketEvent.NothingFound(bindingKey))
  }

  test("The job market.should allow a you to find work.or when the work is not acceptable to the worker") {
    val bindingKey: BindingKey    = BindingKey("0001")
    val job: Job                  = SampleJobs.CantHave()
    val market: JobMarket[Unit]   = JobMarket.subSystem(SubSystemId("market"))
    val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(workContext))

    val updated = market.update(context, List(job))(findEvent)

    assertEquals(updated.unsafeGet, List(job))
    assertEquals(updated.unsafeGlobalEvents.head, JobMarketEvent.NothingFound(bindingKey))
  }

  test("The job market.should allow a you to find work.should give you the highest priority job first") {
    val bindingKey: BindingKey    = BindingKey("0001")
    val jobs: List[Job]           = List(SampleJobs.WanderTo(10), SampleJobs.WanderTo(20), SampleJobs.Fishing(0))
    val market: JobMarket[Unit]   = JobMarket.subSystem(SubSystemId("market"))
    val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(workContext))

    val updated = market.update(context, jobs)(findEvent)

    assertEquals(updated.unsafeGet, List(SampleJobs.WanderTo(10), SampleJobs.WanderTo(20)))
    assertEquals(updated.unsafeGlobalEvents.head, JobMarketEvent.Allocate(bindingKey, SampleJobs.Fishing(0)))
  }

  test("The job market.should allow you to post a job.to an empty market") {
    val job: Job                  = SampleJobs.WanderTo(10)
    val market: JobMarket[Unit]   = JobMarket.subSystem(SubSystemId("market"))
    val postEvent: JobMarketEvent = JobMarketEvent.Post(job)

    val updated = market.update(context, Nil)(postEvent)

    assertEquals(updated.unsafeGet, List(job))
  }

  test("The job market.should allow you to post a job.and append to a non-empty market") {
    val job: Job                  = SampleJobs.WanderTo(10)
    val market: JobMarket[Unit]   = JobMarket.subSystem(SubSystemId("market"))
    val postEvent: JobMarketEvent = JobMarketEvent.Post(job)

    val updated = market.update(context, List(SampleJobs.Fishing(0)))(postEvent)

    assertEquals(updated.unsafeGet, List(SampleJobs.Fishing(0), job))
  }

  test("The job market.should allow you to post a job.the jobs state will be preserved") {
    val job: Job                  = SampleJobs.Fishing(50)
    val market: JobMarket[Unit]   = JobMarket.subSystem(SubSystemId("market"))
    val postEvent: JobMarketEvent = JobMarketEvent.Post(job)

    val updated = market.update(context, Nil)(postEvent)

    assertEquals(updated.unsafeGet, List(SampleJobs.Fishing(50)))
  }

}
