package snake.model.grid

import utest._

object GridSizeTests extends TestSuite {

  val tests: Tests =
    Tests {
      "GridSize" - {

        "should be able to calculate a power of 2 size" - {

          val gridSize: GridSize = GridSize(
            columns = 32,
            rows = 20,
            gridSquareSize = 16
          )

          gridSize.asPowerOf2.value ==> 32

        }

      }
    }

}
