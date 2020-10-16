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
final class WorkSchedule[Actor, Context](val id: BindingKey, val jobStack: List[Job]) {

  def current: Option[Job] =
    WorkSchedule.current(this)

  def update(gameTime: GameTime, dice: Dice, actor: Actor, context: Context)(implicit
      worker: Worker[Actor, Context]
  ): GlobalEvent => Outcome[WorkScheduleUpdate[Actor, Context]] =
    WorkSchedule.update(id, this, gameTime, dice, actor, context)

  def destroy(): Outcome[WorkSchedule[Actor, Context]] =
    WorkSchedule.destroy(this)

}
object WorkSchedule {

  def apply[Actor, Context](id: BindingKey, jobStack: List[Job]): WorkSchedule[Actor, Context] =
    new WorkSchedule[Actor, Context](id, jobStack)

  def apply[Actor, Context](id: BindingKey): WorkSchedule[Actor, Context] =
    new WorkSchedule[Actor, Context](id, Nil)

  def current[Actor, Context](workSchedule: WorkSchedule[Actor, Context]): Option[Job] =
    workSchedule.jobStack.headOption

  def update[Actor, Context](
      id: BindingKey,
      workSchedule: WorkSchedule[Actor, Context],
      gameTime: GameTime,
      dice: Dice,
      actor: Actor,
      context: Context
  )(implicit worker: Worker[Actor, Context]): GlobalEvent => Outcome[WorkScheduleUpdate[Actor, Context]] = {
    case JobMarketEvent.Allocate(allocationId, job) if allocationId === id =>
      Outcome(
        WorkScheduleUpdate(
          WorkSchedule(workSchedule.id, job :: workSchedule.jobStack),
          actor
        )
      )

    case JobMarketEvent.NothingFound(allocationId) if allocationId === id =>
      updateWorkSchedule[Actor, Context](workSchedule, gameTime, dice, actor, context)

    case FrameTick =>
      updateWorkSchedule[Actor, Context](workSchedule, gameTime, dice, actor, context)

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
      context: Context
  )(implicit worker: Worker[Actor, Context]): Outcome[WorkScheduleUpdate[Actor, Context]] =
    workSchedule.jobStack match {
      case Nil =>
        Outcome(
          WorkScheduleUpdate(
            WorkSchedule(workSchedule.id, worker.generateJobs(gameTime, dice)),
            actor
          )
        )

      case current :: _ if worker.isJobComplete(actor)(current) =>
        worker.onJobComplete(actor, context)(current).map { jobs =>
          WorkScheduleUpdate(
            WorkSchedule(workSchedule.id, jobs ++ workSchedule.jobStack.drop(1)),
            actor
          )
        }

      case h :: t =>
        val res: (Job, Actor) = worker.workOnJob(gameTime, actor, context)(h)
        Outcome(
          WorkScheduleUpdate(
            WorkSchedule(workSchedule.id, res._1 :: t),
            res._2
          )
        )
    }

  def destroy[Actor, Context](workSchedule: WorkSchedule[Actor, Context]): Outcome[WorkSchedule[Actor, Context]] =
    Outcome(
      WorkSchedule(workSchedule.id, Nil),
      workSchedule.jobStack.filterNot(_.isLocal).map(j => JobMarketEvent.Post(j))
    )

}

final case class WorkScheduleUpdate[Actor, Context](workSchedule: WorkSchedule[Actor, Context], actor: Actor)
