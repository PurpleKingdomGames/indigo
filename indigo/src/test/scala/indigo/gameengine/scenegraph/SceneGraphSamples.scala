package indigo.gameengine.scenegraph

import indigo.gameengine.assets.{AnimationsRegister, FontRegister}
import indigo.gameengine.scenegraph.datatypes._
import indigo.gameengine.scenegraph.animation._
import indigo.collections.NonEmptyList

object SceneGraphSamples {

  case class TestViewDataType()

  val fontKey: FontKey   = FontKey("test")
  val fontInfo: FontInfo = FontInfo(fontKey, "font-sheet", 256, 256, FontChar("a", 0, 0, 16, 16))
  FontRegister.register(fontInfo)

  val animationsKey: AnimationsKey = AnimationsKey("test-anim")
  val animations: Animations =
    Animations(
      animationsKey,
      "ref",
      Point(64, 32),
      CycleLabel("label"),
      Cycle(
        CycleLabel("label"),
        NonEmptyList(Frame.fromBounds(0, 0, 32, 32), List(Frame.fromBounds(32, 0, 32, 32))),
        0,
        0
      ),
      Map.empty[CycleLabel, Cycle],
      Nil
    )

  AnimationsRegister.register(animations)

  val api: SceneGraphRootNode =
    SceneGraphRootNode(
      SceneGraphLayer(
        List(
          Group(
            Text(
              "Hello",
              10,
              10,
              1,
              fontKey
            ),
            Graphic(10, 10, 32, 32, 1, "ref"),
            Sprite(
              BindingKey("test"),
              10,
              10,
              32,
              32,
              1,
              animationsKey
            ),
            Group(
              Graphic(10, 10, 32, 32, 1, "ref1"),
              Graphic(10, 10, 32, 32, 1, "ref2"),
              Graphic(10, 10, 32, 32, 1, "ref3")
            )
          )
        )
      )
    )

}
