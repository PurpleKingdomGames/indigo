package indigoextras.ui

import utest._
import indigo.shared.scenegraph.Graphic
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Point
import indigo.shared.events.GlobalEvent
import indigo.shared.input.Mouse
import indigo.shared.events.MouseEvent

object RadioButtonGroupTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Radio button group update" - {

        final case class RadioTestEvent(message: String) extends GlobalEvent

        val assets =
          ButtonAssets(
            Graphic(Rectangle(0, 0, 10, 10), 1, Material.Textured(AssetName("up"))),
            Graphic(Rectangle(0, 0, 10, 10), 1, Material.Textured(AssetName("over"))),
            Graphic(Rectangle(0, 0, 10, 10), 1, Material.Textured(AssetName("down")))
          )

        val option1 =
          RadioButton(Point(0, 0)).selected
            .withSelectedAction(RadioTestEvent("option 1 selected"))
            .withUnselectedAction(RadioTestEvent("option 1 unselected"))
            .withHoverOverAction(RadioTestEvent("option 1 hover over"))
            .withHoverOutAction(RadioTestEvent("option 1 hover out"))

        val option2 =
          RadioButton(Point(0, 20))
            .withSelectedAction(RadioTestEvent("option 2 selected"))
            .withUnselectedAction(RadioTestEvent("option 2 unselected"))
            .withHoverOverAction(RadioTestEvent("option 2 hover over"))
            .withHoverOutAction(RadioTestEvent("option 2 hover out"))

        val option3 =
          RadioButton(Point(0, 40))
            .withSelectedAction(RadioTestEvent("option 3 selected"))
            .withUnselectedAction(RadioTestEvent("option 3 unselected"))
            .withHoverOverAction(RadioTestEvent("option 3 hover over"))
            .withHoverOutAction(RadioTestEvent("option 3 hover out"))

        val radioButtons =
          RadioButtonGroup(assets, 10, 10)
            .addRadioButton(option1)
            .addRadioButton(option2)
            .addRadioButton(option3)

        "No mouse interaction" - {

          val mouse =
            new Mouse(Nil, Point(-10, -10), false)

          val actual = radioButtons.update(mouse)

          val expected = radioButtons

          actual.state ==> expected
          actual.globalEvents ==> Nil

        }

        "hover over unselected button" - {

          val mouse =
            new Mouse(Nil, Point(5, 25), false)

          val actual = radioButtons.update(mouse)

          val expected =
            radioButtons.copy(
              options = List(
                option1,
                option2.copy(state = RadioButtonState.Hover),
                option3
              )
            )

          actual.state ==> expected
          actual.globalEvents ==> List(RadioTestEvent("option 2 hover over"))
        }

        "hover out unselected button" - {

          val mouse =
            new Mouse(Nil, Point(-5, 25), false)

          val actual =
            radioButtons
              .copy(
                options = List(
                  option1,
                  option2.copy(state = RadioButtonState.Hover),
                  option3
                )
              )
              .update(mouse)

          val expected =
            radioButtons.copy(
              options = List(
                option1,
                option2.copy(state = RadioButtonState.Normal),
                option3
              )
            )

          actual.state ==> expected
          actual.globalEvents ==> List(RadioTestEvent("option 2 hover out"))
        }

        "selecting a hovered button" - {

          val mouse =
            new Mouse(List(MouseEvent.Click(5, 25)), Point(5, 25), true)

          val actual =
            radioButtons
              .copy(
                options = List(
                  option1.copy(state = RadioButtonState.Normal),
                  option2.copy(state = RadioButtonState.Hover),
                  option3
                )
              )
              .update(mouse)

          val expected =
            radioButtons.copy(
              options = List(
                option1.copy(state = RadioButtonState.Normal),
                option2.copy(state = RadioButtonState.Selected),
                option3
              )
            )

          actual.state.options.map(_.state) ==> expected.options.map(_.state)
          actual.globalEvents ==> List(RadioTestEvent("option 2 selected"))
        }

        "selecting a hovered button, existing selected is de-selected" - {
          val mouse =
            new Mouse(List(MouseEvent.Click(5, 25)), Point(5, 25), true)

          val actual =
            radioButtons
              .copy(
                options = List(
                  option1.copy(state = RadioButtonState.Selected),
                  option2.copy(state = RadioButtonState.Hover),
                  option3
                )
              )
              .update(mouse)

          val expected =
            radioButtons.copy(
              options = List(
                option1.copy(state = RadioButtonState.Normal),
                option2.copy(state = RadioButtonState.Selected),
                option3
              )
            )

          actual.state.options.map(_.state) ==> expected.options.map(_.state)
          actual.globalEvents ==> List(RadioTestEvent("option 1 unselected"), RadioTestEvent("option 2 selected"))
        }

      }

    }

}
