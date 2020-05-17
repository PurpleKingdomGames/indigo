package indigoexts.uicomponents

import utest._
import indigo.shared.scenegraph.Text
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.Graphic
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.FontRegister
import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontInfo
import indigo.shared.AnimationsRegister
import indigo.shared.time.GameTime
import indigo.shared.datatypes.Point

object InputFieldTests extends TestSuite {

  import Samples._

  val assets =
    InputFieldAssets(
      Text("", 0, 0, 1, fontKey),
      Graphic(Rectangle(0, 0, 0, 0), 1, Material.Textured(AssetName("fake")))
    )

  val fontRegister: FontRegister =
    new FontRegister

  fontRegister.register(fontInfo)

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, fontRegister)

  val tests: Tests =
    Tests {

      "Editing operations" - {

        val atCommaPosition =
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorRight.cursorRight

        "set cursor position directly" - {
          val text = "Hello, world!"
          val field = InputField(text, assets).cursorHome
          field.moveCursorTo(-1).cursorPosition ==> 0
          field.moveCursorTo(0).cursorPosition ==> 0
          field.moveCursorTo(text.length() + 1).cursorPosition ==> text.length() - 1
          field.moveCursorTo(text.length()).cursorPosition ==> text.length() - 1
          field.moveCursorTo(text.length() - 1).cursorPosition ==> text.length() - 1
          field.moveCursorTo(5).cursorPosition ==> 5
        }
        "cursor movement" - {
          InputField("Hello, world!", assets).cursorHome.cursorPosition ==> 0
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorPosition ==> 1
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorPosition ==> 2
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorLeft.cursorPosition ==> 1
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorLeft.cursorLeft.cursorPosition ==> 0
        }
        "cursor left treats newlines as a normal character" - {
          InputField("ab\nc", assets).cursorEnd.cursorPosition ==> 4
          InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorPosition ==> 3
          InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorLeft.cursorPosition ==> 2
          InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorLeft.cursorLeft.cursorPosition ==> 1
          InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorLeft.cursorLeft.cursorLeft.cursorPosition ==> 0
        }
        "cursor right treats newlines as a normal character" - {
          InputField("ab\nc", assets).cursorHome.cursorPosition ==> 0
          InputField("ab\nc", assets).cursorHome.cursorRight.cursorPosition ==> 1
          InputField("ab\nc", assets).cursorHome.cursorRight.cursorRight.cursorPosition ==> 2
          InputField("ab\nc", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorPosition ==> 3
          InputField("ab\nc", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorRight.cursorPosition ==> 4
        }
        "cursor home" - {
          InputField("Hello, world!", assets).cursorHome.cursorPosition ==> 0
        }
        "cursor end" - {
          InputField("Hello, world!", assets).cursorHome.cursorEnd.cursorPosition ==> "Hello, world!".length()
        }
        "delete" - {
          InputField("Hello, world!", assets).cursorHome.delete.text ==> "ello, world!"
          InputField("Hello, world!", assets).cursorHome.delete.cursorPosition ==> 0
          InputField("Hello, world!", assets).cursorEnd.delete.text ==> "Hello, world!"
          InputField("Hello, world!", assets).cursorEnd.delete.cursorPosition ==> "Hello, world!".length()

          atCommaPosition.delete.text ==> "Hello world!"
          atCommaPosition.delete.cursorPosition ==> 5
        }
        "backspace" - {
          InputField("Hello, world!", assets).cursorHome.backspace.text ==> "Hello, world!"
          InputField("Hello, world!", assets).cursorHome.backspace.cursorPosition ==> 0
          InputField("Hello, world!", assets).cursorEnd.backspace.text ==> "Hello, world"
          InputField("Hello, world!", assets).cursorEnd.backspace.cursorPosition ==> "Hello, world!".length() - 1

          atCommaPosition.backspace.text ==> "Hell, world!"
          atCommaPosition.backspace.cursorPosition ==> 4
        }
        "add character(s)" - {
          InputField("Hello, world!", assets).addCharacterText(" Fish!").text ==> "Hello, world! Fish!"

          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorRight.cursorRight.delete
            .addCharacterText(" Fish!")
            .text ==> "Hello Fish! world!"

          InputField("Hello, world!", assets).cursorHome.addCharacter('X').text ==> "XHello, world!"

          InputField("Hello, world!", assets).makeSingleLine
            .addCharacter('x')
            .text ==> "Hello, world!x"

          InputField("Hello, world!", assets).makeSingleLine
            .addCharacter('\n')
            .text ==> "Hello, world!"

          InputField("Hello, world!", assets).makeMultiLine
            .addCharacterText("a\nb")
            .text ==> "Hello, world!a\nb"

          InputField("Hello, world!", assets).makeSingleLine
            .addCharacterText("a\nb")
            .text ==> "Hello, world!ab"
        }

      }

      "Multi line boxes have bounds correctly caluculated" - {
        val actual =
          InputField("ab\nc", assets).moveTo(50, 50).bounds(boundaryLocator)

        val expected =
          Rectangle(50, 50, 26, 36)

        actual ==> expected
      }

      "Cursor drawing" - {

        val initialPosition: Point =
          Point(50, 50)

        val inputField =
          InputField("ab\nc", assets).noCursorBlink.giveFocus
            .moveTo(initialPosition)

        def extractCursorPosition(field: InputField): Point =
          field
            .draw(GameTime.zero, boundaryLocator)
            .uiLayer
            .nodes
            .collect { case g: Graphic => g }
            .head
            .position

        "home" - {
          val actual =
            extractCursorPosition(inputField.cursorHome)

          val expected =
            initialPosition

          actual ==> expected
        }

        "somewhere in the middle" - {
          val actual =
            extractCursorPosition(inputField.cursorHome.cursorRight)

          val expected =
            initialPosition + Point(16, 0)

          actual ==> expected
        }

        "end" - {
          val actual =
            extractCursorPosition(inputField.cursorEnd)

          val expected =
            initialPosition + Point(16, 20)

          actual ==> expected
        }

        "newlines move cursor to home on next line" - {
          val actual =
            extractCursorPosition(inputField.moveTo(Point.zero).moveCursorTo(3))

          val expected =
            Point(0, 20)

          actual ==> expected
        }

      }

    }

  object Samples {
    val material = Material.Textured(AssetName("font-sheet"))

    val chars = List(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 10, 20),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("test1")

    val fontInfo = FontInfo(fontKey, material, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)
  }

}
