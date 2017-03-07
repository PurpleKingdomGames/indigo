package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._

object MyView {

  def updateView(currentState: Stuff): SceneGraphRootNode =
    SceneGraphRootNode(
      backgroundLayer(currentState),
      layerOfStuff(currentState)
    )

  def backgroundLayer(currentState: Stuff): SceneGraphLayer = SceneGraphLayer {
    Graphic(-1, -1, 392, 239, 10000, MyAssets.sludge)
  }

  def layerOfStuff(currentState: Stuff): SceneGraphLayer = SceneGraphLayer {
    SceneGraphNodeBranch(
      List(
        currentState.dude.walkDirection match {
          case d @ DudeLeft =>
            currentState.dude.dude.sprite.moveTo(150, 0).changeCycle(d.cycleName).play()
          case d @ DudeRight =>
            currentState.dude.dude.sprite.moveTo(150, 0).changeCycle(d.cycleName).play()
          case d @ DudeUp =>
            currentState.dude.dude.sprite.moveTo(150, 0).changeCycle(d.cycleName).play()
          case d @ DudeDown =>
            currentState.dude.dude.sprite
              .moveTo(150, 0)
              .changeCycle(d.cycleName)
              .play()

          case d @ DudeIdle =>
            currentState.dude.dude.sprite.moveTo(150, 0).changeCycle(d.cycleName).play()
        }
      ) ++
        currentState.blocks.blocks.map { b =>
          Graphic(b.x, b.y, 64, 64, b.zIndex, b.textureName)
            .withAlpha(b.alpha)
            .withTint(b.tint.r, b.tint.g, b.tint.b)
            .flipHorizontal(b.flipH)
            .flipVertical(b.flipV)
        } ++
        {
          if(MyModel.asepriteSprite.isEmpty) Nil
          else List(MyModel.asepriteSprite.get.withBindingKey("aseprite_test").play())
        } ++
        List(
          Sprite(BindingKey("tl1"), 0, 128, 64, 64, 3, MyAssets.trafficLightsName,
            Animations(128, 128,
              Cycle("trafficlights", Frame(0, 0, 64, 64))
                .addFrame(Frame(64, 0, 64, 64))
                .addFrame(Frame(0, 64, 64, 64))
            )
          ).jumpToLastFrame(),
          Text("ABC", 100, 100, 10,
            FontInfo(64, 72, MyAssets.fontName, 888, 640, FontChar("A", Point(8, 215)))
              .addChar(FontChar("B", Point(8 + 64, 215)))
              .addChar(FontChar("C", Point(8 + 64 + 64, 215)))
          )
        )
    )
  }

}
