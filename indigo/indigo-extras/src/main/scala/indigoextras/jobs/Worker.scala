package indigoextras.jobs

import indigo.shared.time.GameTime
import indigo.shared.events.GlobalEvent

trait Worker[Actor, Context] {
  def isJobComplete(actor: Actor): Job => Boolean
  def onJobComplete(actor: Actor, context: Context): Job => JobComplete
  def workOnJob(gameTime: GameTime, actor: Actor, context: Context): Job => (Job, Actor)
  def generateJobs: () => List[Job]
  def canTakeJob(actor: Actor): Job => Boolean
}
object Worker {

  def create[Actor, Context](
      isComplete: Actor => Job => Boolean,
      onComplete: (Actor, Context) => Job => JobComplete,
      doWork: (GameTime, Actor, Context) => Job => (Job, Actor),
      jobGenerator: () => List[Job],
      jobAcceptable: (Actor, Job) => Boolean
  ): Worker[Actor, Context] =
    new Worker[Actor, Context] {
      def isJobComplete(actor: Actor): Job => Boolean =
        isComplete(actor)

      def onJobComplete(actor: Actor, context: Context): Job => JobComplete =
        onComplete(actor, context)

      def workOnJob(gameTime: GameTime, actor: Actor, context: Context): Job => (Job, Actor) =
        doWork(gameTime, actor, context)

      def generateJobs: () => List[Job] =
        jobGenerator

      def canTakeJob(actor: Actor): Job => Boolean =
        jobAcceptable(actor, _)
    }

}

final case class JobComplete(jobs: List[Job], events: List[GlobalEvent])
object JobComplete {
  val empty: JobComplete =
    JobComplete(Nil, Nil)
}
