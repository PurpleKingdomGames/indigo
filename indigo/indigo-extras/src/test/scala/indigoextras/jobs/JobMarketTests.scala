package indigoextras.jobs

import indigo._
import utest._

import indigo.shared.EqualTo._
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.FrameContext
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
    new FrameContext(
      GameTime.zero,
      Dice.loaded(6),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister)
    )

  def tests: Tests =
    Tests {
      "The job market" - {

        "subsytem event filter should only allow JobMarketEvents" - {
          val market: JobMarket = JobMarket(Nil)

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
          val market: JobMarket                 = JobMarket(List(job))
          val allocateEvent: JobMarketEvent     = JobMarketEvent.Allocate(bindingKey, job)
          val nothingFoundEvent: JobMarketEvent = JobMarketEvent.NothingFound(bindingKey)

          val updatedA = market.update(context)(allocateEvent)
          updatedA.state.asInstanceOf[JobMarket].jobs ==> List(job)
          updatedA.globalEvents ==> Nil

          val updatedB = market.update(context)(nothingFoundEvent)
          updatedB.state.asInstanceOf[JobMarket].jobs ==> List(job)
          updatedB.globalEvents ==> Nil

        }

        "should be able to report it's current jobs" - {

          val job: Job          = SampleJobs.CantHave()
          val market: JobMarket = JobMarket(List(job))

          val report = market.jobs.map(_.jobName.value).mkString(",")

          report.contains(job.jobName.value) ==> true
        }

        "should not render anything" - {
          val job: Job          = SampleJobs.WanderTo(10)
          val market: JobMarket = JobMarket(List(job))

          market.render(context).gameLayer.nodes.isEmpty ==> true
          market.render(context).lightingLayer.nodes.isEmpty ==> true
          market.render(context).uiLayer.nodes.isEmpty ==> true
          market.render(context).globalEvents.isEmpty ==> true
          market.render(context).ambientLight === RGBA.Normal ==> true
          market.render(context).audio ==> SceneAudio.None
        }

        "should have an empty subsystem representation" - {
          val market = JobMarket.subSystem

          market.jobs ==> Nil
        }

        "should allow a you to find work" - {
          "when there is a job you can do" - {
            val bindingKey: BindingKey    = BindingKey("0001")
            val job: Job                  = SampleJobs.WanderTo(10)
            val market: JobMarket         = JobMarket(List(job))
            val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(SampleActor.default))

            val updated = market.update(context)(findEvent)

            updated.state.asInstanceOf[JobMarket].jobs ==> Nil
            updated.globalEvents.head ==> JobMarketEvent.Allocate(bindingKey, job)
          }

          "but not when there isn't any work" - {
            val bindingKey: BindingKey    = BindingKey("0001")
            val market: JobMarket         = JobMarket(Nil)
            val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(SampleActor.default))

            val updated = market.update(context)(findEvent)

            updated.state.asInstanceOf[JobMarket].jobs ==> Nil
            updated.globalEvents.head ==> JobMarketEvent.NothingFound(bindingKey)
          }

          "or when the work is not acceptable to the worker" - {
            val bindingKey: BindingKey    = BindingKey("0001")
            val job: Job                  = SampleJobs.CantHave()
            val market: JobMarket         = JobMarket(List(job))
            val findEvent: JobMarketEvent = JobMarketEvent.Find(bindingKey, SampleActor.worker.canTakeJob(SampleActor.default))

            val updated = market.update(context)(findEvent)

            updated.state.asInstanceOf[JobMarket].jobs ==> List(job)
            updated.globalEvents.head ==> JobMarketEvent.NothingFound(bindingKey)
          }
        }

        "should allow you to post a job" - {

          "to an empty market" - {
            val job: Job                  = SampleJobs.WanderTo(10)
            val market: JobMarket         = JobMarket(Nil)
            val postEvent: JobMarketEvent = JobMarketEvent.Post(job)

            val updated = market.update(context)(postEvent)

            updated.state.asInstanceOf[JobMarket].jobs ==> List(job)
          }

          "and append to a non-empty market" - {
            val job: Job                  = SampleJobs.WanderTo(10)
            val market: JobMarket         = JobMarket(List(SampleJobs.Fishing(0)))
            val postEvent: JobMarketEvent = JobMarketEvent.Post(job)

            val updated = market.update(context)(postEvent)

            updated.state.asInstanceOf[JobMarket].jobs ==> List(SampleJobs.Fishing(0), job)
          }

          "the jobs state will be preserved" - {
            val job: Job                  = SampleJobs.Fishing(50)
            val market: JobMarket         = JobMarket(Nil)
            val postEvent: JobMarketEvent = JobMarketEvent.Post(job)

            val updated = market.update(context)(postEvent)

            updated.state.asInstanceOf[JobMarket].jobs ==> List(SampleJobs.Fishing(50))
          }
        }

      }
    }

}
