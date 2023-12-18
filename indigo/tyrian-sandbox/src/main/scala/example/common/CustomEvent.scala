package example.common

trait CustomEvent

enum MyCustomMessage extends CustomEvent:
  case Msg(msg: String)
