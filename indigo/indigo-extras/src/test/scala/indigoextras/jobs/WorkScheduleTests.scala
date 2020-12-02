package indigoextras.jobs

import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.time.Seconds
import indigo.shared.events.FrameTick
import indigo.shared.datatypes.BindingKey
import indigo.shared.events.GlobalEvent
import indigoextras.TestFail._
import indigo.shared.dice.Dice

class WorkScheduleTests extends munit.FunSuite {

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

  val dice = Dice.loaded(1)

  test("The WorkSchedule.should allow you to create an empty work schedule") {
    assertEquals(WorkSchedule[SampleActor, SampleContext](bindingKey).jobStack, Nil)
  }

  test("The WorkSchedule.should generate new local jobs when the stack is empty") {

    val actor: SampleActor     = SampleActor(10, likesFishing = false)
    val context: SampleContext = SampleContext(false)
    val expected: List[Job]    = WanderTo(100) :: Nil

    val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, SampleActor.worker, Nil)

    val gameTime = new GameTime(0, 0, GameTime.FPS(0))

    val actual = workSchedule.update(gameTime, dice, actor, context)(FrameTick).state.workSchedule.jobStack

    assertEquals(actual, expected)

  }

  test("The WorkSchedule.should allow work to be done on jobs") {

    val actor: SampleActor     = SampleActor(10, likesFishing = false)
    val context: SampleContext = SampleContext(false)
    val jobs                   = Fishing(0) :: Nil

    val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, SampleActor.worker, jobs)
    val gameTime     = new GameTime(0, 0, GameTime.FPS(0))

    workSchedule.update(gameTime, dice, actor, context)(FrameTick).state.workSchedule.jobStack.headOption match {
      case Some(j @ Fishing(done)) =>
        assertEquals(done, SampleActor.defaultFishingSpeed)

      case _ =>
        fail("error")
    }

  }

  test("The WorkSchedule.should ignore unrelated events") {
    case class UnrelatedEvent(id: String) extends GlobalEvent

    val actor: SampleActor     = SampleActor(10, likesFishing = false)
    val context: SampleContext = SampleContext(false)
    val expected: List[Job]    = Nil

    val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, SampleActor.worker, Nil)
    val gameTime     = new GameTime(0, 0, GameTime.FPS(0))

    val allocationId = bindingKey

    val actual = workSchedule
      .update(gameTime, dice, actor, context)(UnrelatedEvent("ignored!"))
      .state
      .workSchedule
      .jobStack

    assertEquals(actual, expected)
  }

  test("The WorkSchedule.should accept jobs allocated to this worker") {
    val actor: SampleActor     = SampleActor(10, likesFishing = false)
    val context: SampleContext = SampleContext(false)
    val jobToAllocate: Fishing = Fishing(0)
    val expected: List[Job]    = jobToAllocate :: Nil

    val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, SampleActor.worker, Nil)
    val gameTime     = new GameTime(0, 0, GameTime.FPS(0))

    val allocationId = bindingKey

    val actual = workSchedule
      .update(gameTime, dice, actor, context)(JobMarketEvent.Allocate(allocationId, jobToAllocate))
      .state
      .workSchedule
      .jobStack

    assertEquals(actual, expected)
  }

  test("The WorkSchedule.should generate work if no global work could be found") {
    val actor: SampleActor     = SampleActor(10, likesFishing = false)
    val context: SampleContext = SampleContext(false)
    val expected: List[Job]    = WanderTo(100) :: Nil

    val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, SampleActor.worker, Nil)
    val gameTime     = new GameTime(0, 0, GameTime.FPS(0))

    val allocationId = bindingKey

    val actual = workSchedule
      .update(gameTime, dice, actor, context)(JobMarketEvent.NothingFound(allocationId))
      .state
      .workSchedule
      .jobStack

    assertEquals(actual, expected)
  }

  test("The WorkSchedule.should be able to post to a global job board on destruction") {

    val globalJob                      = CantHave()
    val expected: List[JobMarketEvent] = JobMarketEvent.Post(globalJob) :: Nil

    val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, SampleActor.worker, List(globalJob))

    val actual = workSchedule.destroy().globalEvents

    assertEquals(actual, expected)

  }

  // Note, the behaviours here are very specific to the Worker instance. But this proves the general flow is sound.

  val actor: SampleActor     = SampleActor(10, likesFishing = false)
  val context: SampleContext = SampleContext(false)
  val jobList: List[Job] = List(
    WanderTo(10),
    Fishing(10),
    WanderTo(30)
  )

  val workSchedule = WorkSchedule[SampleActor, SampleContext](bindingKey, SampleActor.worker, jobList)

  test("The WorkSchedule.should complete a job and move onto the next one.Check the current") {
    workSchedule.currentJob match {
      case Some(WanderTo(position)) =>
        assertEquals(position, 10)

      case _ =>
        fail("error")
    }
  }

  val gameTime = new GameTime(0, 0, GameTime.FPS(0))

  val workSchedule2 = workSchedule.update(gameTime, dice, actor, context)(FrameTick).state.workSchedule

  test("The WorkSchedule.should complete a job and move onto the next one.Arrived, move onto next job") {
    workSchedule2.currentJob match {
      case Some(Fishing(done)) =>
        assertEquals(done, 10)

      case _ =>
        fail("error")
    }
  }

  val workSchedule3 =
    workSchedule2
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule //20
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule //30
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule //40
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule //50
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule //60
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule //70
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule //80
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule //90

  test("The WorkSchedule.should complete a job and move onto the next one.Nearly done") {
    workSchedule3.currentJob match {
      case Some(Fishing(done)) =>
        assertEquals(done, 90)

      case _ =>
        fail("error")
    }
  }

  val workSchedule4 =
    workSchedule3
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule // 100
      .update(gameTime, dice, actor, context)(FrameTick)
      .state
      .workSchedule // Complete fishing job, onJobComplete creates WanderTo(0) which is prepended.
      .update(gameTime, dice, actor.copy(position = 0), context)(FrameTick)
      .state
      .workSchedule // WanderTo(0) complete, now back to the original job list.

  test("The WorkSchedule.should complete a job and move onto the next one. on..") {
    workSchedule4.currentJob match {
      case Some(WanderTo(position)) =>
        assertEquals(position, 30)

      case _ =>
        fail("error")
    }
  }

  test("The WorkSchedule.should allow you to see the current job") {

    val jobList: List[Job] = List(
      Fishing(100)
    )

    val actual = WorkSchedule[SampleActor, SampleContext](bindingKey, SampleActor.worker, jobList).currentJob

    actual match {
      case Some(Fishing(done)) =>
        assertEquals(done, 100)

      case _ =>
        fail("error")
    }
  }

}
