module TextInputComponent exposing (..)

import Html exposing (..)
import Html.Events exposing (onInput)
import Html.Attributes exposing (..)


type alias TextInputModel =
    String


initial : String -> TextInputModel
initial default =
    default


type TextInputMsg
    = Update String


update : TextInputMsg -> TextInputModel -> TextInputModel
update msg model =
    case msg of
        Update msg ->
            msg


view : String -> TextInputModel -> Html TextInputMsg
view label model =
    div []
        [ text label
        , input [ value (toString model), onInput Update ] []
        ]
