module CounterComponent exposing (model, view, update, CounterModel, CounterUpdateMsg)

import Html exposing (..)
import Html.Events exposing (onClick, onInput)
import Html.Attributes exposing (..)


-- Config definitions


type alias CounterModel =
    Int



-- Model


model : CounterModel
model =
    0



-- Update


type CounterUpdateMsg
    = IncrementCounter
    | DecrementCounter
    | InputCounter String


clampValue : Int -> Int -> Int -> Int
clampValue min max newValue =
    Basics.max (min + 1) (Basics.min (max - 1) newValue)


update : Int -> Int -> CounterUpdateMsg -> CounterModel -> CounterModel
update min max msg model =
    case msg of
        IncrementCounter ->
            clampValue min max model + 1

        DecrementCounter ->
            clampValue min max model - 1

        InputCounter str ->
            clampValue min max (Result.withDefault 1 (String.toInt str))



-- View


view : String -> CounterModel -> Html CounterUpdateMsg
view label model =
    div []
        [ text label
        , button [ onClick IncrementCounter ] [ text "+" ]
        , input [ value (toString model), onInput InputCounter ] []
        , button [ onClick DecrementCounter ] [ text "-" ]
        ]
