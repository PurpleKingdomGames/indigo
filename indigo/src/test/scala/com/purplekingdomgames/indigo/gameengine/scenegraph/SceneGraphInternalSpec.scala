package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneGraphSamples.TestViewDataType
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.gameengine.{GameEvent, GameTypeHolder}
import com.purplekingdomgames.indigo.util.metrics.{IMetrics, Metrics}
import org.scalatest.{FunSpec, Matchers}

class SceneGraphInternalSpec extends FunSpec with Matchers {

  implicit val metrics: IMetrics = Metrics.getNullInstance

  implicit val gameTypeHolder = new GameTypeHolder[TestViewDataType] {}

  describe("Converting a public Scene graph into a private one") {

    it("should be able to do the conversion") {
      pending
      SceneGraphInternal.fromPublicFacing(SceneGraphSamples.api) shouldEqual SceneGraphSamples.internal

    }

  }

}

object SceneGraphSamples {

  case class TestViewDataType()

  val api: SceneGraphRootNode[TestViewDataType] =
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

  val internal: SceneGraphRootNodeInternal[TestViewDataType] =
    SceneGraphRootNodeInternal(
      SceneGraphGameLayerInternal(
        SceneGraphNodeBranchInternal(
          List(
            TextInternal[TestViewDataType](
              "Hello", List(TextLine("Hello", Rectangle(0, 0, 80, 16))), Rectangle(0, 0, 80, 16), AlignLeft, Point(10, 10), Depth(1), FontInfo("ref", 32, 32, FontChar("a", 0, 0, 16, 16)), Effects.default, (_: GameEvent) => None
            ),
            GraphicInternal[TestViewDataType](
              Rectangle(10, 10, 32, 32),
              Depth(1),
              "ref",
              Point.zero,
              Rectangle(10, 10, 32, 32),
              Effects.default,
              (_: GameEvent) => None
            ),
            SpriteInternal[TestViewDataType](
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
              Effects.default,
              (_: GameEvent) => None
            ),
            SceneGraphNodeBranchInternal(
              List(
                GraphicInternal[TestViewDataType](
                  Rectangle(10, 10, 32, 32),
                  Depth(1),
                  "ref1",
                  Point.zero,
                  Rectangle(10, 10, 32, 32),
                  Effects.default,
                  (_: GameEvent) => None
                ),
                GraphicInternal[TestViewDataType](
                  Rectangle(10, 10, 32, 32),
                  Depth(1),
                  "ref2",
                  Point.zero,
                  Rectangle(10, 10, 32, 32),
                  Effects.default,
                  (_: GameEvent) => None
                ),
                GraphicInternal[TestViewDataType](
                  Rectangle(10, 10, 32, 32),
                  Depth(1),
                  "ref3",
                  Point.zero,
                  Rectangle(10, 10, 32, 32),
                  Effects.default,
                  (_: GameEvent) => None
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
