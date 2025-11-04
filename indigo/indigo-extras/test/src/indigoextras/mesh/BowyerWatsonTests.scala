package indigoextras.mesh

import indigo.*

class BowyerWatsonTests extends munit.FunSuite:

  test("one point") {

    val superTriangle = Triangle(Vertex(0, 10), Vertex(10, 10), Vertex(10, 0))
    val pt            = Vertex(5, 5)

    val actual =
      BowyerWatson.triangulation(Batch(pt), superTriangle)

    val expected =
      Mesh.empty
        .addVertex(Vertex(0, 10))
        .addVertex(Vertex(10, 10))
        .addVertex(Vertex(10, 0))
        .addVertex(Vertex(5, 5))
        .addEdge(Edge(0, 1))
        .addEdge(Edge(1, 2))
        .addEdge(Edge(2, 0))
        .addEdge(Edge(3, 0))
        .addEdge(Edge(1, 3))
        .addEdge(Edge(2, 3))

    assertEquals(actual.vertices.toList.map(_._2), expected.vertices.toList.map(_._2))
    assertEquals(actual.edges.toList.map(_._2), expected.edges.toList.map(_._2))
    assertEquals(actual.tris.length, 3)
  }
