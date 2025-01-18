package indigo.benchmarks

import indigo.*
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*

object QuadTreeBenchmarks:

  given QuadTree.InsertOptions = QuadTree.DefaultOptions

  val empty: QuadTree[Vertex, String] =
    QuadTree.empty(8, 8)

  val one: QuadTree[Vertex, String] =
    QuadTree
      .empty(8, 8)
      .insert(Vertex(0, 0), "one")

  val tree: QuadTree.Branch[Vertex, String] =
    QuadTree.Branch(
      BoundingBox(0, 0, 8, 8),
      QuadTree.Empty(BoundingBox(0, 0, 4, 4)),
      QuadTree.Branch(
        BoundingBox(4, 0, 4, 4),
        QuadTree.Branch(
          BoundingBox(4, 0, 2, 2),
          QuadTree.Empty(BoundingBox(4, 0, 1, 1)),
          QuadTree.Empty(BoundingBox(5, 0, 1, 1)),
          QuadTree.Empty(BoundingBox(4, 1, 1, 1)),
          QuadTree.Leaf(BoundingBox(5, 1, 1, 1), Vertex(5, 1), "hello")
        ),
        QuadTree.Empty(BoundingBox(6, 0, 2, 2)),
        QuadTree.Empty(BoundingBox(4, 2, 2, 2)),
        QuadTree.Empty(BoundingBox(6, 2, 2, 2))
      ),
      QuadTree.Empty(BoundingBox(0, 4, 4, 4)),
      QuadTree.Empty(BoundingBox(4, 4, 4, 4))
    )

  val needPrune = QuadTree
    .empty(16, 16)
    .insert(Vertex(9, 2), 999)
    .removeAt(Vertex(9, 2))
    .prune

  val suite = GuiSuite(
    Suite("QuadTree Benchmarks")(
      Benchmark("findClosestTo") {
        tree.findClosestTo(Vertex(5, 1))
      },
      Benchmark("insert in an empty tree") {
        empty.insert(Vertex(0, 0), "test")
      },
      Benchmark("insert in an empty tree") {
        empty.insert(
          Vertex(0, 0) -> "1",
          Vertex(1, 0) -> "2",
          Vertex(2, 0) -> "3",
          Vertex(3, 0) -> "4"
        )
      },
      Benchmark("insert at top level of existing tree") {
        tree.insert(Vertex(0, 0), "test")
      },
      Benchmark("insert in a nested location") {
        tree.insert(Vertex(4, 0), "test")
      },
      Benchmark("removeElement at top level of existing tree") {
        one.removeAt(Vertex(0, 0))
      },
      Benchmark("removeElement in a nested location") {
        tree.removeAt(Vertex(5, 1))
      },
      Benchmark("prune") {
        needPrune.prune
      },
      Benchmark("searchByBoundingBox (simple tree)") {
        QuadTree.searchByBoundingBox(SampleTree.oneElementTree, BoundingBox(0, 1, 2, 2))
      },
      Benchmark("searchByBoundingBox") {
        QuadTree.searchByBoundingBox(SampleTree.tree, BoundingBox(0, 1, 2, 2))
      },
      Benchmark("searchByLine") {
        QuadTree.searchByLine(SampleTree.tree, LineSegment(Vertex(0.5, 0.5), Vertex(3.5, 3.5)))
      },
      Benchmark("toList") {
        SampleTree.tree.toBatch
      }
    )
  )

object SampleTree {

  given QuadTree.InsertOptions = QuadTree.DefaultOptions

  val tree: QuadTree[Vertex, String] = QuadTree
    .empty(4, 4)
    .insert(
      Batch(
        (Vertex(0, 0), "0,0"),
        (Vertex(0, 1), "0,1"),
        (Vertex(0, 2), "0,2"),
        (Vertex(0, 3), "0,3"),
        (Vertex(1, 0), "1,0"),
        (Vertex(1, 1), "1,1"),
        (Vertex(1, 2), "1,2"),
        (Vertex(1, 3), "1,3"),
        (Vertex(2, 0), "2,0"),
        (Vertex(2, 1), "2,1"),
        (Vertex(2, 2), "2,2"),
        (Vertex(2, 3), "2,3"),
        (Vertex(3, 0), "3,0"),
        (Vertex(3, 1), "3,1"),
        (Vertex(3, 2), "3,2"),
        (Vertex(3, 3), "3,3")
      )
    )

  val oneElementTree: QuadTree[Vertex, String] = QuadTree
    .empty(4, 4)
    .insert(Vertex(0, 1), "0,1")

}
