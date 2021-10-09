package indigoextras.jobs

import indigo.shared.Outcome
import indigo.shared.datatypes.BindingKey
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemFrameContext

/**
  * The JobMarket is a subsystem that manages a global pool of available jobs.
  *
  * Not all jobs are available to all workers however.
  *
  * All interaction with the job market is done by a series of events.
  *
  * @param availableJobs Jobs currently available for allocation to workers.
  */
final case class JobMarket(availableJobs: List[Job]) extends SubSystem {
  type EventType      = JobMarketEvent
  type SubSystemModel = List[Job]

  val eventFilter: GlobalEvent => Option[JobMarketEvent] = {
    case e: JobMarketEvent => Option(e)
    case _                 => None
  }

  val initialModel: Outcome[List[Job]] =
    Outcome(availableJobs)

  private given CanEqual[Option[Job], Option[Job]] = CanEqual.derived

  def update(frameContext: SubSystemFrameContext, jobs: List[Job]): JobMarketEvent => Outcome[List[Job]] = {
    case JobMarketEvent.Post(job) =>
      Outcome(jobs ++ List(job))

    case JobMarketEvent.Find(id, canTakeJob) =>
      JobMarket.findJob(jobs, canTakeJob) match {
        case (None, _) =>
          Outcome(jobs)
            .addGlobalEvents(JobMarketEvent.NothingFound(id))

        case (Some(job), updatedJobsList) =>
          Outcome(updatedJobsList)
            .addGlobalEvents(JobMarketEvent.Allocate(id, job))
      }

    case _ =>
      Outcome(jobs)
  }

  def present(frameContext: SubSystemFrameContext, jobs: List[Job]): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
}

object JobMarket {

  /**
    * Creates an empty JobMarket
    *
    * @return An empty JobMarket
    */
  def subSystem: JobMarket =
    JobMarket(Nil)

  def apply(availableJobs: Job*): JobMarket =
    JobMarket(availableJobs.toList)

  def findJob(availableJobs: List[Job], canTakeJob: Job => Boolean): (Option[Job], List[Job]) = {
    @annotation.tailrec
    def rec(remaining: List[Job], acc: List[Job]): (Option[Job], List[Job]) =
      remaining match {
        case Nil =>
          (None, acc)

        case j :: js if canTakeJob(j) =>
          (Option(j), acc ++ js)

        case j :: js =>
          rec(js, j :: acc)
      }

    rec(availableJobs.sortBy(_.priority), Nil)
  }

}

/**
  * Events that are used to manage the JobMarket
  */
sealed trait JobMarketEvent extends GlobalEvent with Product with Serializable derives CanEqual
object JobMarketEvent {

  /**
    * An event to Post a job onto the global market
    *
    * @param job the job to put onto the marker
    */
  final case class Post(job: Job) extends JobMarketEvent

  /**
    * An event emitted by a worker, used to try and find a job to do.
    *
    * @param workerId the workers ID, so that we can report back to the requester.
    * @param canTakeJob A predicate job discriminator supplied by the worker to decide if a job can be done by the requesting worker.
    */
  final case class Find(workerId: BindingKey, canTakeJob: Job => Boolean) extends JobMarketEvent

  /**
    * An event that represents a job that has been found, for delivery/allocation to the worker.
    *
    * @param workerId the id of the worker the job is being sent to.
    * @param job the job to be given to the worker.
    */
  final case class Allocate(workerId: BindingKey, job: Job) extends JobMarketEvent

  /**
    * An event representing that no job could be found for the worker.
    *
    * @param workerId the id of the worker who made the request.
    */
  final case class NothingFound(workerId: BindingKey) extends JobMarketEvent
}
