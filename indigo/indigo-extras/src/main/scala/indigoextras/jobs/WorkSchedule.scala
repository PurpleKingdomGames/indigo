package indigoextras.jobs

import indigo.shared.time.GameTime
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.datatypes.BindingKey
import indigo.shared.Outcome
import indigo.shared.dice.Dice

/**
  * Represents an Actor's work schedule
  *
  * @param id
  * @param jobStack
  */
final case class WorkSchedule[Actor, Context](val id: BindingKey, val worker: Worker[Actor, Context], val jobStack: List[Job]) derives CanEqual {

  /**
    * Give the job currently being worked on
    *
    * @return Option[Job]
    */
  def currentJob: Option[Job] =
    WorkSchedule.current(this)

  /**
    * When supplied with a global event, creates an outcome of the updated work schedule.
    * 
    * The update function coordinates all of the work for this worker, creating work, finding jobs, working on tasks etc.
    *
    * @param gameTime
    * @param dice
    * @param actor
    * @param context
    * @return
    */
  def update(gameTime: GameTime, dice: Dice, actor: Actor, context: Context): GlobalEvent => Outcome[WorkProgressReport[Actor, Context]] =
    WorkSchedule.update(id, this, gameTime, dice, actor, context, worker)

  /**
    * The significance of this function is that any local jobs are lost, but any global jobs are returned to the JobMarket.
    *
    * @return An Outcome of an empty work schedule of the same type.
    */
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
  ): GlobalEvent => Outcome[WorkProgressReport[Actor, Context]] = {
    case JobMarketEvent.Allocate(allocationId, job) if allocationId == id =>
      Outcome(
        WorkProgressReport(
          workSchedule.copy[Actor, Context](
            jobStack = job :: workSchedule.jobStack
          ),
          actor
        )
      )

    case JobMarketEvent.NothingFound(allocationId) if allocationId == id =>
      updateWorkSchedule[Actor, Context](workSchedule, WorkContext(gameTime, dice, actor, context), worker)

    case FrameTick =>
      updateWorkSchedule[Actor, Context](workSchedule, WorkContext(gameTime, dice, actor, context), worker)

    case _ =>
      Outcome(
        WorkProgressReport(workSchedule, actor)
      )
  }

  def updateWorkSchedule[Actor, Context](
      workSchedule: WorkSchedule[Actor, Context],
      workContext: WorkContext[Actor, Context],
      worker: Worker[Actor, Context]
  ): Outcome[WorkProgressReport[Actor, Context]] =
    workSchedule.jobStack match {
      case Nil =>
        Outcome(
          WorkProgressReport(
            workSchedule.copy[Actor, Context](
              jobStack = worker.generateJobs(workContext)
            ),
            workContext.actor
          )
        ).addGlobalEvents(JobMarketEvent.Find(workSchedule.id, worker.canTakeJob(workContext)))

      case current :: _ if worker.isJobComplete(workContext)(current) =>
        worker.onJobComplete(workContext)(current).map {
          case (jobs, actor) =>
            WorkProgressReport(
              workSchedule.copy[Actor, Context](
                jobStack = jobs ++ workSchedule.jobStack.drop(1)
              ),
              actor
            )
        }

      case h :: t =>
        val res: (Job, Actor) = worker.workOnJob(workContext)(h)
        Outcome(
          WorkProgressReport(
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

/**
  * Encapsulates an updated schedule and an updated actor.
  * Work is done by workers, but work can also affect workers, e.g. making them stronger, smarter, or tired.
  *
  * @param workSchedule The updated work schedule.
  * @param actor The updated actor.
  */
final case class WorkProgressReport[Actor, Context](workSchedule: WorkSchedule[Actor, Context], actor: Actor) derives CanEqual
