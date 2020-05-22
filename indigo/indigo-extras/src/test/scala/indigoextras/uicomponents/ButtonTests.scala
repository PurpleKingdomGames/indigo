package indigoextras.uicomponents

import utest._
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.scenegraph.Graphic
import indigo.shared.events.MouseState
import indigo.shared.datatypes.Point
import indigo.shared.events.MouseEvent
import indigo.shared.events.GlobalEvent

object ButtonTests extends TestSuite {

  final case class FakeEvent(message: String) extends GlobalEvent

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
    ).withHoverOverAction(List(FakeEvent("mouse over")))
      .withHoverOutAction(List(FakeEvent("mouse out")))
      .withDownAction(List(FakeEvent("mouse down")))
      .withUpAction(List(FakeEvent("mouse up")))

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

          "Within the button: On mouse over, the over action is performed" - {
            assert(
              actual.globalEvents.length == 1,
              actual.globalEvents.contains(FakeEvent("mouse over"))
            )
          }
        }

        "Transition from Over -> Up when mouse out" - {
          val mouse =
            new MouseState(Nil, Point(0, 0), false)

          val actual = button.toOverState.update(mouse)

          actual.state.state.isUp ==> true

          "Starting within the button: On mouse out, the out action is performed" - {
            assert(
              actual.globalEvents.length == 1,
              actual.globalEvents.contains(FakeEvent("mouse out"))
            )
          }
        }

        "Transition from Over -> Down on mouse press" - {
          val mouse =
            new MouseState(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false)

          val actual = button.toOverState.update(mouse)

          actual.state.state.isDown ==> true

          "Within the button: On mouse down, the down action is performed" - {
            assert(
              actual.globalEvents.length == 1,
              actual.globalEvents.contains(FakeEvent("mouse down"))
            )
          }
        }

        "Transition from Up -> Down on mouse press" - {
          val mouse =
            new MouseState(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false)

          val actual = button.toUpState.update(mouse)

          actual.state.state.isDown ==> true

          "Within the button: On mouse down, the down action is performed" - {
            assert(
              actual.globalEvents.length == 2,
              actual.globalEvents.contains(FakeEvent("mouse over")),
              actual.globalEvents.contains(FakeEvent("mouse down")),
              actual.globalEvents == List(FakeEvent("mouse over"), FakeEvent("mouse down"))
            )
          }
        }

        "Transition from Down -> Over on mouse release" - {
          val mouse =
            new MouseState(List(MouseEvent.MouseUp(20, 20)), Point(20, 20), false)

          val actual = button.toDownState.update(mouse)

          actual.state.state.isOver ==> true

          "Within the button: On mouse release, the up action is performed" - {
            assert(
              actual.globalEvents.length == 1,
              actual.globalEvents.contains(FakeEvent("mouse up"))
            )
          }
        }

        "If the button is down, and the mouse moves out, the button stays down until release." - {
          val actual = for {
            buttonPressed <- button.update(new MouseState(List(MouseEvent.MouseDown(20, 20)), Point(20, 20), false))
            mouseOut      <- buttonPressed.update(new MouseState(Nil, Point(200, 200), false))
            mouseReleased <- mouseOut.update(new MouseState(List(MouseEvent.MouseUp(200, 200)), Point(200, 200), false))
          } yield (buttonPressed.state, mouseOut.state, mouseReleased.state)

          assert(
            actual.state._1.isDown,
            actual.state._2.isDown,
            actual.state._3.isUp
          )

          "If the mouse is moved onto and pressed down on the button, dragged out and released, only the down action is performed." - {
            println(actual.globalEvents)
            assert(
              actual.globalEvents.length == 2,
              actual.globalEvents.contains(FakeEvent("mouse over")),
              actual.globalEvents.contains(FakeEvent("mouse down")),
              actual.globalEvents == List(FakeEvent("mouse over"), FakeEvent("mouse down"))
            )
          }
        }

      }

    }

}
