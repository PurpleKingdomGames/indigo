// package indigo.shared.scenegraph

// import indigo.gameengine.assets.{AnimationsRegister, FontRegister}
// import indigo.shared.datatypes._
// import indigo.shared.animation._
// import indigo.shared.collections.NonEmptyList

// object SceneGraphSamples {

//   case class TestViewDataType()

//   val fontKey: FontKey   = FontKey("test")
//   val fontInfo: FontInfo = FontInfo(fontKey, "font-sheet", 256, 256, FontChar("a", 0, 0, 16, 16))
//   FontRegister.register(fontInfo)

//   val animationsKey: AnimationKey = AnimationKey("test-anim")
//   val animations: Animation =
//     Animation(
//       animationsKey,
//       ImageAssetRef("ref"),
//       Point(64, 32),
//       CycleLabel("label"),
//       NonEmptyList(
//         Cycle(
//           CycleLabel("label"),
//           NonEmptyList(Frame.fromBounds(0, 0, 32, 32), Frame.fromBounds(32, 0, 32, 32)),
//           0,
//           0
//         )
//       ),
//       Nil
//     )

//   AnimationsRegister.register(animations)

//   val api: SceneGraphRootNode =
//     SceneGraphRootNode(
//       SceneGraphLayer(
//         List(
//           Group(
//             Text(
//               "Hello",
//               10,
//               10,
//               1,
//               fontKey
//             ),
//             Graphic(10, 10, 32, 32, 1, "ref"),
//             Sprite(
//               BindingKey("test"),
//               10,
//               10,
//               32,
//               32,
//               1,
//               animationsKey
//             ),
//             Group(
//               Graphic(10, 10, 32, 32, 1, "ref1"),
//               Graphic(10, 10, 32, 32, 1, "ref2"),
//               Graphic(10, 10, 32, 32, 1, "ref3")
//             )
//           )
//         )
//       )
//     )

// }
