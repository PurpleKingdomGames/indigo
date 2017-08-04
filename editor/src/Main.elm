module Main exposing (..)

import Html exposing (..)
import ConfigEditor.ConfigEditor exposing (..)
import ConfigEditor.ConfigModel exposing (..)


main : Program Never ConfigModel ConfigUpdateMsg
main =
    Html.beginnerProgram { model = configModel, view = configView, update = configUpdate }
