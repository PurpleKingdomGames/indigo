package com.example.confetti

import indigo._

object Assets:

  val dots: AssetName = AssetName("dots")

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(dots, AssetPath("assets/dots.png"))
    )

  val redDot: Graphic[Material.Bitmap]    = Graphic(Rectangle(0, 0, 16, 16), 1, Material.Bitmap(dots)).withRef(8, 8)
  val greenDot: Graphic[Material.Bitmap]  = Graphic(Rectangle(16, 0, 16, 16), 1, Material.Bitmap(dots)).withRef(8, 8)
  val blueDot: Graphic[Material.Bitmap]   = Graphic(Rectangle(0, 16, 16, 16), 1, Material.Bitmap(dots)).withRef(8, 8)
  val yellowDot: Graphic[Material.Bitmap] = Graphic(Rectangle(16, 16, 16, 16), 1, Material.Bitmap(dots)).withRef(8, 8)

