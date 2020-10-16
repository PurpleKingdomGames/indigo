package indigoextras.jobs

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.dice.Dice

/**
  * Represents a Worker for a given Actor
  */
trait Worker[Actor, Context] {

  /**
    * Test whether an actor thinks a job is complete
    *
    * @param actor the Actor doing the work
    * @return Job => Boolean
    */
  def isJobComplete(actor: Actor): Job => Boolean

  /**
    * When a job is completed,
    *
    * @param actor The actor instance
    * @param context Some context in which to do the work
    * @return Job => JobComplete
    */
  def onJobComplete(actor: Actor, context: Context): Job => Outcome[List[Job]]

  /**
    * A function describing how this actor does work on whichever jobs they are able to work on
    *
    * @param gameTime The current game time
    * @param actor The actor instance
    * @param context Some context in which to do the work
    * @return
    */
  def workOnJob(gameTime: GameTime, actor: Actor, context: Context): Job => (Job, Actor)
  def generateJobs(gameTime: GameTime, dice: Dice): List[Job]
  def canTakeJob(actor: Actor): Job => Boolean
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
      isComplete: Actor => Job => Boolean,
      onComplete: (Actor, Context) => Job => Outcome[List[Job]],
      doWork: (GameTime, Actor, Context) => Job => (Job, Actor),
      jobGenerator: (GameTime, Dice) => List[Job],
      jobAcceptable: (Actor, Job) => Boolean
  ): Worker[Actor, Context] =
    new Worker[Actor, Context] {
      def isJobComplete(actor: Actor): Job => Boolean =
        isComplete(actor)

      def onJobComplete(actor: Actor, context: Context): Job => Outcome[List[Job]] =
        onComplete(actor, context)

      def workOnJob(gameTime: GameTime, actor: Actor, context: Context): Job => (Job, Actor) =
        doWork(gameTime, actor, context)

      def generateJobs(gameTime: GameTime, dice: Dice): List[Job] =
        jobGenerator(gameTime, dice)

      def canTakeJob(actor: Actor): Job => Boolean =
        jobAcceptable(actor, _)
    }

}
