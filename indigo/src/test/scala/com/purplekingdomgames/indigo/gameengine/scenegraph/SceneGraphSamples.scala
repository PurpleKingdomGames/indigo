package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._

object SceneGraphSamples {

  case class TestViewDataType()

  val api: SceneGraphRootNode =
    SceneGraphRootNode(
      SceneGraphLayer(
        Group(
          Text(
            "Hello", 10, 10, 1,
            FontInfo("ref", 32, 32, FontChar("a", 0, 0, 16, 16))
          ),
          Graphic(10, 10, 32, 32, 1, "ref"),
          Sprite(
            BindingKey("test"), 10, 10, 32, 32, 1, "ref",
            Animations(
              64, 32,
              Cycle("label", Frame(0, 0, 32, 32))
                .addFrame(Frame(32, 0, 32, 32))
            )
          ),
          Group(
            Graphic(10, 10, 32, 32, 1, "ref1"),
            Graphic(10, 10, 32, 32, 1, "ref2"),
            Graphic(10, 10, 32, 32, 1, "ref3")
          )
        )
      ),
      SceneGraphLightingLayer.empty,
      SceneGraphUiLayer.empty
    )

}
