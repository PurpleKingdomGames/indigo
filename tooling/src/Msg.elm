module Msg exposing (..)

import Browser exposing (UrlRequest)
import Url exposing (Url)


type Msg
    = UrlChange Url
    | ClickedLink UrlRequest
