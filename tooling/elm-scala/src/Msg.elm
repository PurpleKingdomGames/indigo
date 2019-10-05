module Msg exposing (..)


type FromScala
    = Doubled Int
    | Ignore


type SendToScala
    = DoubleIt Int
    | LogIt String


type Msg
    = Increment
    | Decrement
    | LogMessage String
    | ScalaCallback FromScala
