package indigo.shared.scenegraph.syntax

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Flip

trait BasicSpatial[T]:
  extension (spatial: T)
    def position: Point
    def rotation: Radians
    def scale: Vector2
    def depth: Depth
    def ref: Point
    def flip: Flip

    def withPosition(newPosition: Point): T
    def withRotation(newRotation: Radians): T
    def withScale(newScale: Vector2): T
    def withRef(newRef: Point): T
    def withRef(x: Int, y: Int): T
    def withDepth(newDepth: Depth): T
    def withFlip(newFlip: Flip): T

    def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): T =
      spatial.withPosition(newPosition).withRotation(newRotation).withScale(newScale)
    def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): T =
      spatial
        .withPosition(position + positionDiff)
        .withRotation(rotation + rotationDiff)
        .withScale(scale * scaleDiff)

object BasicSpatial:
  import scala.deriving.*
  import scala.compiletime.summonAll

  private def basicSpatialSum[T](s: Mirror.SumOf[T], instances: => List[BasicSpatial[?]]): BasicSpatial[T] =
    def usingInstanceFor[O](input: T)(f: BasicSpatial[input.type] => O): O =
      val ordx = s.ordinal(input)
      f(instances(ordx).asInstanceOf[BasicSpatial[input.type]])

    new BasicSpatial[T]:
      extension (bspatial: T)
        def position: Point   = usingInstanceFor(bspatial)(_.position(bspatial))
        def rotation: Radians = usingInstanceFor(bspatial)(_.rotation(bspatial))
        def scale: Vector2    = usingInstanceFor(bspatial)(_.scale(bspatial))
        def depth: Depth      = usingInstanceFor(bspatial)(_.depth(bspatial))
        def ref: Point        = usingInstanceFor(bspatial)(_.ref(bspatial))
        def flip: Flip        = usingInstanceFor(bspatial)(_.flip(bspatial))

        def withPosition(newPosition: Point): T   = usingInstanceFor(bspatial)(_.withPosition(bspatial)(newPosition))
        def withRotation(newRotation: Radians): T = usingInstanceFor(bspatial)(_.withRotation(bspatial)(newRotation))
        def withScale(newScale: Vector2): T       = usingInstanceFor(bspatial)(_.withScale(bspatial)(newScale))
        def withRef(newRef: Point): T             = usingInstanceFor(bspatial)(_.withRef(bspatial)(newRef))
        def withRef(x: Int, y: Int): T            = usingInstanceFor(bspatial)(_.withRef(bspatial)(x, y))
        def withDepth(newDepth: Depth): T         = usingInstanceFor(bspatial)(_.withDepth(bspatial)(newDepth))
        def withFlip(newFlip: Flip): T            = usingInstanceFor(bspatial)(_.withFlip(bspatial)(newFlip))

        override def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): T =
          usingInstanceFor(bspatial)(_.transformTo(bspatial)(newPosition, newRotation, newScale))
        override def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): T =
          usingInstanceFor(bspatial)(_.transformBy(bspatial)(positionDiff, rotationDiff, scaleDiff))

  inline given derived[T](using m: Mirror.SumOf[T]): BasicSpatial[T] =
    type BasicSpatialInstances = Tuple.Map[m.MirroredElemTypes, BasicSpatial]
    lazy val basicSpatialInstances = summonAll[BasicSpatialInstances].toList.asInstanceOf[List[BasicSpatial[?]]]
    basicSpatialSum(m, basicSpatialInstances)
