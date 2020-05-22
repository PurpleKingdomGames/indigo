package indigoextras.jobs

import indigo.shared.subsystems.SubSystem
import indigo.shared.events.GlobalEvent
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.datatypes.BindingKey
import indigo.shared.FrameContext

final case class JobMarket(jobs: List[Job]) extends SubSystem {

  type EventType = JobMarketEvent

  val eventFilter: GlobalEvent => Option[JobMarketEvent] = {
    case e: JobMarketEvent => Option(e)
    case _                 => None
  }

  def update(frameContext: FrameContext): JobMarketEvent => Outcome[SubSystem] = {
    case JobMarketEvent.Post(job) =>
      Outcome(
        this.copy(jobs = jobs :+ job)
      )

    case JobMarketEvent.Find(id, canTakeJob) =>
      JobMarket.findJob(jobs, canTakeJob, Nil) match {
        case (None, _) =>
          Outcome(this)
            .addGlobalEvents(JobMarketEvent.NothingFound(id))

        case (Some(job), updatedJobsList) =>
          Outcome(this.copy(jobs = updatedJobsList))
            .addGlobalEvents(JobMarketEvent.Allocate(id, job))
      }

    case _ =>
      Outcome(this)
  }

  def render(frameContext: FrameContext): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

object JobMarket {

  def subSystem: JobMarket =
    JobMarket(Nil)

  @annotation.tailrec
  def findJob(remaining: List[Job], canTakeJob: Job => Boolean, acc: List[Job]): (Option[Job], List[Job]) =
    remaining match {
      case Nil =>
        (None, acc)

      case j :: js if canTakeJob(j) =>
        (Option(j), acc ++ js)

      case j :: js =>
        findJob(js, canTakeJob, j :: acc)
    }

}

sealed trait JobMarketEvent extends GlobalEvent with Product with Serializable
object JobMarketEvent {
  final case class Post(job: Job)                                   extends JobMarketEvent
  final case class Find(id: BindingKey, canTakeJob: Job => Boolean) extends JobMarketEvent
  final case class Allocate(id: BindingKey, job: Job)               extends JobMarketEvent
  final case class NothingFound(id: BindingKey)                     extends JobMarketEvent
}
