package com.example.jobs

import indigo._

object View {

  def present(model: Model): SceneUpdateFragment =
    SceneUpdateFragment(
      drawHut.moveTo(150, 50),
      drawWood(model.woodPiles),
      drawBob(model.bob),
      drawTrees(model.grove),
      Text(model.woodCollected.toString(), 10, 10, 1, Assets.fontKey, Material.Bitmap(Assets.font))
    )

  def drawHut: Group =
    Group(
      // Roof
      Assets.blueDot.moveTo(-8, -16),
      Assets.blueDot.moveTo(8, -16),
      // Left wall
      Assets.blueDot.moveTo(-16, 0),
      Assets.blueDot.moveTo(-16, 16),
      // Right wall
      Assets.blueDot.moveTo(16, 0),
      Assets.blueDot.moveTo(16, 16)
    )

  def drawBob(bob: Bob): Graphic[Material.Bitmap] =
    Assets.redDot.moveTo(bob.position)

  def drawTrees(grove: Grove): Group =
    Group(
      grove.trees
        .map { tree =>
          val scale: Double = tree.growth.value.toDouble / 100.toDouble

          Assets.greenDot
            .moveTo(tree.position)
            .scaleBy(scale, scale)
        }
    )

  def drawWood(woodPiles: Batch[Wood]): Group =
    Group(
      woodPiles.map { wood =>
        Assets.yellowDot
          .moveTo(wood.position)
      }
    )

}
