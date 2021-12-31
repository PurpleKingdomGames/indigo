package com.example.jobs

import indigo._
import indigoextras.datatypes.IncreaseTo
import indigoextras.jobs.JobMarketEvent

final case class Model(bob: Bob, grove: Grove, woodPiles: List[Wood], woodCollected: Int) {

  def update(gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[Model] = {
    case e @ FrameTick =>
      Outcome.combine(bob.update(gameTime, dice)(e), grove.update(gameTime.delta)).map {
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

    case DropWood(position) =>
      val wood =
        List(
          Wood(BindingKey.fromDice(dice), position + Point(-8, -8)),
          Wood(BindingKey.fromDice(dice), position + Point(8, 8))
        )

      Outcome(
        this.copy(
          woodPiles = woodPiles ++ wood
        )
      ).addGlobalEvents(
        wood.map(w => JobMarketEvent.Post(CollectWood(w)))
      )

    case _ =>
      Outcome(this)
  }

  def removeTreeWithIndex(index: Int): Outcome[Model] =
    Outcome(
      this.copy(
        grove = grove.removeTreeWithIndex(index)
      )
    )

  def removeWoodWithId(id: BindingKey): Outcome[Model] =
    Outcome(
      this.copy(
        woodPiles = woodPiles.filter(_.id != id),
        woodCollected = woodCollected + 1
      )
    )

}
object Model {

  def initialModel(startupData: StartupData): Model =
    Model(
      bob = Bob.initial,
      grove = Grove(
        startupData.trees.map {
          case TreeData(i, v, gr) =>
            Tree(
              index = i,
              position = Point((v.x * 25d).toInt, (v.y * 25d).toInt) + Point(50, 150),
              growth = IncreaseTo(0, 10 + (10 * gr), 100),
              fullyGrown = false
            )
        }
      ),
      woodPiles = Nil,
      woodCollected = 0
    )

}

final case class Grove(trees: List[Tree]) {

  def update(timeDelta: Seconds): Outcome[Grove] =
    Outcome
      .sequence(trees.map(_.update(timeDelta)))
      .map(ts => this.copy(trees = ts))

  def removeTreeWithIndex(index: Int): Grove =
    this.copy(trees = trees.filter(_.index != index))

}

final case class Tree(index: Int, position: Point, growth: IncreaseTo, fullyGrown: Boolean) {

  def update(timeDelta: Seconds): Outcome[Tree] =
    if (fullyGrown) Outcome(this)
    else {
      val nextGrowth   = growth.update(timeDelta)
      val isFullyGrown = nextGrowth.toInt == 100

      Outcome(
        this.copy(
          growth = nextGrowth,
          fullyGrown = isFullyGrown
        )
      ).addGlobalEvents(
        if (isFullyGrown) List(JobMarketEvent.Post(ChopDown(index, position))) else Nil
      )
    }

}

final case class Wood(id: BindingKey, position: Point)
