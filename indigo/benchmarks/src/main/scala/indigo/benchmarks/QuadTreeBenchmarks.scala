package indigo.benchmarks

import indigo.*

import japgolly.scalajs.benchmark._
import japgolly.scalajs.benchmark.gui._
import indigoextras.trees.QuadTree.*
import indigoextras.trees.QuadTree

object QuadTreeBenchmarks:

  val empty: QuadTree[String] =
    QuadTree.empty(8, 8)

  val one: QuadTree[String] =
    QuadTree
      .empty(8, 8)
      .insertElement("one", Vertex(0, 0))

  val tree: QuadBranch[String] =
    QuadBranch(
      BoundingBox(0, 0, 8, 8),
      QuadEmpty(BoundingBox(0, 0, 4, 4)),
      QuadBranch(
        BoundingBox(4, 0, 4, 4),
        QuadBranch(
          BoundingBox(4, 0, 2, 2),
          QuadEmpty(BoundingBox(4, 0, 1, 1)),
          QuadEmpty(BoundingBox(5, 0, 1, 1)),
          QuadEmpty(BoundingBox(4, 1, 1, 1)),
          QuadLeaf(BoundingBox(5, 1, 1, 1), Vertex(5, 1), "hello")
        ),
        QuadEmpty(BoundingBox(6, 0, 2, 2)),
        QuadEmpty(BoundingBox(4, 2, 2, 2)),
        QuadEmpty(BoundingBox(6, 2, 2, 2))
      ),
      QuadEmpty(BoundingBox(0, 4, 4, 4)),
      QuadEmpty(BoundingBox(4, 4, 4, 4))
    )

  val needPrune = QuadTree
    .empty(16, 16)
    .insertElement(999, Vertex(9, 2))
    .removeElement(Vertex(9, 2))
    .prune

  val suite = GuiSuite(
    Suite("QuadTree Benchmarks")(
      Benchmark("findClosestTo") {
        tree.findClosestTo(Vertex(5, 1))
      },
      Benchmark("insertElement in an empty tree") {
        empty.insertElement("test", Vertex(0, 0))
      },
      Benchmark("insertElements in an empty tree") {
        empty.insertElements(
          "1" -> Vertex(0, 0),
          "2" -> Vertex(1, 0),
          "3" -> Vertex(2, 0),
          "4" -> Vertex(3, 0)
        )
      },
      Benchmark("insertElement at top level of existing tree") {
        tree.insertElement("test", Vertex(0, 0))
      },
      Benchmark("insertElement in a nested location") {
        tree.insertElement("test", Vertex(4, 0))
      },
      Benchmark("removeElement at top level of existing tree") {
        one.removeElement(Vertex(0, 0))
      },
      Benchmark("removeElement in a nested location") {
        tree.removeElement(Vertex(5, 1))
      },
      Benchmark("prune") {
        needPrune.prune
      },
      Benchmark("subdivision") {
        QuadTree.QuadBranch.subdivide(BoundingBox(0, 0, 100, 200))
      },
      Benchmark("searchByBoundingBox (simple tree)") {
        QuadTree.searchByBoundingBox(SampleTree.oneElementTree, BoundingBox(0, 1, 2, 2))
      },
      Benchmark("searchByBoundingBox") {
        QuadTree.searchByBoundingBox(SampleTree.tree, BoundingBox(0, 1, 2, 2))
      },
      Benchmark("searchByBoundingBoxWithPosition") {
        QuadTree.searchByBoundingBoxWithPosition(SampleTree.tree, BoundingBox(0, 1, 2, 2))
      },
      Benchmark("searchByLine") {
        QuadTree.searchByLine(SampleTree.tree, Vertex(0.5, 0.5), Vertex(3.5, 3.5))
      },
      Benchmark("searchByLineWithPosition") {
        QuadTree.searchByLineWithPosition(SampleTree.tree, Vertex(0.5, 0.5), Vertex(3.5, 3.5))
      },
      Benchmark("toList") {
        SampleTree.tree.toBatch
      },
      Benchmark("toListWithPosition") {
        SampleTree.tree.toBatchWithPosition
      }
    )
  )

object SampleTree {

  val tree: QuadTree[String] = QuadTree
    .empty(4, 4)
    .insertElement("0,0", Vertex(0, 0))
    .insertElement("0,1", Vertex(0, 1))
    .insertElement("0,2", Vertex(0, 2))
    .insertElement("0,3", Vertex(0, 3))
    .insertElement("1,0", Vertex(1, 0))
    .insertElement("1,1", Vertex(1, 1))
    .insertElement("1,2", Vertex(1, 2))
    .insertElement("1,3", Vertex(1, 3))
    .insertElement("2,0", Vertex(2, 0))
    .insertElement("2,1", Vertex(2, 1))
    .insertElement("2,2", Vertex(2, 2))
    .insertElement("2,3", Vertex(2, 3))
    .insertElement("3,0", Vertex(3, 0))
    .insertElement("3,1", Vertex(3, 1))
    .insertElement("3,2", Vertex(3, 2))
    .insertElement("3,3", Vertex(3, 3))

  val oneElementTree: QuadTree[String] = QuadTree
    .empty(4, 4)
    .insertElement("0,1", Vertex(0, 1))

}
