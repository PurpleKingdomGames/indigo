module App.Msg exposing (..)

import Browser exposing (UrlRequest)
import Modules.BumpToNormal exposing (BumpToNormalMsg)
import Url exposing (Url)


type Msg
    = UrlChange Url
    | ClickedLink UrlRequest
    | BumpToNormalMsgWrapper BumpToNormalMsg
