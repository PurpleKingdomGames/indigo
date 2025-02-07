package indigoextras.ui.simple

import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.events.MouseButton
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerType
import indigo.shared.input.Pointer
import indigo.shared.input.Pointers
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Graphic

class RadioButtonGroupTests extends munit.FunSuite {

  val assets =
    ButtonAssets(
      Graphic(Rectangle(0, 0, 10, 10), Material.Bitmap(AssetName("up"))),
      Graphic(Rectangle(0, 0, 10, 10), Material.Bitmap(AssetName("over"))),
      Graphic(Rectangle(0, 0, 10, 10), Material.Bitmap(AssetName("down")))
    )

  val option1 =
    RadioButton(Point(0, 0)).selected
      .withSelectedActions(RadioTestEvent("option 1 selected"))
      .withDeselectedActions(RadioTestEvent("option 1 unselected"))
      .withHoverOverActions(RadioTestEvent("option 1 hover over"))
      .withHoverOutActions(RadioTestEvent("option 1 hover out"))

  val option2 =
    RadioButton(Point(0, 20))
      .withSelectedActions(RadioTestEvent("option 2 selected"))
      .withDeselectedActions(RadioTestEvent("option 2 unselected"))
      .withHoverOverActions(RadioTestEvent("option 2 hover over"))
      .withHoverOutActions(RadioTestEvent("option 2 hover out"))

  val option3 =
    RadioButton(Point(0, 40))
      .withSelectedActions(RadioTestEvent("option 3 selected"))
      .withDeselectedActions(RadioTestEvent("option 3 unselected"))
      .withHoverOverActions(RadioTestEvent("option 3 hover over"))
      .withHoverOutActions(RadioTestEvent("option 3 hover out"))

  val radioButtons =
    RadioButtonGroup(assets, 10, 10)
      .withRadioButtons(option1, option2, option3)

  test("No mouse interaction") {

    val mouse =
      new Pointers(
        Batch(Pointer(Point(-10, -10), PointerType.Mouse)),
        Batch.empty
      )

    val actual = radioButtons.update(mouse)

    val expected = radioButtons

    assertEquals(actual.unsafeGet, expected)
    assertEquals(actual.unsafeGlobalEvents, Batch.empty)

  }

  test("hover over unselected button") {

    val mouse =
      new Pointers(
        Batch(Pointer(Point(5, 25), PointerType.Mouse)),
        Batch.empty
      )

    val actual = radioButtons.update(mouse)

    val expected =
      radioButtons.copy(
        options = Batch(
          option1,
          option2.copy(state = RadioButtonState.Hover),
          option3
        )
      )

    assertEquals(actual.unsafeGet, expected)
    assertEquals(actual.unsafeGlobalEvents, Batch(RadioTestEvent("option 2 hover over")))
  }

  test("hover out unselected button") {

    val mouse =
      new Pointers(
        Batch(Pointer(Point(-5, 25), PointerType.Mouse)),
        Batch.empty
      )

    val actual =
      radioButtons
        .copy(
          options = Batch(
            option1,
            option2.copy(state = RadioButtonState.Hover),
            option3
          )
        )
        .update(mouse)

    val expected =
      radioButtons.copy(
        options = Batch(
          option1,
          option2.deselected,
          option3
        )
      )

    assertEquals(actual.unsafeGet, expected)
    assertEquals(actual.unsafeGlobalEvents, Batch(RadioTestEvent("option 2 hover out")))
  }

  test("selecting a hovered button") {

    val mouse =
      new Pointers(
        Batch(Pointer(Point(5, 25), PointerType.Mouse, MouseButton.LeftMouseButton)),
        Batch(PointerEvent.Click(5, 25, PointerType.Mouse))
      )

    val actual =
      radioButtons
        .copy(
          options = Batch(
            option1.deselected,
            option2.copy(state = RadioButtonState.Hover),
            option3
          )
        )
        .update(mouse)

    val expected =
      radioButtons.copy(
        options = Batch(
          option1.deselected,
          option2.selected,
          option3
        )
      )

    assertEquals(actual.unsafeGet.options.map(_.state), expected.options.map(_.state))
    assertEquals(actual.unsafeGlobalEvents, Batch(RadioTestEvent("option 2 selected")))
  }

  test("selecting a hovered button, existing selected is de-selected") {
    val mouse =
      new Pointers(
        Batch(Pointer(Point(5, 25), PointerType.Mouse, MouseButton.LeftMouseButton)),
        Batch(PointerEvent.Click(5, 25, PointerType.Mouse))
      )

    val actual =
      radioButtons
        .copy(
          options = Batch(
            option1.selected,
            option2.copy(state = RadioButtonState.Hover),
            option3
          )
        )
        .update(mouse)

    val expected =
      radioButtons.copy(
        options = Batch(
          option1.deselected,
          option2.selected,
          option3
        )
      )

    assertEquals(actual.unsafeGet.options.map(_.state), expected.options.map(_.state))
    assertEquals(
      actual.unsafeGlobalEvents,
      Batch(RadioTestEvent("option 1 unselected"), RadioTestEvent("option 2 selected"))
    )
  }

}

final case class RadioTestEvent(message: String) extends GlobalEvent
