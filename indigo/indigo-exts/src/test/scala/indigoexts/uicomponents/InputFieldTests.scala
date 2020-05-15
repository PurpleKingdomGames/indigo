package indigoexts.uicomponents

import utest._

object InputFieldTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Editing operations" - {

        val atCommaPosition =
          InputField("Hello, world!").cursorHome.cursorRight.cursorRight.cursorRight.cursorRight.cursorRight

        "cursor movement" - {
          InputField("Hello, world!").cursorHome.cursorPosition ==> 0
          InputField("Hello, world!").cursorHome.cursorRight.cursorPosition ==> 1
          InputField("Hello, world!").cursorHome.cursorRight.cursorRight.cursorPosition ==> 2
          InputField("Hello, world!").cursorHome.cursorRight.cursorRight.cursorLeft.cursorPosition ==> 1
          InputField("Hello, world!").cursorHome.cursorRight.cursorRight.cursorLeft.cursorLeft.cursorPosition ==> 0
        }
        "cursor home" - {
          InputField("Hello, world!").cursorHome.cursorPosition ==> 0
        }
        "cursor end" - {
          InputField("Hello, world!").cursorHome.cursorEnd.cursorPosition ==> "Hello, world!".length()
        }
        "delete" - {
          InputField("Hello, world!").cursorHome.delete.text ==> "ello, world!"
          InputField("Hello, world!").cursorHome.delete.cursorPosition ==> 0
          InputField("Hello, world!").cursorEnd.delete.text ==> "Hello, world!"
          InputField("Hello, world!").cursorEnd.delete.cursorPosition ==> "Hello, world!".length()

          atCommaPosition.delete.text ==> "Hello world!"
          atCommaPosition.delete.cursorPosition ==> 5
        }
        "backspace" - {
          InputField("Hello, world!").cursorHome.backspace.text ==> "Hello, world!"
          InputField("Hello, world!").cursorHome.backspace.cursorPosition ==> 0
          InputField("Hello, world!").cursorEnd.backspace.text ==> "Hello, world"
          InputField("Hello, world!").cursorEnd.backspace.cursorPosition ==> "Hello, world!".length() - 1

          atCommaPosition.backspace.text ==> "Hell, world!"
          atCommaPosition.backspace.cursorPosition ==> 4
        }
        "add character(s)" - {
          InputField("Hello, world!").addCharacterText(" Fish!").text ==> "Hello, world! Fish!"

          InputField("Hello, world!").cursorHome.cursorRight.cursorRight.cursorRight.cursorRight.cursorRight.delete
            .addCharacterText(" Fish!")
            .text ==> "Hello Fish! world!"

          InputField("Hello, world!").cursorHome.addCharacter('X').text ==> "XHello, world!"

          InputField("Hello, world!").makeSingleLine
            .addCharacter('x')
            .text ==> "Hello, world!x"

          InputField("Hello, world!").makeSingleLine
            .addCharacter('\n')
            .text ==> "Hello, world!"

          InputField("Hello, world!").makeMultiLine
            .addCharacterText("a\nb")
            .text ==> "Hello, world!a\nb"

          InputField("Hello, world!").makeSingleLine
            .addCharacterText("a\nb")
            .text ==> "Hello, world!ab"
        }

      }

      "Cursor drawing calculations" - {
        1 ==> 2
      }

    }

}
