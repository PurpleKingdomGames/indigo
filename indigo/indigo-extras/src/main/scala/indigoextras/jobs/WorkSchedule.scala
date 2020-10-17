package indigoextras.jobs

import indigo.shared.time.GameTime
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.datatypes.BindingKey
import indigo.shared.Outcome
import indigo.shared.dice.Dice

/**
  * Represents an Actors work schedule
  *
  * @param id
  * @param jobStack
  */
final case class WorkSchedule[Actor, Context](val id: BindingKey, val worker: Worker[Actor, Context], val jobStack: List[Job]) {

  def current: Option[Job] =
    WorkSchedule.current(this)

  def update(gameTime: GameTime, dice: Dice, actor: Actor, context: Context): GlobalEvent => Outcome[WorkScheduleUpdate[Actor, Context]] =
    WorkSchedule.update(id, this, gameTime, dice, actor, context, worker)

  def destroy(): Outcome[WorkSchedule[Actor, Context]] =
    WorkSchedule.destroy(this)

}
object WorkSchedule {

  def apply[Actor, Context](id: BindingKey)(implicit worker: Worker[Actor, Context]): WorkSchedule[Actor, Context] =
    new WorkSchedule[Actor, Context](id, worker, Nil)

  def current[Actor, Context](workSchedule: WorkSchedule[Actor, Context]): Option[Job] =
    workSchedule.jobStack.headOption

  def update[Actor, Context](
      id: BindingKey,
      workSchedule: WorkSchedule[Actor, Context],
      gameTime: GameTime,
      dice: Dice,
      actor: Actor,
      context: Context,
      worker: Worker[Actor, Context]
  ): GlobalEvent => Outcome[WorkScheduleUpdate[Actor, Context]] = {
    case JobMarketEvent.Allocate(allocationId, job) if allocationId === id =>
      Outcome(
        WorkScheduleUpdate(
          workSchedule.copy[Actor, Context](
            jobStack = job :: workSchedule.jobStack
          ),
          actor
        )
      )

    case JobMarketEvent.NothingFound(allocationId) if allocationId === id =>
      updateWorkSchedule[Actor, Context](workSchedule, gameTime, dice, actor, context, worker)

    case FrameTick =>
      updateWorkSchedule[Actor, Context](workSchedule, gameTime, dice, actor, context, worker)

    case _ =>
      Outcome(
        WorkScheduleUpdate(workSchedule, actor)
      )
  }

  def updateWorkSchedule[Actor, Context](
      workSchedule: WorkSchedule[Actor, Context],
      gameTime: GameTime,
      dice: Dice,
      actor: Actor,
      context: Context,
      worker: Worker[Actor, Context]
  ): Outcome[WorkScheduleUpdate[Actor, Context]] =
    workSchedule.jobStack match {
      case Nil =>
        Outcome(
          WorkScheduleUpdate(
            workSchedule.copy[Actor, Context](
              jobStack = worker.generateJobs(gameTime, dice)
            ),
            actor
          )
        ).addGlobalEvents(JobMarketEvent.Find(workSchedule.id, worker.canTakeJob(actor)))

      case current :: _ if worker.isJobComplete(actor)(current) =>
        worker.onJobComplete(actor, context)(current).map { jobs =>
          WorkScheduleUpdate(
            workSchedule.copy[Actor, Context](
              jobStack = jobs ++ workSchedule.jobStack.drop(1)
            ),
            actor
          )
        }

      case h :: t =>
        val res: (Job, Actor) = worker.workOnJob(gameTime, actor, context)(h)
        Outcome(
          WorkScheduleUpdate(
            workSchedule.copy[Actor, Context](
              jobStack = res._1 :: t
            ),
            res._2
          )
        )
    }

  def destroy[Actor, Context](workSchedule: WorkSchedule[Actor, Context]): Outcome[WorkSchedule[Actor, Context]] =
    Outcome(
      workSchedule.copy[Actor, Context](jobStack = Nil),
      workSchedule.jobStack.filterNot(_.isLocal).map(j => JobMarketEvent.Post(j))
    )

}

final case class WorkScheduleUpdate[Actor, Context](workSchedule: WorkSchedule[Actor, Context], actor: Actor)
