module App.Model exposing (..)

import Browser.Navigation as Nav
import Modules.BumpToNormal exposing (BumpToNormal)
import WebGL.Texture as Texture exposing (..)


type Page
    = Home
    | Bump2Normal
    | NotFound


type alias Model =
    { page : Page
    , navKey : Nav.Key
    , bumpToNormal : BumpToNormal
    }
