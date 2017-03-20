package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import org.scalatest.{FunSpec, Matchers}

class SceneGraphInternalSpec extends FunSpec with Matchers {

  describe("Converting a public Scene graph into a private one") {

    it("should be able to do the conversion") {

      SceneGraphInternal.fromPublicFacing(SceneGraphSamples.api) shouldEqual SceneGraphSamples.internal

    }

  }

}

object SceneGraphSamples {

  val api: SceneGraphRootNode =
    SceneGraphRootNode(
      SceneGraphGameLayer(
        SceneGraphNodeBranch(
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
          SceneGraphNodeBranch(
            Graphic(10, 10, 32, 32, 1, "ref1"),
            Graphic(10, 10, 32, 32, 1, "ref2"),
            Graphic(10, 10, 32, 32, 1, "ref3")
          )
        )
      ),
      SceneGraphLightingLayer.empty,
      SceneGraphUiLayer.empty
    )

  val internal: SceneGraphRootNodeInternal =
    SceneGraphRootNodeInternal(
      SceneGraphGameLayerInternal(
        SceneGraphNodeBranchInternal(
          List(
            TextInternal(
              "Hello", Rectangle(0, 0, 80, 16), AlignLeft, Point(10, 10), Depth(1), FontInfo("ref", 32, 32, FontChar("a", 0, 0, 16, 16)), Effects.default
            ),
            GraphicInternal(
              Rectangle(10, 10, 32, 32),
              Depth(1),
              "ref",
              Point.zero,
              Rectangle(10, 10, 32, 32),
              Effects.default
            ),
            SpriteInternal(
              BindingKey("test"),
              Rectangle(10, 10, 32, 32),
              Depth(1),
              "ref",
              AnimationsInternal(
                Point(64, 32),
                CycleLabel("label"),
                nonEmtpyCycles = Map(
                  CycleLabel("label") -> CycleInternal(
                    CycleLabel("label"),
                    nonEmtpyFrames = List(
                      Frame(0, 0, 32, 32),
                      Frame(32, 0, 32, 32)
                    ),
                    playheadPosition = 0,
                    lastFrameAdvance = 0
                  )
                ),
                Nil
              ),
              Point.zero,
              Effects.default
            ),
            SceneGraphNodeBranchInternal(
              List(
                GraphicInternal(
                  Rectangle(10, 10, 32, 32),
                  Depth(1),
                  "ref1",
                  Point.zero,
                  Rectangle(10, 10, 32, 32),
                  Effects.default
                ),
                GraphicInternal(
                  Rectangle(10, 10, 32, 32),
                  Depth(1),
                  "ref2",
                  Point.zero,
                  Rectangle(10, 10, 32, 32),
                  Effects.default
                ),
                GraphicInternal(
                  Rectangle(10, 10, 32, 32),
                  Depth(1),
                  "ref3",
                  Point.zero,
                  Rectangle(10, 10, 32, 32),
                  Effects.default
                )
              )
            )
          )
        )
      ),
      SceneGraphLightingLayerInternal(SceneGraphNodeBranchInternal(Nil), AmbientLight.none),
      SceneGraphUiLayerInternal(SceneGraphNodeBranchInternal(Nil))
    )

}
