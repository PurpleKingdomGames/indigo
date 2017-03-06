package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._

object MyView {

  def updateView(currentState: Stuff): SceneGraphNode = {
    SceneGraphNodeBranch(
      currentState.blocks.blocks.map { b =>
        Graphic(b.x, b.y, 64, 64, b.zIndex, b.textureName)
          .withAlpha(b.alpha)
          .withTint(b.tint.r, b.tint.g, b.tint.b)
          .flipHorizontal(b.flipH)
          .flipVertical(b.flipV)
      } ++
        {
          if(MyModel.asepriteSprite.isEmpty) Nil else {
//            MyModel.asepriteSprite = MyModel.asepriteSprite.map(_.nextFrame)
            //            println(MyModel.asepriteSprite.get.animations.currentCycle.playheadPosition)
            List(MyModel.asepriteSprite.get)
          }
        } ++
        List(
          Sprite(BindingKey.generate, 0, 128, 64, 64, 3, MyAssets.trafficLightsName,
            Animations(128, 128,
              Cycle("trafficlights", Frame(0, 0, 64, 64))
                .addFrame(Frame(64, 0, 64, 64))
                .addFrame(Frame(0, 64, 64, 64))
            )
          ).play(),
          Text("cba", 100, 100, 10,
            FontInfo(64, 72, MyAssets.fontName, 888, 640, FontChar("A", Point(8, 215)))
              .addChar(FontChar("B", Point(8 + 64, 215)))
              .addChar(FontChar("C", Point(8 + 64 + 64, 215)))
          )
        )
    )
  }

}
