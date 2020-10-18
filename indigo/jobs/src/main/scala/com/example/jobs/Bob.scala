package com.example.jobs

import indigo._
import indigoextras.jobs._
import indigo.shared.EqualTo._
import indigoextras.datatypes.TimeVaryingValue

// Bob is our NPC 'Actor'
final case class Bob(position: Point, workSchedule: WorkSchedule[Bob, Unit]) {

  def update(gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[Bob] =
    e => {
      val nextJobs =
        workSchedule.update(gameTime, dice, this, ())(e)

      nextJobs.map {
        case WorkProgressReport(nextSchedule, nextBob) =>
          nextBob.copy(workSchedule = nextSchedule)
      }
    }

}

object Bob {

  val loiterPositionA: Point = Point(150 - 16, 90)
  val loiterPositionB: Point = Point(150 + 16, 90)

  def initial: Bob =
    Bob(
      position = Point(150 - 16, 90),
      workSchedule = WorkSchedule((BindingKey("bob")))
    )

  implicit val bobWorker: Worker[Bob, Unit] =
    new Worker[Bob, Unit] {

      def isJobComplete(context: WorkContext[Bob, Unit]): Job => Boolean = {
        case ChopDown(_, position) if context.actor.position === position =>
          true

        case ChopDown(_, _) =>
          false

        case CollectWood(wood) if context.actor.position === wood.position =>
          true

        case CollectWood(_) =>
          false

        case Pace(to) if context.actor.position === to =>
          true

        case Pace(_) =>
          false

        case Idle(percentDone) if percentDone.value === 100 =>
          true

        case Idle(_) =>
          false

        case _ =>
          true
      }

      def onJobComplete(context: WorkContext[Bob, Unit]): Job => Outcome[(List[Job], Bob)] = {
        case ChopDown(index, position) =>
          Outcome((Nil, context.actor)).addGlobalEvents(RemoveTree(index), DropWood(position))

        case CollectWood(wood) =>
          Outcome((Nil, context.actor)).addGlobalEvents(RemoveWood(wood.id))

        case Pace(_) =>
          Outcome((Nil, context.actor))

        case Idle(_) =>
          Outcome((Nil, context.actor))

        case _ =>
          Outcome((Nil, context.actor))
      }

      private def moveTowards(position: Point, target: Point): Point =
        Point(
          x =
            if (target.x === position.x) target.x
            else if (target.x < position.x) position.x - 1
            else position.x + 1,
          y =
            if (target.y === position.y) target.y
            else if (target.y < position.y) position.y - 1
            else position.y + 1
        )

      def workOnJob(context: WorkContext[Bob, Unit]): Job => (Job, Bob) = {
        case job @ ChopDown(_, destination) =>
          (job, context.actor.copy(position = moveTowards(context.actor.position, destination)))

        case job @ CollectWood(wood) =>
          (job, context.actor.copy(position = moveTowards(context.actor.position, wood.position)))

        case job @ Pace(to) =>
          (job, context.actor.copy(position = moveTowards(context.actor.position, to)))

        case job @ Idle(_) if context.actor.position !== loiterPositionA =>
          (job, context.actor.copy(position = moveTowards(context.actor.position, loiterPositionA)))

        case Idle(percentDone) =>
          (Idle(percentDone.increaseTo(100, 35, context.gameTime.running)), context.actor)

        case job =>
          (job, context.actor)
      }

      def generateJobs(context: WorkContext[Bob, Unit]): List[Job] = {
        val noTimesToPace: List[Job] =
          (1 to context.dice.roll(3)).toList.map { _ =>
            List(Pace(loiterPositionB), Pace(loiterPositionA))
          }.flatten

        List(Idle(TimeVaryingValue(0, context.gameTime.running))) ++
          noTimesToPace ++
          List(Idle(TimeVaryingValue(0, context.gameTime.running)))
      }

      def canTakeJob(context: WorkContext[Bob, Unit]): Job => Boolean = {
        case ChopDown(_, _) =>
          true

        case CollectWood(_) =>
          true

        case _ =>
          false
      }

    }

}
