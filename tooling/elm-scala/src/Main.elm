port module Main exposing (..)

import Browser
import Html exposing (Html, button, div, text)
import Html.Events exposing (onClick)
import Json.Decode as Decode exposing (..)


main =
    Browser.element
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }


type Msg
    = Increment
    | Decrement
    | Doubled Int


type alias Model =
    { amount : Int
    , doubled : Int
    }


port fromElm : Int -> Cmd msg


port toElm : (Int -> msg) -> Sub msg


init : String -> ( Model, Cmd Msg )
init =
    \_ -> ( Model 0 0, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions _ =
    toElm (\d -> Doubled d)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Increment ->
            ( { model | amount = model.amount + 1 }, fromElm <| model.amount + 1 )

        Decrement ->
            ( { model | amount = model.amount - 1 }, fromElm <| model.amount - 1 )

        Doubled d ->
            ( { model | doubled = d }, Cmd.none )


view : Model -> Html Msg
view model =
    div []
        [ button [ onClick Decrement ] [ text "-" ]
        , div [] [ text (String.fromInt model.amount) ]
        , button [ onClick Increment ] [ text "+" ]
        , div [] [ text <| "Doubled: " ++ String.fromInt model.doubled ]
        ]
