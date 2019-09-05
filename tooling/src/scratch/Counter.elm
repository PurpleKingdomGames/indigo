module Main exposing (..)

import Browser
import Html exposing (Html, button, div, text)
import Html.Events exposing (onClick)


main =
    Browser.sandbox { init = initialModel, update = update, view = view }


type Msg
    = Increment
    | Decrement


type alias Model =
    { amount : Int }


initialModel : Model
initialModel =
    Model 0


update : Msg -> Model -> Model
update msg model =
    case msg of
        Increment ->
            { model | amount = model.amount + 1 }

        Decrement ->
            { model | amount = model.amount - 1 }


view : Model -> Html.Html Msg
view model =
    div []
        [ button [ onClick Decrement ] [ text "-" ]
        , div [] [ text (String.fromInt model.amount) ]
        , button [ onClick Increment ] [ text "+" ]
        ]
