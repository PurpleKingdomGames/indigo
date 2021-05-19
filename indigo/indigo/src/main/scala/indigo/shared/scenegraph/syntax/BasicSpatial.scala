package indigo.shared.scenegraph.syntax

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Flip

trait BasicSpatial[T]:
  extension (spatial: T)
    def withPosition(newPosition: Point): T
    def withRotation(newRotation: Radians): T
    def withScale(newScale: Vector2): T
    def withDepth(newDepth: Depth): T
    def withFlip(newFlip: Flip): T

object BasicSpatial:
  import scala.deriving.*
  import scala.compiletime.summonAll

  private def basicSpatialSum[T](s: Mirror.SumOf[T], instances: => List[BasicSpatial[?]]): BasicSpatial[T] =
    def usingInstanceFor[O](input: T)(f: BasicSpatial[input.type] => O): O =
      val ordx = s.ordinal(input)
      f(instances(ordx).asInstanceOf[BasicSpatial[input.type]])

    new BasicSpatial[T]:
      extension (bspatial: T)
        def withPosition(newPosition: Point): T   = usingInstanceFor(bspatial)(_.withPosition(bspatial)(newPosition))
        def withRotation(newRotation: Radians): T = usingInstanceFor(bspatial)(_.withRotation(bspatial)(newRotation))
        def withScale(newScale: Vector2): T       = usingInstanceFor(bspatial)(_.withScale(bspatial)(newScale))
        def withDepth(newDepth: Depth): T         = usingInstanceFor(bspatial)(_.withDepth(bspatial)(newDepth))
        def withFlip(newFlip: Flip): T            = usingInstanceFor(bspatial)(_.withFlip(bspatial)(newFlip))

  inline given derived[T](using m: Mirror.SumOf[T]): BasicSpatial[T] =
    type BasicSpatialInstances = Tuple.Map[m.MirroredElemTypes, BasicSpatial]
    lazy val basicSpatialInstances = summonAll[BasicSpatialInstances].toList.asInstanceOf[List[BasicSpatial[?]]]
    basicSpatialSum(m, basicSpatialInstances)
