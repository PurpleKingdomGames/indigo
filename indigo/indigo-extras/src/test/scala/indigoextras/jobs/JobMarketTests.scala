package indigoextras.jobs

import indigo._
import utest._

import indigo.shared.EqualTo._
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.BindingKey
import indigo.shared.events.FrameTick
import indigo.shared.scenegraph.SceneAudio
import indigo.shared.datatypes.RGBA

object JobMarketTests extends TestSuite {

  val context =
    new SubSystemFrameContext(
      GameTime.zero,
      Dice.loaded(6),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister)
    )

  val workContext =
    WorkContext[SampleActor, SampleContext](
      GameTime.zero,
      Dice.loaded(6),
      SampleActor.default,
      SampleContext(true)
    )

  def tests: Tests =
    Tests {
      "The job market" - {

        "subsytem event filter should only allow JobMarketEvents" - {
          val market = JobMarket.subSystem

          val job: Job                  = SampleJobs.CantHave()
          val key: BindingKey           = BindingKey("test")
          val predicate: Job => Boolean = _ => true

          market.eventFilter(FrameTick) ==> None
          market.eventFilter(JobMarketEvent.Post(job)) ==> Some(JobMarketEvent.Post(job))
          market.eventFilter(JobMarketEvent.Find(key, predicate)) ==> Some(JobMarketEvent.Find(key, predicate))
          market.eventFilter(JobMarketEvent.Allocate(key, job)) ==> Some(JobMarketEvent.Allocate(key, job))
          market.eventFilter(JobMarketEvent.NothingFound(key)) ==> Some(JobMarketEvent.NothingFound(key))
        }

        "should not process outbound JobMarketEvent types" - {

          val bindingKey: BindingKey            = BindingKey("0001")
          val job: Job                          = SampleJobs.WanderTo(10)
          val market                            = JobMarket.subSystem
          val allocateEvent: JobMarketEvent     = JobMarketEvent.Allocate(bindingKey, job)
          val nothingFoundEvent: JobMarketEvent = JobMarketEvent.NothingFound(bindingKey)

          val updatedA = market.update(context, List(job))(allocateEvent)
          updatedA.state ==> List(job)
          updatedA.globalEvents ==> Nil

          val updatedB = market.update(context, updatedA.state)(nothingFoundEvent)
          updatedB.state ==> List(job)
          updatedB.globalEvents ==> Nil

        }

        "should be able to report it's current jobs" - {
          val job: Job          = SampleJobs.CantHave()
          val market: JobMarket = JobMarket(List(job))

          val report = market.availableJobs.map(_.jobName.value).mkString(",")

          report.contains(job.jobName.value) ==> true
        }

        "should not render anything" - {
          val job: Job          = SampleJobs.WanderTo(10)
          val market: JobMarket = JobMarket.subSystem

          market.present(context, List(job)).gameLayer.nodes.isEmpty ==> true
          market.present(context, List(job)).lightingLayer.nodes.isEmpty ==> true
          market.present(context, List(job)).uiLayer.nodes.isEmpty ==> true
          market.present(context, List(job)).globalEvents.isEmpty ==> true
          market.present(context, List(job)).ambientLight === RGBA.Normal ==> true
          market.present(context, List(job)).audio ==> SceneAudio.None
        }

        "should have an empty subsystem representation" - {
          val market = JobMarket.subSystem

          market.availableJobs ==> Nil
        }

        "should allow a you to find work" - {
          "when there is a job you can do" - {
            val bindingKey: BindingKey    = BindingKey("0001")
            val job: Job                  = SampleJobs.WanderTo(10)
            val market: JobMarket         = JobMarket.subSystem
            val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(workContext))

            val updated = market.update(context, List(job))(findEvent)

            updated.state ==> Nil
            updated.globalEvents.head ==> JobMarketEvent.Allocate(bindingKey, job)
          }

          "but not when there isn't any work" - {
            val bindingKey: BindingKey    = BindingKey("0001")
            val market: JobMarket         = JobMarket.subSystem
            val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(workContext))

            val updated = market.update(context, Nil)(findEvent)

            updated.state ==> Nil
            updated.globalEvents.head ==> JobMarketEvent.NothingFound(bindingKey)
          }

          "or when the work is not acceptable to the worker" - {
            val bindingKey: BindingKey    = BindingKey("0001")
            val job: Job                  = SampleJobs.CantHave()
            val market: JobMarket         = JobMarket.subSystem
            val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(workContext))

            val updated = market.update(context, List(job))(findEvent)

            updated.state ==> List(job)
            updated.globalEvents.head ==> JobMarketEvent.NothingFound(bindingKey)
          }

          "should give you the highest priority job first" - {
            val bindingKey: BindingKey    = BindingKey("0001")
            val jobs: List[Job]           = List(SampleJobs.WanderTo(10), SampleJobs.WanderTo(20), SampleJobs.Fishing(0))
            val market: JobMarket         = JobMarket.subSystem
            val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(workContext))

            val updated = market.update(context, jobs)(findEvent)

            updated.state ==> List(SampleJobs.WanderTo(10), SampleJobs.WanderTo(20))
            updated.globalEvents.head ==> JobMarketEvent.Allocate(bindingKey, SampleJobs.Fishing(0))
          }
        }

        "should allow you to post a job" - {

          "to an empty market" - {
            val job: Job                  = SampleJobs.WanderTo(10)
            val market: JobMarket         = JobMarket.subSystem
            val postEvent: JobMarketEvent = JobMarketEvent.Post(job)

            val updated = market.update(context, Nil)(postEvent)

            updated.state ==> List(job)
          }

          "and append to a non-empty market" - {
            val job: Job                  = SampleJobs.WanderTo(10)
            val market: JobMarket         = JobMarket.subSystem
            val postEvent: JobMarketEvent = JobMarketEvent.Post(job)

            val updated = market.update(context, List(SampleJobs.Fishing(0)))(postEvent)

            updated.state ==> List(SampleJobs.Fishing(0), job)
          }

          "the jobs state will be preserved" - {
            val job: Job                  = SampleJobs.Fishing(50)
            val market: JobMarket         = JobMarket.subSystem
            val postEvent: JobMarketEvent = JobMarketEvent.Post(job)

            val updated = market.update(context, Nil)(postEvent)

            updated.state ==> List(SampleJobs.Fishing(50))
          }
        }

      }
    }

}
