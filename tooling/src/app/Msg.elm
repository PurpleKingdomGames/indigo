module App.Msg exposing (..)

import Browser exposing (UrlRequest)
import Modules.BumpToNormal exposing (BumpToNormalMsg)
import Url exposing (Url)


type FromScala
    = Doubled Int
    | Ignore


type SendToScala
    = DoubleIt Int
    | LogIt String


type Msg
    = UrlChange Url
    | ClickedLink UrlRequest
    | BumpToNormalMsgWrapper BumpToNormalMsg
    | ScalaCallback FromScala
