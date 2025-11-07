package com.example.perf

import indigo.*

import scala.annotation.tailrec
import scala.util.Random

object PerfView {

  val cloneId: CloneId = CloneId("Dude")

  def updateView(model: DudeModel): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addLayers(
        Layer(gameLayer(model)),
        Layer(uiLayer)
      )
      .addCloneBlanks(
        CloneBlank(cloneId, model.dude.sprite)
      )

  private val herdCount: Int      = 210_000 - 1
  private val cloneBatchSize: Int = 2048

  private val positions: List[Point] =
    (1 to herdCount).toList.map { _ =>
      Point((Random.nextFloat() * PerfGame.viewportWidth).toInt, (Random.nextFloat() * PerfGame.viewportHeight).toInt)
    }

  private val theHerd: Batch[CloneBatch] = {
    @tailrec
    def rec(remaining: List[Point], batchSize: Int, batchNumber: Int, acc: List[CloneBatch]): Batch[CloneBatch] =
      remaining match {
        case Nil =>
          Batch.fromList(acc)

        case rs =>
          val (l, r) = rs.splitAt(batchSize)

          l match
            case Nil =>
              rec(r, batchSize, batchNumber + 1, acc)

            case p :: ps =>
              rec(
                r,
                batchSize,
                batchNumber + 1,
                CloneBatch(
                  cloneId,
                  Batch.fromList(ps).map(p => CloneBatchData(p.x, p.y))
                ).withStaticBatchKey(BindingKey("herd" + batchNumber.toString)) :: acc
              )
      }

    rec(positions, cloneBatchSize, 0, Nil)
  }

  def gameLayer(currentState: DudeModel): Batch[SceneNode] =
    Batch(
      currentState.walkDirection match {
        case d @ DudeLeft =>
          currentState.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeRight =>
          currentState.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeUp =>
          currentState.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeDown =>
          currentState.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeIdle =>
          currentState.dude.sprite
            .changeCycle(d.cycleName)
            .play()
      }
    ) ++ theHerd

  val uiLayer: Batch[SceneNode] =
    Batch(
      Text(
        (herdCount + 1).toString + " Naked dudes!",
        PerfGame.viewportWidth / 2,
        40,
        Fonts.fontKey,
        PerfAssets.fontMaterial
      ).alignCenter,
      Text("Thundering Herd!", PerfGame.viewportWidth / 2, 10, Fonts.fontKey, PerfAssets.fontMaterial).alignCenter
    )

}
