package com.example.perf

import indigo._

import scala.util.Random
import scala.annotation.tailrec

object PerfView {

  val cloneId: CloneId = CloneId("Dude")

  def updateView(model: DudeModel, inputState: InputState): SceneUpdateFragment = {
    inputState.mouse.mouseClickAt match {
      case Some(position) => println("Mouse clicked at: " + position.toString())
      case None           => ()
    }

    SceneUpdateFragment.empty
      .addLayers(
        Layer(gameLayer(model)),
        Layer(lightingLayer(inputState)),
        Layer(uiLayer)
      )
      .withAmbientLight(RGBA.White.withAmount(0.5))
      .addCloneBlanks(
        CloneBlank(cloneId, model.dude.sprite)
      )
  }

  private val herdCount: Int = 11999

  private val positions: List[Point] =
    (1 to herdCount).toList.map { _ =>
      Point((Random.nextFloat() * PerfGame.viewportWidth).toInt, (Random.nextFloat() * PerfGame.viewportHeight).toInt)
    }

  private val theHerd: List[CloneBatch] = {
    @tailrec
    def rec(remaining: List[Point], batchSize: Int, batchNumber: Int, acc: List[CloneBatch]): List[CloneBatch] =
      remaining match {
        case Nil =>
          acc

        case rs =>
          val (l, r) = rs.splitAt(batchSize)
          rec(
            r,
            batchSize,
            batchNumber + 1,
            CloneBatch(
              cloneId,
              Depth(1),
              CloneTransformData.identity,
              l.map(CloneTransformData.startAt),
              Some(BindingKey("herd" + batchNumber.toString))
            ) :: acc
          )
      }

    rec(positions, 256, 0, Nil)
  }

  def gameLayer(currentState: DudeModel): List[SceneGraphNode] =
    List(
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

  val staticLights: List[Graphic] =
    List(
      Graphic(0, 0, 320, 240, 1, PerfAssets.lightMaterial),
      // .withTint(1, 0, 0),
      Graphic(-115, -100, 320, 240, 1, PerfAssets.lightMaterial)
    )

  def lightingLayer(inputState: InputState): List[SceneGraphNode] =
    Graphic(inputState.mouse.position.x - 160, inputState.mouse.position.y - 120, 320, 240, 1, PerfAssets.lightMaterial) :: staticLights

  val uiLayer: List[SceneGraphNode] =
    List(
      Text((herdCount + 1).toString + " Naked dudes!", PerfGame.viewportWidth / 2, 40, 5, Fonts.fontKey, PerfAssets.fontMaterial).alignCenter,
      Text("Thundering Herd!", PerfGame.viewportWidth / 2, 10, 5, Fonts.fontKey, PerfAssets.fontMaterial).alignCenter
    )

}
