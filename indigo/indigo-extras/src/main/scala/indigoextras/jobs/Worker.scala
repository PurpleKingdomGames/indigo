package indigoextras.jobs

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.dice.Dice

/**
  * Represents a Worker for a given Actor
  */
trait Worker[Actor, Context] {

  /**
    * Test whether an actor thinks a job is complete.
    *
    * @param context Information about the context the worker is in
    * @return Job => Boolean
    */
  def isJobComplete(context: WorkContext[Actor, Context]): Job => Boolean

  /**
    * When a job is completed, produce an outcome of any new jobs and an updated Actor.
    *
    * @param context Information about the context the worker is in
    * @return Job => Outcome[(List[Job], Actor)]
    */
  def onJobComplete(context: WorkContext[Actor, Context]): Job => Outcome[(List[Job], Actor)]

  /**
    * A function describing how this actor does work on whichever jobs they are able to work on.
    *
    * @param context Information about the context the worker is in
    * @return Job => (Job, Actor)
    */
  def workOnJob(context: WorkContext[Actor, Context]): Job => (Job, Actor)

  /**
    * The worker has nothing to do, create jobs from within the given context.
    *
    * @param context Information about the context the worker is in
    * @return List[Job]
    */
  def generateJobs(context: WorkContext[Actor, Context]): List[Job]

  /**
    * Predicate discriminator used to determine if a worker can carry out a job.
    *
    * @param context Information about the context the worker is in
    * @return Job => Boolean
    */
  def canTakeJob(context: WorkContext[Actor, Context]): Job => Boolean
}
object Worker {

  /**
    * Convenience function for creating Worker instances.
    *
    * @param isComplete has the work been completed?
    * @param onComplete results of work completion, more jobs or events.
    * @param doWork a function the explains how the worker does the work
    * @param jobGenerator when called, generates jobs.
    * @param jobAcceptable predicate used by the job market to decide if a job is suitable for this worker
    * @return a Worker instance
    */
  def create[Actor, Context](
      isComplete: WorkContext[Actor, Context] => Job => Boolean,
      onComplete: WorkContext[Actor, Context] => Job => Outcome[(List[Job], Actor)],
      doWork: WorkContext[Actor, Context] => Job => (Job, Actor),
      jobGenerator: WorkContext[Actor, Context] => List[Job],
      jobAcceptable: WorkContext[Actor, Context] => Job => Boolean
  ): Worker[Actor, Context] =
    new Worker[Actor, Context] {
      def isJobComplete(context: WorkContext[Actor, Context]): Job => Boolean =
        isComplete(context)

      def onJobComplete(context: WorkContext[Actor, Context]): Job => Outcome[(List[Job], Actor)] =
        onComplete(context)

      def workOnJob(context: WorkContext[Actor, Context]): Job => (Job, Actor) =
        doWork(context)

      def generateJobs(context: WorkContext[Actor, Context]): List[Job] =
        jobGenerator(context)

      def canTakeJob(context: WorkContext[Actor, Context]): Job => Boolean =
        jobAcceptable(context)
    }

}

/**
  * The context the work is being done in, similar to frame context, work is not done in a vacuum.
  *
  * @param gameTime The supplied game time.
  * @param dice A dice for random number generation.
  * @param actor An instance of the actor, for asking question like "Can the actor do the work?"
  * @param context A free form type, e.g.: A list of other characters the worker can see.
  */
final case class WorkContext[Actor, Context](
    gameTime: GameTime,
    dice: Dice,
    actor: Actor,
    context: Context
)
