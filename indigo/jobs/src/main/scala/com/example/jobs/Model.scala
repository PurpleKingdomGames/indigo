package com.example.jobs

import indigo._
import indigoextras.jobs.Worker
import indigoextras.jobs.WorkSchedule
import indigoextras.jobs.WorkScheduleUpdate
import indigoextras.jobs.Job

import indigo.shared.EqualTo._
import indigoextras.datatypes.TimeVaryingValue
import indigoextras.jobs.JobMarketEvent

final case class Model(bob: Bob, grove: Grove, woodPiles: List[Point], woodCollected: Int) {

  def update(gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[Model] = {
    case e @ FrameTick =>
      Outcome.combine(bob.update(gameTime, dice)(e), grove.update(gameTime.running)).map {
        case (b, g) =>
          this.copy(
            bob = b,
            grove = g
          )
      }

    case e @ JobMarketEvent.Allocate(_, _) =>
      bob.update(gameTime, dice)(e).map {
        case b =>
          this.copy(
            bob = b
          )
      }

    case _ =>
      Outcome(this)
  }

  def removeTreeWithIndex(index: Int): Outcome[Model] =
    Outcome(
      this.copy(
        grove = grove.removeTreeWithIndex(index)
      )
    )

}
object Model {

  def initialModel(startupData: StartupData): Model =
    Model(
      bob = Bob(
        position = Point(150 - 16, 90),
        workSchedule = WorkSchedule((BindingKey("bob")))
      ),
      grove = Grove(
        startupData.trees.map {
          case TreeData(i, v, gr) =>
            Tree(
              index = i,
              position = Point((v.x * 25d).toInt, (v.y * 25d).toInt) + Point(50, 150),
              growth = TimeVaryingValue(0, Seconds.zero),
              growthRate = 10 + (10 * gr).toInt,
              ready = false
            )
        }
      ),
      woodPiles = Nil,
      woodCollected = 0
    )

}

final case class Grove(trees: List[Tree]) {

  def update(runningTime: Seconds): Outcome[Grove] =
    Outcome
      .sequence(trees.map(_.update(runningTime)))
      .map(ts => this.copy(trees = ts))

  def removeTreeWithIndex(index: Int): Grove =
    this.copy(trees = trees.filter(_.index !== index))

}

final case class Tree(index: Int, position: Point, growth: TimeVaryingValue[Int], growthRate: Int, ready: Boolean) {

  def update(runningTime: Seconds): Outcome[Tree] =
    if (ready) Outcome(this)
    else {
      val nextGrowth = growth.increaseTo(100, growthRate, runningTime)
      val isReady    = nextGrowth.value === 100

      Outcome(
        this.copy(
          growth = nextGrowth,
          ready = isReady
        )
      ).addGlobalEvents(
        if (isReady) List(JobMarketEvent.Post(ChopDown(index, position))) else Nil
      )
    }

}

// Bob is our NPC 'Actor'
final case class Bob(position: Point, workSchedule: WorkSchedule[Bob, Unit]) {

  def update(gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[Bob] =
    e => {
      val nextJobs =
        workSchedule.update(gameTime, dice, this, ())(e)

      nextJobs.map {
        case WorkScheduleUpdate(s, b) =>
          b.copy(workSchedule = s)
      }
    }

}

object Bob {

  val loiterPositionA: Point = Point(150 - 16, 90)
  val loiterPositionB: Point = Point(150 + 16, 90)

  implicit val bobWorker: Worker[Bob, Unit] =
    new Worker[Bob, Unit] {

      def isJobComplete(bob: Bob): Job => Boolean = {
        case ChopDown(_, position) if bob.position === position =>
          true

        case ChopDown(_, _) =>
          false

        case Pace(to) if bob.position === to =>
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

      def onJobComplete(bob: Bob, context: Unit): Job => Outcome[List[Job]] = {
        case ChopDown(index, _) =>
          Outcome(Nil).addGlobalEvents(RemoveTree(index))

        case Pace(_) =>
          Outcome(Nil)

        case Idle(_) =>
          Outcome(Nil)

        case _ =>
          Outcome(Nil)
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

      def workOnJob(gameTime: GameTime, bob: Bob, context: Unit): Job => (Job, Bob) = {
        case job @ ChopDown(_, destination) =>
          (job, bob.copy(position = moveTowards(bob.position, destination)))

        case job @ Pace(to) =>
          (job, bob.copy(position = moveTowards(bob.position, to)))

        case Idle(percentDone) =>
          (Idle(percentDone.increaseTo(100, 20, gameTime.running)), bob)

        case job =>
          (job, bob)
      }

      def generateJobs(gameTime: GameTime, dice: Dice): List[Job] =
        List(
          Pace(loiterPositionB),
          Pace(loiterPositionA),
          Idle(TimeVaryingValue(0, gameTime.running))
        )

      def canTakeJob(bob: Bob): Job => Boolean = {
        case ChopDown(_, _) =>
          true

        case _ =>
          false
      }

    }

}
