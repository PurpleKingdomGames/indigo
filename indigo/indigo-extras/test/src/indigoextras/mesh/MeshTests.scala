package indigoextras.mesh

import indigo.Batch
import indigo.Vertex
import indigo.shared.geometry.LineSegment

class MeshTests extends munit.FunSuite {

  /*
  0-2-2
  |  /|
  0 1 4
  |/  |
  1-3-3
   */
  val quadMesh =
    Mesh(
      vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(0, 1), 2 -> Vertex(1, 0), 3 -> Vertex(1, 1)),
      vertexNext = 4,
      edges = Batch(0 -> Edge(0, 1), 1 -> Edge(1, 2), 2 -> Edge(2, 0), 3 -> Edge(1, 3), 4 -> Edge(3, 2)),
      edgeNext = 5,
      tris = Batch(0 -> Tri(0, 1, 2), 1 -> Tri(4, 3, 1)),
      triNext = 2
    )

  test("addVertex") {
    val actual =
      Mesh.empty.addVertex(Vertex(1, 0))

    val expected =
      Mesh.empty.copy(
        vertices = Batch(0 -> Vertex(1, 0)),
        vertexNext = 1
      )

    assertEquals(actual, expected)
  }

  /*
  0---2
  |  /
  | /
  |/
  1
   */
  test("removeVertex") {
    val actual =
      quadMesh.removeVertex(Vertex(1, 1))

    val expected =
      Mesh(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(0, 1), 2 -> Vertex(1, 0)),
        vertexNext = 4,
        edges = Batch(0 -> Edge(0, 1), 1 -> Edge(1, 2), 2 -> Edge(2, 0)),
        edgeNext = 5,
        tris = Batch(0 -> Tri(0, 1, 2)),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  /*
  0---2
  |  /
  | /
  |/
  1
   */
  test("removeVertexAt (3)") {
    val actual =
      quadMesh.removeVertexAt(3)

    val expected =
      Mesh(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(0, 1), 2 -> Vertex(1, 0)),
        vertexNext = 4,
        edges = Batch(0 -> Edge(0, 1), 1 -> Edge(1, 2), 2 -> Edge(2, 0)),
        edgeNext = 5,
        tris = Batch(0 -> Tri(0, 1, 2)),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  /*
      2
     /|
    / |
   /  |
  1---3
   */
  test("removeVertexAt (0)") {
    val actual =
      quadMesh.removeVertexAt(0)

    val expected =
      Mesh(
        vertices = Batch(1 -> Vertex(0, 1), 2 -> Vertex(1, 0), 3 -> Vertex(1, 1)),
        vertexNext = 4,
        edges = Batch(1 -> Edge(1, 2), 3 -> Edge(1, 3), 4 -> Edge(3, 2)),
        edgeNext = 5,
        tris = Batch(1 -> Tri(4, 3, 1)),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  test("addEdge") {
    val actual =
      Mesh.empty
        .addVertex(Vertex(0, 0))
        .addVertex(Vertex(1, 0))
        .addEdge(Edge(0, 1))

    val expected =
      Mesh.empty.copy(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(1, 0)),
        vertexNext = 2,
        edges = Batch(0 -> Edge(0, 1)),
        edgeNext = 1
      )

    assertEquals(actual, expected)
  }

  test("addEdge - fail - missing vertex") {
    val actual =
      Mesh.empty
        .addVertex(Vertex(0, 0))
        .addVertex(Vertex(1, 0))
        .addEdge(Edge(0, 2))

    val expected =
      Mesh.empty.copy(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(1, 0)),
        vertexNext = 2,
        edges = Batch(),
        edgeNext = 0
      )

    assertEquals(actual, expected)
  }

  /*
  0---2
  |  /|
  | / x <-- edge 4
  |/ x|
  1---3
   */
  test("removeEdge") {
    val actual =
      quadMesh.removeEdge(Edge(3, 2))

    val expected =
      Mesh(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(0, 1), 2 -> Vertex(1, 0), 3 -> Vertex(1, 1)),
        vertexNext = 4,
        edges = Batch(0 -> Edge(0, 1), 1 -> Edge(1, 2), 2 -> Edge(2, 0), 3 -> Edge(1, 3)),
        edgeNext = 5,
        tris = Batch(0 -> Tri(0, 1, 2)),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  /*
  0---2
  |  /|
  | / x <-- edge 4
  |/ x|
  1---3
   */
  test("removeEdgeAt") {
    val actual =
      quadMesh.removeEdgeAt(4)

    val expected =
      Mesh(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(0, 1), 2 -> Vertex(1, 0), 3 -> Vertex(1, 1)),
        vertexNext = 4,
        edges = Batch(0 -> Edge(0, 1), 1 -> Edge(1, 2), 2 -> Edge(2, 0), 3 -> Edge(1, 3)),
        edgeNext = 5,
        tris = Batch(0 -> Tri(0, 1, 2)),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  test("addTri") {
    val actual =
      Mesh.empty
        .addVertex(Vertex(0, 0))
        .addVertex(Vertex(1, 0))
        .addVertex(Vertex(0, 1))
        .addEdge(Edge(0, 1))
        .addEdge(Edge(1, 2))
        .addEdge(Edge(2, 0))
        .addTri(Tri(0, 1, 2))

    val expected =
      Mesh.empty.copy(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(1, 0), 2 -> Vertex(0, 1)),
        vertexNext = 3,
        edges = Batch(0 -> Edge(0, 1), 1 -> Edge(1, 2), 2 -> Edge(2, 0)),
        edgeNext = 3,
        tris = Batch(0 -> Tri(0, 1, 2)),
        triNext = 1
      )

    assertEquals(actual, expected)
  }

  /*
  0-2-2
  |0 /|
  0 1 4
  |/ 1|
  1-3-3
   */
  test("removeTri") {
    val actual =
      quadMesh.removeTri(Tri(4, 3, 1))

    val expected =
      Mesh(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(0, 1), 2 -> Vertex(1, 0), 3 -> Vertex(1, 1)),
        vertexNext = 4,
        edges = Batch(
          0 -> Edge(0, 1),
          1 -> Edge(1, 2),
          2 -> Edge(2, 0),
          3 -> Edge(1, 3),
          4 -> Edge(3, 2)
        ),
        edgeNext = 5,
        tris = Batch(0 -> Tri(0, 1, 2)),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  /*
  0-2-2
  |0 /|
  0 1 4
  |/ 1|
  1-3-3
   */
  test("removeTriAt") {
    val actual =
      quadMesh.removeTriAt(0)

    val expected =
      Mesh(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(0, 1), 2 -> Vertex(1, 0), 3 -> Vertex(1, 1)),
        vertexNext = 4,
        edges = Batch(
          0 -> Edge(0, 1),
          1 -> Edge(1, 2),
          2 -> Edge(2, 0),
          3 -> Edge(1, 3),
          4 -> Edge(3, 2)
        ),
        edgeNext = 5,
        tris = Batch(1 -> Tri(4, 3, 1)),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  test("addTriangle") {
    val actual =
      Mesh
        .fromTriangle(
          Triangle(
            Vertex(0, 0),
            Vertex(0, 1),
            Vertex(1, 0)
          )
        )
        .addTriangle(
          Triangle(
            Vertex(1, 1),
            Vertex(1, 0),
            Vertex(0, 1)
          )
        )

    /* Makes an unoptimised mesh of two distinct triangles
    0-2-2 4
    |  / /|
    0 1 4 3
    |/ /  |
    1 5-5-3
     */
    val expected =
      Mesh(
        vertices = Batch(
          (0, Vertex(0, 0)),
          (1, Vertex(0, 1)),
          (2, Vertex(1, 0)),
          (3, Vertex(1, 1)),
          (4, Vertex(1, 0)),
          (5, Vertex(0, 1))
        ),
        vertexNext = 6,
        edges = Batch(
          (0, Edge(0, 1)),
          (1, Edge(1, 2)),
          (2, Edge(2, 0)),
          (3, Edge(3, 4)),
          (4, Edge(4, 5)),
          (5, Edge(5, 3))
        ),
        edgeNext = 6,
        tris = Batch(
          (0, Tri(0, 1, 2)),
          (1, Tri(3, 4, 5))
        ),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  test("toTriangles") {
    val actual =
      quadMesh.toTriangles

    val expected =
      Batch(
        Triangle(
          Vertex(0, 0),
          Vertex(0, 1),
          Vertex(1, 0)
        ),
        Triangle(
          Vertex(1, 1),
          Vertex(1, 0),
          Vertex(0, 1)
        )
      )

    assertEquals(actual, expected)
  }

  test("toTriangles - real case") {
    val actual =
      Mesh(
        Batch((0, Vertex(0, 10)), (1, Vertex(10, 10)), (2, Vertex(5, 0)), (3, Vertex(5, 5))),
        12,
        Batch(
          (0, Edge(0, 1)),
          (1, Edge(1, 2)),
          (2, Edge(2, 0)),
          (3, Edge(3, 0)),
          (5, Edge(1, 3)),
          (8, Edge(2, 3))
        ),
        12,
        Batch((1, Tri(3, 0, 5)), (2, Tri(5, 1, 8)), (3, Tri(3, 2, 8))),
        4
      ).toTriangles

    val expected =
      Batch(
        Triangle(Vertex(5, 5), Vertex(0, 10), Vertex(10, 10)),
        Triangle(Vertex(10, 10), Vertex(5, 5), Vertex(5, 0)),
        Triangle(Vertex(5, 5), Vertex(0, 10), Vertex(5, 0))
      )

    assertEquals(actual, expected)
  }

  test("toLineSegments") {
    val actual =
      quadMesh.toLineSegments

    val expected =
      Batch(
        LineSegment(Vertex(0, 0), Vertex(0, 1)),
        LineSegment(Vertex(0, 1), Vertex(1, 0)),
        LineSegment(Vertex(1, 0), Vertex(0, 0)),
        LineSegment(Vertex(0, 1), Vertex(1, 1)),
        LineSegment(Vertex(1, 1), Vertex(1, 0))
      )

    assertEquals(actual, expected)
  }

  test("offsetBy") {
    val actual =
      Mesh.offsetIndexesBy(10, 20, 30)(
        Mesh.empty
          .addVertex(Vertex(0, 0))
          .addVertex(Vertex(1, 0))
          .addVertex(Vertex(0, 1))
          .addEdge(Edge(0, 1))
          .addEdge(Edge(1, 2))
          .addEdge(Edge(2, 0))
          .addTri(Tri(0, 1, 2))
      )

    val expected =
      Mesh.empty.copy(
        vertices = Batch(10 -> Vertex(0, 0), 11 -> Vertex(1, 0), 12 -> Vertex(0, 1)),
        vertexNext = 13,
        edges = Batch(20 -> Edge(10, 11), 21 -> Edge(11, 12), 22 -> Edge(12, 10)),
        edgeNext = 23,
        tris = Batch(30 -> Tri(20, 21, 22)),
        triNext = 31
      )

    assertEquals(actual, expected)
  }

  test("combine / |+|") {
    val actual =
      Mesh.combine(
        Mesh.empty
          .addVertex(Vertex(0, 0))
          .addVertex(Vertex(1, 0))
          .addVertex(Vertex(0, 1))
          .addEdge(Edge(0, 1))
          .addEdge(Edge(1, 2))
          .addEdge(Edge(2, 0))
          .addTri(Tri(0, 1, 2)),
        Mesh.empty
          .addVertex(Vertex(1, 0))
          .addVertex(Vertex(0, 1))
          .addVertex(Vertex(1, 1))
          .addEdge(Edge(0, 1))
          .addEdge(Edge(1, 2))
          .addEdge(Edge(2, 0))
          .addTri(Tri(0, 1, 2))
      )

    val expected =
      Mesh.empty.copy(
        vertices = Batch(
          0 -> Vertex(0, 0),
          1 -> Vertex(1, 0),
          2 -> Vertex(0, 1),
          3 -> Vertex(1, 0),
          4 -> Vertex(0, 1),
          5 -> Vertex(1, 1)
        ),
        vertexNext = 6,
        edges = Batch(
          0 -> Edge(0, 1),
          1 -> Edge(1, 2),
          2 -> Edge(2, 0),
          3 -> Edge(3, 4),
          4 -> Edge(4, 5),
          5 -> Edge(5, 3)
        ),
        edgeNext = 6,
        tris = Batch(
          0 -> Tri(0, 1, 2),
          1 -> Tri(3, 4, 5)
        ),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  /*
  0-2-2
  |  /|
  0 1 x <-- edge 4
  |/ x|
  1-3-3 <-- dangling v 3, e 3, to be pruned
   */
  test("prune") {
    val actual =
      quadMesh
        .removeEdgeAt(4) // leaves edge 3 and vertex 3 not associated with a Tri
        .prune

    val expected =
      Mesh(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(0, 1), 2 -> Vertex(1, 0)),
        vertexNext = 4,
        edges = Batch(0 -> Edge(0, 1), 1 -> Edge(1, 2), 2 -> Edge(2, 0)),
        edgeNext = 5,
        tris = Batch(0 -> Tri(0, 1, 2)),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  test("weld") {
    val actual =
      Mesh.empty
        .copy(
          vertices = Batch(
            0 -> Vertex(0, 0),
            1 -> Vertex(1, 0),
            2 -> Vertex(0, 1),
            3 -> Vertex(1, 0),
            4 -> Vertex(0, 1),
            5 -> Vertex(1, 1)
          ),
          vertexNext = 6,
          edges = Batch(
            0 -> Edge(0, 1),
            1 -> Edge(1, 2),
            2 -> Edge(2, 0),
            3 -> Edge(3, 4),
            4 -> Edge(4, 5),
            5 -> Edge(5, 3)
          ),
          edgeNext = 6,
          tris = Batch(
            0 -> Tri(0, 1, 2),
            1 -> Tri(3, 4, 5)
          ),
          triNext = 2
        )
        .weld

    val expected =
      Mesh.empty.copy(
        vertices = Batch(
          0 -> Vertex(0, 0),
          1 -> Vertex(1, 0),
          2 -> Vertex(0, 1),
          // 3 -> Vertex(1, 0), // copy of 1
          // 4 -> Vertex(0, 1), // copy of 2
          5 -> Vertex(1, 1)
        ),
        vertexNext = 6, // unchanged, running count, not the length
        edges = Batch(
          0 -> Edge(0, 1),
          1 -> Edge(1, 2),
          2 -> Edge(2, 0),
          // 3 -> Edge(3, 4), // copy of 1
          4 -> Edge(2, 5),
          5 -> Edge(5, 1)
        ),
        edgeNext = 6,
        tris = Batch(
          0 -> Tri(0, 1, 2),
          1 -> Tri(1, 4, 5)
        ),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

  test("weld - to edges with the same vertices but reversed are also welded") {
    val actual =
      Mesh.empty
        .copy(
          vertices = Batch(
            0 -> Vertex(0, 0),
            1 -> Vertex(1, 0)
          ),
          vertexNext = 6,
          edges = Batch(
            0 -> Edge(0, 1),
            1 -> Edge(1, 0)
          ),
          edgeNext = 2
        )
        .weld

    val expected =
      Mesh.empty
        .copy(
          vertices = Batch(
            0 -> Vertex(0, 0),
            1 -> Vertex(1, 0)
          ),
          vertexNext = 6,
          edges = Batch(
            0 -> Edge(0, 1)
          ),
          edgeNext = 2
        )

    assertEquals(actual, expected)
  }

  test("fromTriangle") {
    val actual =
      Mesh.fromTriangle(
        Triangle(
          Vertex(0, 0),
          Vertex(0, 1),
          Vertex(1, 0)
        )
      )

    val expected =
      Mesh(
        vertices = Batch(0 -> Vertex(0, 0), 1 -> Vertex(0, 1), 2 -> Vertex(1, 0)),
        vertexNext = 3,
        edges = Batch(0 -> Edge(0, 1), 1 -> Edge(1, 2), 2 -> Edge(2, 0)),
        edgeNext = 3,
        tris = Batch(0 -> Tri(0, 1, 2)),
        triNext = 1
      )

    assertEquals(actual, expected)
  }

  test("fromTriangles") {
    val actual =
      Mesh.fromTriangles(
        Batch(
          Triangle(
            Vertex(0, 0),
            Vertex(0, 1),
            Vertex(1, 0)
          ),
          Triangle(
            Vertex(1, 1),
            Vertex(1, 0),
            Vertex(0, 1)
          )
        )
      )

    /*
    0-2-2
    |  /|
    0 1 3
    |/  |
    1-5-3
     */
    val expected =
      Mesh(
        vertices = Batch(
          (0, Vertex(0, 0)),
          (1, Vertex(0, 1)),
          (2, Vertex(1, 0)),
          (3, Vertex(1, 1))
        ),
        vertexNext = 6,
        edges = Batch(
          (0, Edge(0, 1)),
          (1, Edge(1, 2)),
          (2, Edge(2, 0)),
          (3, Edge(3, 2)),
          (5, Edge(1, 3))
        ),
        edgeNext = 6,
        tris = Batch(
          (0, Tri(0, 1, 2)),
          (1, Tri(3, 1, 5))
        ),
        triNext = 2
      )

    assertEquals(actual, expected)
  }

}
