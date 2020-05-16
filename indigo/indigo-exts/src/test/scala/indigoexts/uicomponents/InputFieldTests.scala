package indigoexts.uicomponents

import utest._
import indigo.shared.scenegraph.Text
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.Graphic
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName

object InputFieldTests extends TestSuite {

  val assets =
    InputFieldAssets(
      Text("", 0, 0, 1, FontKey("nada")),
      Graphic(Rectangle(0, 0, 0, 0), 1, Material.Textured(AssetName("fake")))
    )

  val tests: Tests =
    Tests {

      "Editing operations" - {

        val atCommaPosition =
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorRight.cursorRight

        "cursor movement" - {
          InputField("Hello, world!", assets).cursorHome.cursorPosition ==> 0
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorPosition ==> 1
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorPosition ==> 2
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorLeft.cursorPosition ==> 1
          InputField("Hello, world!", assets).cursorHome.cursorRight.cursorRight.cursorLeft.cursorLeft.cursorPosition ==> 0
        }
        "cursor left ignore newlines" - {
          InputField("ab\nc", assets).cursorEnd.cursorPosition ==> 4
          InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorPosition ==> 2
          InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorLeft.cursorPosition ==> 1
          InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorLeft.cursorLeft.cursorPosition ==> 0
          InputField("ab\nc", assets).cursorEnd.cursorLeft.cursorLeft.cursorLeft.cursorLeft.cursorPosition ==> 0
        }
        "cursor right ignore newlines" - {
          InputField("ab\nc", assets).cursorHome.cursorPosition ==> 0
          InputField("ab\nc", assets).cursorHome.cursorRight.cursorPosition ==> 1
          InputField("ab\nc", assets).cursorHome.cursorRight.cursorRight.cursorPosition ==> 2
          InputField("ab\nc", assets).cursorHome.cursorRight.cursorRight.cursorRight.cursorPosition ==> 4
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

    }

}
