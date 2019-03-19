package indigoexts.jobs

import org.scalatest.{FunSpec, Matchers}
import indigo.gameengine.GameTime
import indigo.gameengine.events.FrameTick
import indigo.gameengine.scenegraph.datatypes.BindingKey
import indigo.GlobalEvent

class WorkScheduleSpec extends FunSpec with Matchers {

  import SampleJobs._

  val bindingKey: BindingKey = BindingKey("test")

  describe("The WorkSchedule") {

    it("should allow you to create an empty work schedule") {
      WorkSchedule.empty[SampleActor, Unit].jobStack shouldEqual Nil
    }

    it("should generate new local jobs when the stack is empty") {

      val actor: SampleActor     = SampleActor(10, likesFishing = false)
      val context: SampleContext = SampleContext(false)
      val expected: List[Job]    = WanderTo(100) :: Nil

      val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)

      val gameTime = new GameTime(0, 0, 0)

      val actual = workSchedule.update(gameTime, actor, context)(SampleActor.worker)(FrameTick).workSchedule.jobStack

      actual shouldEqual expected

    }

    it("should allow work to be done on jobs") {

      val actor: SampleActor     = SampleActor(10, likesFishing = false)
      val context: SampleContext = SampleContext(false)
      val jobs                   = Fishing(0) :: Nil

      val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, jobs)
      val gameTime     = new GameTime(0, 0, 0)

      workSchedule.update(gameTime, actor, context)(SampleActor.worker)(FrameTick).workSchedule.jobStack.headOption match {
        case Some(j @ Fishing(done)) =>
          done shouldEqual SampleActor.defaultFishingSpeed

        case _ =>
          fail("Unexpected job type found")
      }

    }

    it("should ignore unrelated events") {
      case class UnrelatedEvent(id: String) extends GlobalEvent

      val actor: SampleActor     = SampleActor(10, likesFishing = false)
      val context: SampleContext = SampleContext(false)
      val expected: List[Job]    = Nil

      val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)
      val gameTime     = new GameTime(0, 0, 0)

      val allocationId = bindingKey

      val actual = workSchedule
        .update(gameTime, actor, context)(SampleActor.worker)(UnrelatedEvent("ignored!"))
        .workSchedule
        .jobStack

      actual shouldEqual expected
    }

    it("should accept jobs allocated to this worker") {
      val actor: SampleActor     = SampleActor(10, likesFishing = false)
      val context: SampleContext = SampleContext(false)
      val jobToAllocate: Fishing = Fishing(0)
      val expected: List[Job]    = jobToAllocate :: Nil

      val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)
      val gameTime     = new GameTime(0, 0, 0)

      val allocationId = bindingKey

      val actual = workSchedule
        .update(gameTime, actor, context)(SampleActor.worker)(JobMarketEvent.Allocate(allocationId, jobToAllocate))
        .workSchedule
        .jobStack

      actual shouldEqual expected
    }

    it("should generate work if no global work could be found") {
      val actor: SampleActor     = SampleActor(10, likesFishing = false)
      val context: SampleContext = SampleContext(false)
      val expected: List[Job]    = WanderTo(100) :: Nil

      val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)
      val gameTime     = new GameTime(0, 0, 0)

      val allocationId = bindingKey

      val actual = workSchedule
        .update(gameTime, actor, context)(SampleActor.worker)(JobMarketEvent.NothingFound(allocationId))
        .workSchedule
        .jobStack

      actual shouldEqual expected
    }

    it("should be able to post to a global job board on destruction") {

      val globalJob                      = CantHave()
      val expected: List[JobMarketEvent] = JobMarketEvent.Post(globalJob) :: Nil

      val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, List(globalJob))

      val actual = workSchedule.destroy().events

      actual shouldEqual expected

    }

    // Note, the behaviours here are very specific to the Worker instance. But this proves the general flow is sound.
    it("should complete a job and move onto the next one") {

      val actor: SampleActor     = SampleActor(10, likesFishing = false)
      val context: SampleContext = SampleContext(false)
      val jobList: List[Job] = List(
        WanderTo(10),
        Fishing(10),
        WanderTo(30)
      )

      val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, jobList)

      withClue("Check the current") {
        workSchedule.current match {
          case Some(WanderTo(position)) =>
            position shouldEqual 10

          case _ =>
            fail("Did not find expected current job")
        }
      }

      val gameTime = new GameTime(0, 0, 0)

      val workSchedule2 = workSchedule.update(gameTime, actor, context)(SampleActor.worker)(FrameTick).workSchedule

      withClue("Arrived, move onto next job") {
        workSchedule2.current match {
          case Some(Fishing(done)) =>
            done shouldEqual 10

          case _ =>
            fail("Did not find expected current job")
        }
      }

      val workSchedule3 =
        workSchedule2
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule //20
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule //30
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule //40
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule //50
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule //60
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule //70
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule //80
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule //90

      withClue("Nearly done") {
        workSchedule3.current match {
          case Some(Fishing(done)) =>
            done shouldEqual 90

          case _ =>
            fail("Did not find expected current job")
        }
      }

      val workSchedule4 =
        workSchedule3
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule // 100
          .update(gameTime, actor, context)(SampleActor.worker)(FrameTick)
          .workSchedule // Complete fishing job, onJobComplete creates WanderTo(0) which is prepended.
          .update(gameTime, actor.copy(position = 0), context)(SampleActor.worker)(FrameTick)
          .workSchedule // WanderTo(0) complete, now back to the original job list.

      withClue("Moving on..") {
        workSchedule4.current match {
          case Some(WanderTo(position)) =>
            position shouldEqual 30

          case _ =>
            fail("Did not find expected current job")
        }
      }
    }

    it("should allow you to see the current job") {

      val jobList: List[Job] = List(
        Fishing(100)
      )

      val actual = WorkSchedule[SampleActor, SampleContext](bindingKey, jobList).current

      actual match {
        case Some(Fishing(done)) =>
          done shouldEqual 100

        case _ =>
          fail("Did not find current job")
      }
    }

  }

  describe("The UpdatedWorkSchedule") {

    it("should be able to add events") {
      val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)

      val updated = UpdatedWorkSchedule(workSchedule, SampleActor(10, likesFishing = false))

      updated.addEvents(List(FrameTick)).events shouldEqual List(FrameTick)
    }

  }

}
