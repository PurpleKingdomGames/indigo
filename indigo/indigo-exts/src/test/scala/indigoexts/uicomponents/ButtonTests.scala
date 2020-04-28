package indigoexts.uicomponents

import utest._
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.scenegraph.Graphic
import indigo.shared.events.MouseState
import indigo.shared.datatypes.Point
import indigo.shared.events.MouseEvent

object ButtonTests extends TestSuite {

  val bounds =
    Rectangle(10, 10, 100, 100)

  val button =
    Button(
      ButtonAssets(
        Graphic(bounds, 1, Material.Textured(AssetName("up"))),
        Graphic(bounds, 1, Material.Textured(AssetName("over"))),
        Graphic(bounds, 1, Material.Textured(AssetName("down")))
      ),
      bounds,
      Depth(1)
    )

  val tests: Tests =
    Tests {

      "Mouse interactions" - {

        "Initial state is up" - {
          button.state.isUp ==> true
        }

        "Transition from Up -> Over when mouse over" - {
          button.state.isUp ==> true

          val mouse =
            new MouseState(Nil, Point(20, 20), false)

          val actual = button.update(mouse)

          actual.state.state.isOver ==> true
        }

        "Transition from Over -> Up when mouse out" - {
          val mouse =
            new MouseState(Nil, Point(0, 0), false)

          val actual = button.toOverState.update(mouse)

          actual.state.state.isUp ==> true
        }

        "Transition from Over -> Down on mouse press" - {
          val mouse =
            new MouseState(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false)

          val actual = button.toOverState.update(mouse)

          actual.state.state.isDown ==> true
        }

        "Transition from Down -> Over on mouse release" - {
          val mouse =
            new MouseState(List(MouseEvent.MouseUp(20, 20)), Point(20, 20), false)

          val actual = button.toDownState.update(mouse)

          actual.state.state.isOver ==> true
        }

      }

    }

}
