package indigoextras.ui

import indigo.platform.assets.DynamicText
import indigo.platform.renderer.Renderer
import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.Context
import indigo.shared.FontRegister
import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.constants.Key
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.events.InputState
import indigo.shared.events.KeyboardEvent
import indigo.shared.input.Gamepad
import indigo.shared.input.Keyboard
import indigo.shared.input.Mouse
import indigo.shared.input.Pointers
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Text
import indigo.shared.time.GameTime

class InputFieldTests extends munit.FunSuite {

  import Samples._

  val assets =
    InputFieldAssets(
      Text("", 0, 0, 1, fontKey, material),
      Graphic(Rectangle(0, 0, 0, 0), 1, Material.Bitmap(AssetName("fake")))
    )

  val fontRegister: FontRegister =
    new FontRegister

  fontRegister.register(fontInfo)

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, fontRegister, new DynamicText())
  val bounds: Context.Services.Bounds =
    Context.Services.Bounds(boundaryLocator)

  val atCommaPosition =
    InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorRight.cursorRight

  test("Editing operations.set cursor position directly") {
    val text  = "Hello, world!"
    val field = InputField(text, assets).cursorHome
    assertEquals(field.moveCursorTo(-1).cursorPosition, 0)
    assertEquals(field.moveCursorTo(0).cursorPosition, 0)
    assertEquals(field.moveCursorTo(text.length() + 1).cursorPosition, text.length() - 1)
    assertEquals(field.moveCursorTo(text.length()).cursorPosition, text.length() - 1)
    assertEquals(field.moveCursorTo(text.length() - 1).cursorPosition, text.length() - 1)
    assertEquals(field.moveCursorTo(5).cursorPosition, 5)
  }
  test("Editing operations.cursor movement") {
    assertEquals(InputField("Hello, world!", assets).cursorHome.cursorPosition, 0)
    assertEquals(InputField("Hello, world!", assets).cursorHome.cursorRight.cursorPosition, 1)
    assertEquals(InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorPosition, 2)
    assertEquals(InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorLeft.cursorPosition, 1)
    assertEquals(
      InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorLeft.cursorLeft.cursorPosition,
      0
    )
  }
  test("Editing operations.cursor left treats newlines as a normal character") {
    assertEquals(InputField("ab\nc", assets).cursorEnd.cursorPosition, 4)
    assertEquals(InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorPosition, 3)
    assertEquals(InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorLeft.cursorPosition, 2)
    assertEquals(InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorLeft.cursorLeft.cursorPosition, 1)
    assertEquals(InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorLeft.cursorLeft.cursorLeft.cursorPosition, 0)
  }
  test("Editing operations.cursor right treats newlines as a normal character") {
    assertEquals(InputField("ab\nc", assets).cursorHome.cursorPosition, 0)
    assertEquals(InputField("ab\nc", assets).cursorHome.cursorRight.cursorPosition, 1)
    assertEquals(InputField("ab\nc", assets).cursorHome.cursorRight.cursorRight.cursorPosition, 2)
    assertEquals(InputField("ab\nc", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorPosition, 3)
    assertEquals(
      InputField("ab\nc", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorRight.cursorPosition,
      4
    )
  }
  test("Editing operations.cursor home") {
    assertEquals(InputField("Hello, world!", assets).cursorHome.cursorPosition, 0)
  }
  test("Editing operations.cursor end") {
    assertEquals(InputField("Hello, world!", assets).cursorHome.cursorEnd.cursorPosition, "Hello, world!".length())
  }
  test("Editing operations.delete") {
    assertEquals(InputField("Hello, world!", assets).cursorHome.delete.text, "ello, world!")
    assertEquals(InputField("Hello, world!", assets).cursorHome.delete.cursorPosition, 0)
    assertEquals(InputField("Hello, world!", assets).cursorEnd.delete.text, "Hello, world!")
    assertEquals(InputField("Hello, world!", assets).cursorEnd.delete.cursorPosition, "Hello, world!".length())

    assertEquals(atCommaPosition.delete.text, "Hello world!")
    assertEquals(atCommaPosition.delete.cursorPosition, 5)
  }
  test("Editing operations.backspace") {
    assertEquals(InputField("Hello, world!", assets).cursorHome.backspace.text, "Hello, world!")
    assertEquals(InputField("Hello, world!", assets).cursorHome.backspace.cursorPosition, 0)
    assertEquals(InputField("Hello, world!", assets).cursorEnd.backspace.text, "Hello, world")
    assertEquals(InputField("Hello, world!", assets).cursorEnd.backspace.cursorPosition, "Hello, world!".length() - 1)

    assertEquals(atCommaPosition.backspace.text, "Hell, world!")
    assertEquals(atCommaPosition.backspace.cursorPosition, 4)
  }
  test("Editing operations.add character(s)") {
    assertEquals(InputField("Hello, world!", assets).addCharacterText(" Fish!").text, "Hello, world! Fish!")

    assertEquals(
      InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorRight.cursorRight.delete
        .addCharacterText(" Fish!")
        .text,
      "Hello Fish! world!"
    )

    assertEquals(InputField("Hello, world!", assets).cursorHome.addCharacter('X').text, "XHello, world!")

    assertEquals(
      InputField("Hello, world!", assets).makeSingleLine
        .addCharacter('x')
        .text,
      "Hello, world!x"
    )

    assertEquals(
      InputField("Hello, world!", assets).makeSingleLine
        .addCharacter('\n')
        .text,
      "Hello, world!"
    )

    assertEquals(
      InputField("Hello, world!", assets).makeMultiLine
        .addCharacterText("a\nb")
        .text,
      "Hello, world!a\nb"
    )

    assertEquals(
      InputField("Hello, world!", assets).makeSingleLine
        .addCharacterText("a\nb")
        .text,
      "Hello, world!ab"
    )
  }

  test("Multi line boxes have bounds correctly caluculated") {
    val actual =
      InputField("ab\nc", assets).moveTo(50, 50).bounds(bounds).get

    val expected =
      Rectangle(50, 50, 26, 36)

    assertEquals(actual, expected)
  }

  val initialPosition: Point =
    Point(50, 50)

  val inputField =
    InputField("ab\nc", assets).noCursorBlink.giveFocus.unsafeGet
      .moveTo(initialPosition)

  def extractCursorPosition(field: InputField): Point =
    field
      .draw(GameTime.zero, bounds)
      .collect { case g: Graphic[_] => g }
      .head
      .position

  test("Cursor drawing.home") {
    val actual =
      extractCursorPosition(inputField.cursorHome)

    val expected =
      initialPosition

    assertEquals(actual, expected)
  }

  test("Cursor drawing.somewhere in the middle") {
    val actual =
      extractCursorPosition(inputField.cursorHome.cursorRight)

    val expected =
      initialPosition + Point(16, 0)

    assertEquals(actual, expected)
  }

  test("Cursor drawing.end") {
    val actual =
      extractCursorPosition(inputField.cursorEnd)

    val expected =
      initialPosition + Point(16, 20)

    assertEquals(actual, expected)
  }

  test("Cursor drawing.newlines move cursor to home on next line") {
    val actual =
      extractCursorPosition(inputField.moveTo(Point.zero).moveCursorTo(3))

    val expected =
      Point(0, 20)

    assertEquals(actual, expected)
  }

  test("Cursor drawing.Updated text emits an event.if the key is set") {
    val key =
      BindingKey("test")

    val field =
      InputField("", assets).giveFocus.unsafeGet.withKey(BindingKey("test"))

    val actual =
      field.update(context).flatMap(_.update(context))

    assertEquals(actual.unsafeGet.text, "ABCABC")
    assertEquals(actual.unsafeGlobalEvents.head, InputFieldChange(key, "ABC"))
    assertEquals(actual.unsafeGlobalEvents(1), InputFieldChange(key, "ABCABC"))
  }

  test("Cursor drawing.Updated text emits an event.unless the key is unset") {
    val field =
      InputField("", assets).giveFocus.unsafeGet

    val actual =
      field.update(context).flatMap(_.update(context))

    assertEquals(actual.unsafeGet.text, "ABCABC")
    assertEquals(actual.unsafeGlobalEvents, Batch.empty)
  }

  test("Cursor drawing.Focusing an input field emits events") {
    val event =
      TestInputFieldEvent("test 1")

    val actual =
      InputField("", assets)
        .withFocusActions(event)
        .giveFocus

    assertEquals(actual.unsafeGet.hasFocus, true)
    assertEquals(actual.unsafeGlobalEvents, Batch(event))
  }

  test("Cursor drawing.Losing focus on an input field emits events") {
    val event =
      TestInputFieldEvent("test 2")

    val actual =
      InputField("", assets)
        .withLoseFocusActions(event)
        .giveFocus
        .unsafeGet
        .loseFocus

    assertEquals(actual.unsafeGet.hasFocus, false)
    assertEquals(actual.unsafeGlobalEvents, Batch(event))
  }

  val keysUp: Batch[KeyboardEvent.KeyUp] =
    Batch(
      KeyboardEvent.KeyUp(Key.KEY_A),
      KeyboardEvent.KeyUp(Key.KEY_B),
      KeyboardEvent.KeyUp(Key.KEY_C)
    )

  def context: Context[Unit] =
    Context.initial
      .modifyFrame(
        _.withDice(Dice.loaded(1))
          .withInput(
            new InputState(Mouse.default, new Keyboard(keysUp, Batch.empty, None), Gamepad.default, Pointers.default)
          )
      )

  object Samples {
    val material = Material.Bitmap(AssetName("font-sheet"))

    val chars = Batch(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 10, 20),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("test1")

    val fontInfo = FontInfo(fontKey, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)
  }

}

final case class TestInputFieldEvent(message: String) extends GlobalEvent
