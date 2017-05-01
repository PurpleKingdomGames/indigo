package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._

object SceneGraphSamples {

  case class TestViewDataType()

  val api: SceneGraphRootNode[TestViewDataType] =
    SceneGraphRootNode[TestViewDataType](
      SceneGraphGameLayer[TestViewDataType](
        SceneGraphNodeBranch[TestViewDataType](
          Text[TestViewDataType](
            "Hello", 10, 10, 1,
            FontInfo("ref", 32, 32, FontChar("a", 0, 0, 16, 16))
          ),
          Graphic[TestViewDataType](10, 10, 32, 32, 1, "ref"),
          Sprite[TestViewDataType](
            BindingKey("test"), 10, 10, 32, 32, 1, "ref",
            Animations(
              64, 32,
              Cycle("label", Frame(0, 0, 32, 32))
                .addFrame(Frame(32, 0, 32, 32))
            )
          ),
          SceneGraphNodeBranch[TestViewDataType](
            Graphic[TestViewDataType](10, 10, 32, 32, 1, "ref1"),
            Graphic[TestViewDataType](10, 10, 32, 32, 1, "ref2"),
            Graphic[TestViewDataType](10, 10, 32, 32, 1, "ref3")
          )
        )
      ),
      SceneGraphLightingLayer.empty[TestViewDataType],
      SceneGraphUiLayer.empty[TestViewDataType]
    )

}
