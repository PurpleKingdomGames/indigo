module CounterComponent exposing (initialModel, view, update, CounterModel, CounterUpdateMsg)

import Html exposing (..)
import Html.Events exposing (onClick, onInput)
import Html.Attributes exposing (..)


-- Config definitions


type alias CounterModel =
    Int



-- Model


initialModel : Int -> CounterModel
initialModel i =
    i



-- Update


type CounterUpdateMsg
    = IncrementCounter
    | DecrementCounter
    | InputCounter String


clampValue : Int -> Int -> Int -> Int
clampValue lowerBound upperBound newValue =
    Basics.max (lowerBound + 1) (Basics.min (upperBound - 1) newValue)


update : Int -> Int -> CounterUpdateMsg -> CounterModel -> CounterModel
update lowerBound upperBound msg model =
    case msg of
        IncrementCounter ->
            clampValue lowerBound upperBound model + 1

        DecrementCounter ->
            clampValue lowerBound upperBound model - 1

        InputCounter str ->
            clampValue lowerBound upperBound (Result.withDefault lowerBound (String.toInt str))



-- View


view : String -> CounterModel -> Html CounterUpdateMsg
view label model =
    div []
        [ text label
        , button [ onClick IncrementCounter ] [ text "+" ]
        , input [ value (toString model), onInput InputCounter ] []
        , button [ onClick DecrementCounter ] [ text "-" ]
        ]
