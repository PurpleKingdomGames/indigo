package indigoexts.grid

import org.scalatest.{FunSpec, Matchers}

class GridSizeSpec extends FunSpec with Matchers {

  describe("GridSize") {

    it("should be able to calculate a power of 2 size") {

      val gridSize: GridSize = GridSize(
        columns = 32,
        rows = 20,
        gridSquareSize = 16
      )

      gridSize.asPowerOf2.value shouldEqual 32

    }

  }

}
