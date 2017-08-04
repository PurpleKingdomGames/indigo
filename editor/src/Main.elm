module Main exposing (..)

import Html exposing (..)
import Components.CheckboxComponent exposing (..)
import ConfigEditor.ConfigEditor exposing (..)


main : Program Never ConfigModel ConfigUpdateMsg
main =
    Html.beginnerProgram { model = configModel, view = configView, update = configUpdate }
