package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine.scenegraph._

object MyView {

  def updateView(currentState: Stuff): SceneGraphNode = {
    SceneGraphNodeBranch(
      currentState.blocks.blocks.map { b =>
        Graphic(Rectangle(Point(b.x, b.y), Point(64, 64)), Depth(b.zIndex), b.textureName, ref = Point(0, 0), crop = None, effects = Effects.default)
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
          Sprite(
            bindingKey = BindingKey.generate,
            bounds = Rectangle(Point(0, 128), Point(64, 64)),
            depth = Depth(3),
            imageAssetRef = MyAssets.trafficLightsName,
            animations =
              Animations(
                Point(128, 128),
                Cycle(
                  label = "trafficlights",
                  playheadPosition = 0,
                  frame = Frame(
                    bounds = Rectangle(
                      Point(0, 0),
                      Point(64, 64)
                    ),
                    current = currentState.trafficLights.isRed
                  ),
                  frames = Nil,
                  current = true
                ).addFrame(
                  frame = Frame(
                    bounds = Rectangle(
                      Point(64, 0),
                      Point(64, 64)
                    ),
                    current = currentState.trafficLights.isAmber
                  )
                ).addFrame(
                  frame = Frame(
                    bounds = Rectangle(
                      Point(0, 64),
                      Point(64, 64)
                    ),
                    current = currentState.trafficLights.isGreen
                  )
                ),
                cycles = Nil
              ),
            ref = Point(0, 0),
            effects = Effects.default
          ),
          Text(
            text = "CBA",
            alignment = AlignLeft,
            position = Point(100, 100),
            depth = Depth(10),
            fontInfo = FontInfo(
              charSize = Point(64, 72),
              fontSpriteSheet = FontSpriteSheet(
                imageAssetRef = MyAssets.fontName,
                size = Point(888, 640)
              ),
              fontChar = FontChar("A", Point(8, 215)),
              fontChars = Nil
            )
              .addChar(FontChar("B", Point(8 + 64, 215)))
              .addChar(FontChar("C", Point(8 + 64 + 64, 215))),
            effects = Effects.default
          )
        )
    )
  }

}
