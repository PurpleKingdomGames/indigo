module App.Model exposing (..)

import Browser.Navigation as Nav
import Modules.BumpToNormal exposing (BumpToNormal)
import Modules.FontSheet exposing (FontSheet)
import WebGL.Texture as Texture exposing (..)


type Page
    = Home
    | Bump2Normal
    | Lighting2Normal
    | FontSheet
    | NotFound


type alias Model =
    { page : Page
    , navKey : Nav.Key
    , bumpToNormal : BumpToNormal
    , fontSheet : FontSheet
    }


pageToString : Page -> String
pageToString page =
    case page of
        Home ->
            "Home"

        Bump2Normal ->
            "Bump To Normal"

        Lighting2Normal ->
            "Lighting To Normal"

        FontSheet ->
            "Font Sheet"

        NotFound ->
            "Not Found"
