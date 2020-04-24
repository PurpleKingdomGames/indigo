package indigoexts.jobs

import indigo.shared.time.GameTime
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.datatypes.BindingKey

final class WorkSchedule[Actor, Context](val id: BindingKey, val jobStack: List[Job]) {

  def current: Option[Job] =
    WorkSchedule.current(this)

  def update(gameTime: GameTime, actor: Actor, context: Context)(
      implicit worker: Worker[Actor, Context]
  ): GlobalEvent => UpdatedWorkSchedule[Actor, Context] =
    WorkSchedule.update(id, this, gameTime, actor, context)

  def destroy(): DestroyedSchedule[Actor, Context] =
    WorkSchedule.destroy(this)

}
object WorkSchedule {

  def apply[Actor, Context](id: BindingKey, jobStack: List[Job]): WorkSchedule[Actor, Context] =
    new WorkSchedule[Actor, Context](id, jobStack)

  def empty[Actor, Context]: WorkSchedule[Actor, Context] =
    WorkSchedule(BindingKey.generate, Nil)

  def current[Actor, Context](workSchedule: WorkSchedule[Actor, Context]): Option[Job] =
    workSchedule.jobStack.headOption

  def update[Actor, Context](
      id: BindingKey,
      workSchedule: WorkSchedule[Actor, Context],
      gameTime: GameTime,
      actor: Actor,
      context: Context
  )(implicit worker: Worker[Actor, Context]): GlobalEvent => UpdatedWorkSchedule[Actor, Context] = {
    case JobMarketEvent.Allocate(allocationId, job) if allocationId === id =>
      UpdatedWorkSchedule(
        WorkSchedule(workSchedule.id, job :: workSchedule.jobStack),
        actor
      )

    case JobMarketEvent.NothingFound(allocationId) if allocationId === id =>
      updateWorkSchedule[Actor, Context](workSchedule, gameTime, actor, context)

    case FrameTick =>
      updateWorkSchedule[Actor, Context](workSchedule, gameTime, actor, context)

    case _ =>
      UpdatedWorkSchedule(workSchedule, actor)
  }

  def updateWorkSchedule[Actor, Context](
      workSchedule: WorkSchedule[Actor, Context],
      gameTime: GameTime,
      actor: Actor,
      context: Context
  )(implicit worker: Worker[Actor, Context]): UpdatedWorkSchedule[Actor, Context] =
    workSchedule.jobStack match {
      case Nil =>
        UpdatedWorkSchedule(
          WorkSchedule(workSchedule.id, worker.generateJobs()),
          actor
        )

      case current :: _ if worker.isJobComplete(actor)(current) =>
        val completed = worker.onJobComplete(actor, context)(current)

        UpdatedWorkSchedule(
          WorkSchedule(workSchedule.id, completed.jobs ++ workSchedule.jobStack.drop(1)),
          actor,
          completed.events
        )

      case h :: t =>
        val res: (Job, Actor) = worker.workOnJob(gameTime, actor, context)(h)
        UpdatedWorkSchedule(
          WorkSchedule(workSchedule.id, res._1 :: t),
          res._2
        )
    }

  def destroy[Actor, Context](workSchedule: WorkSchedule[Actor, Context]): DestroyedSchedule[Actor, Context] =
    DestroyedSchedule(
      WorkSchedule(workSchedule.id, Nil),
      workSchedule.jobStack.filterNot(_.isLocal).map(j => JobMarketEvent.Post(j))
    )

}

final case class UpdatedWorkSchedule[Actor, Context](
    workSchedule: WorkSchedule[Actor, Context],
    actor: Actor,
    events: List[GlobalEvent]
) {
  def addEvents(newEvents: List[GlobalEvent]): UpdatedWorkSchedule[Actor, Context] =
    UpdatedWorkSchedule(workSchedule, actor, events ++ newEvents)
}
object UpdatedWorkSchedule {
  def apply[Actor, Context](workSchedule: WorkSchedule[Actor, Context], actor: Actor): UpdatedWorkSchedule[Actor, Context] =
    UpdatedWorkSchedule(workSchedule, actor, Nil)
}

final case class DestroyedSchedule[Actor, Context](workSchedule: WorkSchedule[Actor, Context], events: List[GlobalEvent])
