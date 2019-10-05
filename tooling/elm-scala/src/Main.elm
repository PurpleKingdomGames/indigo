module Main exposing (..)

import Browser
import Html exposing (Html, button, div, text)
import Html.Events exposing (onClick)
import Msg exposing (..)
import ScalaJSMailbox


type alias Model =
    { amount : Int
    , doubled : Int
    }


main : Program String Model Msg
main =
    Browser.element
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }


init : String -> ( Model, Cmd Msg )
init =
    \_ -> ( Model 0 0, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.map (\m -> ScalaCallback m) ScalaJSMailbox.receive


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Increment ->
            let
                newAmount =
                    model.amount + 1
            in
            ( { model | amount = newAmount }, ScalaJSMailbox.send <| DoubleIt newAmount )

        Decrement ->
            let
                newAmount =
                    model.amount - 1
            in
            ( { model | amount = newAmount }, ScalaJSMailbox.send <| DoubleIt newAmount )

        LogMessage m ->
            ( model, ScalaJSMailbox.send <| LogIt m )

        ScalaCallback (Doubled d) ->
            ( { model | doubled = d }, Cmd.none )

        ScalaCallback Ignore ->
            ( model, Cmd.none )


view : Model -> Html Msg
view model =
    div []
        [ button [ onClick Decrement ] [ text "-" ]
        , div [] [ text (String.fromInt model.amount) ]
        , button [ onClick Increment ] [ text "+" ]
        , div [] [ text <| "Doubled: " ++ String.fromInt model.doubled ]
        , button [ onClick <| LogMessage "Hello, World!" ] [ text "Log something!" ]
        ]
