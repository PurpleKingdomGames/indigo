package indigoexts.jobs

import utest._
import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.time.Seconds
import indigo.shared.events.FrameTick
import indigo.shared.datatypes.BindingKey
import indigo.GlobalEvent
import indigoexts.TestFail._

object WorkScheduleTests extends TestSuite {

  implicit def intToMillis(i: Int): Millis =
    Millis(i.toLong)

  implicit def doubleToMillis(d: Double): Millis =
    Millis(d.toLong)

  implicit def intToSeconds(i: Int): Seconds =
    Seconds(i.toDouble)

  implicit def doubleToSeconds(d: Double): Seconds =
    Seconds(d)

  import SampleJobs._

  val bindingKey: BindingKey = BindingKey("test")

  val tests: Tests =
    Tests {
      "The WorkSchedule" - {

        "should allow you to create an empty work schedule" - {
          WorkSchedule.empty[SampleActor, Unit].jobStack ==> Nil
        }

        "should generate new local jobs when the stack is empty" - {

          val actor: SampleActor     = SampleActor(10, likesFishing = false)
          val context: SampleContext = SampleContext(false)
          val expected: List[Job]    = WanderTo(100) :: Nil

          val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)

          val gameTime = new GameTime(0, 0, GameTime.FPS(0))

          val actual = workSchedule.update(gameTime, actor, context)(SampleActor.worker)(FrameTick).workSchedule.jobStack

          actual ==> expected

        }

        "should allow work to be done on jobs" - {

          val actor: SampleActor     = SampleActor(10, likesFishing = false)
          val context: SampleContext = SampleContext(false)
          val jobs                   = Fishing(0) :: Nil

          val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, jobs)
          val gameTime     = new GameTime(0, 0, GameTime.FPS(0))

          workSchedule.update(gameTime, actor, context)(SampleActor.worker)(FrameTick).workSchedule.jobStack.headOption match {
            case Some(j @ Fishing(done)) =>
              done ==> SampleActor.defaultFishingSpeed

            case _ =>
              fail("error")
          }

        }

        "should ignore unrelated events" - {
          case class UnrelatedEvent(id: String) extends GlobalEvent

          val actor: SampleActor     = SampleActor(10, likesFishing = false)
          val context: SampleContext = SampleContext(false)
          val expected: List[Job]    = Nil

          val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)
          val gameTime     = new GameTime(0, 0, GameTime.FPS(0))

          val allocationId = bindingKey

          val actual = workSchedule
            .update(gameTime, actor, context)(SampleActor.worker)(UnrelatedEvent("ignored!"))
            .workSchedule
            .jobStack

          actual ==> expected
        }

        "should accept jobs allocated to this worker" - {
          val actor: SampleActor     = SampleActor(10, likesFishing = false)
          val context: SampleContext = SampleContext(false)
          val jobToAllocate: Fishing = Fishing(0)
          val expected: List[Job]    = jobToAllocate :: Nil

          val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)
          val gameTime     = new GameTime(0, 0, GameTime.FPS(0))

          val allocationId = bindingKey

          val actual = workSchedule
            .update(gameTime, actor, context)(SampleActor.worker)(JobMarketEvent.Allocate(allocationId, jobToAllocate))
            .workSchedule
            .jobStack

          actual ==> expected
        }

        "should generate work if no global work could be found" - {
          val actor: SampleActor     = SampleActor(10, likesFishing = false)
          val context: SampleContext = SampleContext(false)
          val expected: List[Job]    = WanderTo(100) :: Nil

          val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)
          val gameTime     = new GameTime(0, 0, GameTime.FPS(0))

          val allocationId = bindingKey

          val actual = workSchedule
            .update(gameTime, actor, context)(SampleActor.worker)(JobMarketEvent.NothingFound(allocationId))
            .workSchedule
            .jobStack

          actual ==> expected
        }

        "should be able to post to a global job board on destruction" - {

          val globalJob                      = CantHave()
          val expected: List[JobMarketEvent] = JobMarketEvent.Post(globalJob) :: Nil

          val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, List(globalJob))

          val actual = workSchedule.destroy().events

          actual ==> expected

        }

        // Note, the behaviours here are very specific to the Worker instance. But this proves the general flow is sound.
        "should complete a job and move onto the next one" - {

          val actor: SampleActor     = SampleActor(10, likesFishing = false)
          val context: SampleContext = SampleContext(false)
          val jobList: List[Job] = List(
            WanderTo(10),
            Fishing(10),
            WanderTo(30)
          )

          val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, jobList)

          "Check the current" - {
            workSchedule.current match {
              case Some(WanderTo(position)) =>
                position ==> 10

              case _ =>
                fail("error")
            }
          }

          val gameTime = new GameTime(0, 0, GameTime.FPS(0))

          val workSchedule2 = workSchedule.update(gameTime, actor, context)(SampleActor.worker)(FrameTick).workSchedule

          "Arrived, move onto next job" - {
            workSchedule2.current match {
              case Some(Fishing(done)) =>
                done ==> 10

              case _ =>
                fail("error")
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

          "Nearly done" - {
            workSchedule3.current match {
              case Some(Fishing(done)) =>
                done ==> 90

              case _ =>
                fail("error")
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

          "Moving on.." - {
            workSchedule4.current match {
              case Some(WanderTo(position)) =>
                position ==> 30

              case _ =>
                fail("error")
            }
          }
        }

        "should allow you to see the current job" - {

          val jobList: List[Job] = List(
            Fishing(100)
          )

          val actual = WorkSchedule[SampleActor, SampleContext](bindingKey, jobList).current

          actual match {
            case Some(Fishing(done)) =>
              done ==> 100

            case _ =>
              fail("error")
          }
        }

      }

      "The UpdatedWorkSchedule" - {

        "should be able to add events" - {
          val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, Nil)

          val updated = UpdatedWorkSchedule(workSchedule, SampleActor(10, likesFishing = false))

          updated.addEvents(List(FrameTick)).events ==> List(FrameTick)
        }

      }
    }

}
