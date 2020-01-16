module App.Msg exposing (..)

import Browser exposing (UrlRequest)
import Modules.BumpToNormal exposing (BumpToNormalMsg)
import Modules.FontSheet exposing (FontSheetMsg)
import Url exposing (Url)


type FromScala
    = Ignore


type SendToScala
    = LogIt String


type Msg
    = UrlChange Url
    | ClickedLink UrlRequest
    | BumpToNormalMsgWrapper BumpToNormalMsg
    | FontSheetMsgWrapper FontSheetMsg
    | ScalaCallback FromScala
    | LogMessage String
