package com.example.jobs

import indigo._
import indigoextras.jobs._
import indigo.shared.EqualTo._
import indigoextras.datatypes.IncreaseTo

// Bob is our NPC 'Actor'
final case class Bob(position: Point, workSchedule: WorkSchedule[Bob, Unit], state: BobState) {

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

  val hutPosition: Point = Point(150, 90)

  def initial: Bob =
    Bob(
      position = Point(150 - 16, 90),
      workSchedule = WorkSchedule((BindingKey("bob"))),
      BobState.Wandering
    )

  implicit val bobWorker: Worker[Bob, Unit] =
    new Worker[Bob, Unit] {

      def isJobComplete(context: WorkContext[Bob, Unit]): Job => Boolean = {
        case ChopDown(_, position) if context.actor.position === position =>
          true

        case CollectWood(wood) if context.actor.position === wood.position =>
          true

        case Wander(to) if context.actor.position === to =>
          true

        case Idle(percentDone) if percentDone.value === 100 =>
          true

        case _ =>
          false
      }

      def onJobComplete(context: WorkContext[Bob, Unit]): Job => Outcome[(List[Job], Bob)] = {
        case ChopDown(index, position) =>
          Outcome((Nil, context.actor)).addGlobalEvents(RemoveTree(index), DropWood(position))

        case CollectWood(wood) =>
          Outcome((Nil, context.actor)).addGlobalEvents(RemoveWood(wood.id))

        case Wander(_) =>
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
          (
            job,
            context.actor.copy(
              position = moveTowards(context.actor.position, destination),
              state = BobState.Working
            )
          )

        case job @ CollectWood(wood) =>
          (
            job,
            context.actor.copy(
              position = moveTowards(context.actor.position, wood.position),
              state = BobState.Working
            )
          )

        case job @ Wander(to) =>
          (
            job,
            context.actor.copy(
              position = moveTowards(context.actor.position, to),
              state = BobState.Wandering
            )
          )

        case Idle(percentDone) =>
          (
            Idle(percentDone.update(context.gameTime.delta)),
            context.actor.copy(state = BobState.Idle)
          )

        case job =>
          (
            job,
            context.actor
          )
      }

      private val idleJob: List[Idle] =
        List(Idle(IncreaseTo(0, 75, 100)))

      def generateJobs(context: WorkContext[Bob, Unit]): List[Job] =
        context.actor.state match {
          case BobState.Idle =>
            List(Wander(Point(context.dice.roll(100) + 50, context.dice.roll(30) + 90)))

          case BobState.Wandering =>
            idleJob

          case BobState.Working =>
            idleJob
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

sealed trait BobState
object BobState {
  case object Idle      extends BobState
  case object Wandering extends BobState
  case object Working   extends BobState
}
