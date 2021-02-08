// package indigo.shared.scenegraph

// import indigo.shared.datatypes.RGBA
// import scala.annotation.tailrec

// final case class SceneLayer(nodes: List[SceneGraphNode], tint: RGBA, saturation: Double, magnification: Option[Int]) {

//   def |+|(other: SceneLayer): SceneLayer = {
//     val newSaturation: Double =
//       (saturation, other.saturation) match {
//         case (1d, b) => b
//         case (a, 1d) => a
//         case (a, b)  => Math.min(a, b)
//       }

//     SceneLayer(nodes ++ other.nodes, tint + other.tint, newSaturation, magnification.orElse(other.magnification))
//   }

//   def ++(moreNodes: List[SceneGraphNode]): SceneLayer =
//     SceneLayer(nodes ++ moreNodes, tint, saturation, magnification)

//   def withTint(newTint: RGBA): SceneLayer =
//     SceneLayer(nodes, newTint, saturation, magnification)

//   def withSaturationLevel(amount: Double): SceneLayer =
//     SceneLayer(nodes, tint, amount, magnification)

//   def withMagnification(level: Int): SceneLayer =
//     SceneLayer(nodes, tint, saturation, SceneLayer.sanitiseMagnification(level))

//   def visibleNodeCount: Int = {
//     @tailrec
//     def rec(remaining: List[SceneGraphNode], count: Int): Int =
//       remaining match {
//         case Nil =>
//           count

//         case (g: Group) :: xs =>
//           rec(g.children ++ xs, count)

//         case _ :: xs =>
//           rec(xs, count + 1)
//       }

//     rec(nodes, 0)
//   }

// }

// object SceneLayer {

//   def apply(nodes: List[SceneGraphNode]): SceneLayer =
//     new SceneLayer(nodes, RGBA.None, 1.0d, Option.empty[Int])

//   def None: SceneLayer =
//     SceneLayer(Nil, RGBA.None, 1.0d, Option.empty[Int])

//   def sanitiseMagnification(level: Int): Option[Int] =
//     Option(Math.max(1, Math.min(256, level)))

// }
