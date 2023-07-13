package pirate.scenes.level.model

import indigo.*
import indigo.syntax.*
import indigo.physics.*
import pirate.core.TileType
import scala.annotation.tailrec

/*
Most games where you wander around operate on the idea of a
Navigation Mesh (nav mesh), which is some abstract shapes that
roughly map to your game's level map, minus all the pretty details.
They dictate how and where a character can move to.

In this case, we convert the "Tiled" map into a list of bounding
boxes.

Indigo has two sorts of primitives, illustrated by `Rectangle` &
`BoundingBox`. Primitives used for rendering all operate on whole
pixels, hence Rectangle is really:

Rectangle(position: Point(x: Int, y: Int), size: Size(width: Int, height: Int))

That makes rendering nice and easy, but whole pixels are terrible
for abstract representations and for smoothly graphing movement.
So there is a parallel set of geometry primitives, including:

BoundingBox(position: Vertex(x: Double, y: Double), size: Vertex(width: Double, height: Double))

Almost the same, but a different level of explicit precision.

So in this case, the nav mesh is a bunch of bounding boxes that Indigo's physics
model can perform collision checks against.
 */
final case class Platform(navMesh: Batch[Collider[String]], rowCount: Int)

object Platform:

  given CanEqual[Option[BoundingBox], Option[BoundingBox]] = CanEqual.derived

  def fromTerrainMap(terrainMap: TiledGridMap[TileType]): Platform =
    val layer = terrainMap.toListPerLayer.head

    def toNavMesh: List[TiledGridCell[TileType]] => Batch[BoundingBox] =
      filterPlatformTiles andThen
        convertCellsToBoundingBoxes andThen
        weldBoundingBoxes

    Platform(
      toNavMesh(layer).map(b => Collider.Box("platform", b).makeStatic.withFriction(Friction(0.5))),
      terrainMap.layers.head.rowCount
    )

  val filterPlatformTiles: List[TiledGridCell[TileType]] => List[TiledGridCell[TileType]] =
    tiles =>
      tiles
        .filter { cell =>
          cell.tile match
            case TileType.Solid if cell.row != 0 =>
              tiles.find(t => t.column == cell.column && t.row == cell.row - 1) match {
                case Some(TiledGridCell(_, _, TileType.Empty)) =>
                  true

                case _ =>
                  false
              }

            case _ =>
              false

        }

  val convertCellsToBoundingBoxes: List[TiledGridCell[TileType]] => List[BoundingBox] =
    _.flatMap { t =>
      t.tile match
        case TileType.Empty =>
          Nil

        case TileType.Solid =>
          List(BoundingBox(t.column.toDouble, t.row.toDouble, 1, 1))

    }

  val weldBoundingBoxes: List[BoundingBox] => Batch[BoundingBox] =
    rectangles =>
      @tailrec
      def rec(remaining: List[BoundingBox], acc: List[BoundingBox]): Batch[BoundingBox] =
        remaining match
          case Nil =>
            acc.toBatch

          case head :: next =>
            next.find(r => head.y == r.y && (r.x + r.width == head.x || r.x == head.x + head.width)) match
              case Some(r) =>
                if r.x + r.width == head.x then
                  rec(BoundingBox(r.x, r.y, r.width + head.width, 1) :: next.filterNot(_ == r), acc)
                else if r.x == head.x + head.width then
                  rec(BoundingBox(head.x, head.y, head.width + r.width, 1) :: next.filterNot(_ == r), acc)
                else rec(next, head :: acc) // Shouldn't get here.

              case None =>
                rec(next, head :: acc)

      rec(rectangles, Nil)
