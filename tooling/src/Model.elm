module Model exposing (..)

import Browser.Navigation as Nav


type Page
    = Home
    | Page1
    | Page2
    | NotFound


type alias Model =
    { page : Page
    , navKey : Nav.Key
    }
