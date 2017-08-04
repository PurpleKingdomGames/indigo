module TextInputComponent exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)


type TextInputModel
    = AsString String
    | AsInt Int
    | AsFloat Float


toInt : TextInputModel -> Int
toInt model =
    case model of
        AsInt i ->
            i

        _ ->
            1


toString : TextInputModel -> String
toString model =
    case model of
        AsString str ->
            str

        AsInt i ->
            (Basics.toString i)

        AsFloat f ->
            (Basics.toString f)


toFloat : TextInputModel -> Float
toFloat model =
    case model of
        AsFloat f ->
            f

        _ ->
            1.0


initial : TextInputModel -> TextInputModel
initial default =
    default


type TextInputMsg
    = Update String


update : TextInputMsg -> TextInputModel -> TextInputModel
update msg model =
    case ( msg, model ) of
        ( Update msg, AsString _ ) ->
            AsString msg

        ( Update msg, AsInt _ ) ->
            AsInt (Result.withDefault 1 (String.toInt msg))

        ( Update msg, AsFloat _ ) ->
            AsFloat (Result.withDefault 1 (String.toFloat msg))


view : String -> TextInputModel -> Html TextInputMsg
view label model =
    div []
        [ text label
        , input [ value (toString model), onInput Update ] []
        ]
